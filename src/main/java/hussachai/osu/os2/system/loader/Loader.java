package hussachai.osu.os2.system.loader;
import hussachai.osu.os2.system.TheSystem;
import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.storage.Buffer;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.storage.Memory.Partition;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 
 * @author hussachai
 *
 */
public class Loader {
    
    private Buffer buffer;
    
    private Memory memory;
    
    /* Keep tracking last position file has been read */
    private long lastFilePointer = 0;
    
    public void init(TheSystem system){
        this.buffer = new Buffer(8);
        this.memory = system.getMemory();
    }
    
    /**
     * 
     * @param file
     * @return context if the job is already transfered to memory 
     * or null if memory is not available.
     */
    @SuppressWarnings("resource")/* JDK bug: false resource leak warning */
    public void loader(File file, Context context){
        
        boolean done = false;
        String data = null;
        RandomAccessFile dataInput = null;
        
        try{
            
            dataInput = new RandomAccessFile(file, "r");
            
            /* resume reading from the last point */
            dataInput.seek(this.lastFilePointer);
            
            while( (data = dataInput.readLine()) !=null){
                
                /*guarantee that no space surrounding data*/
                data = data.trim();
                
                /* empty line is used as job delimiter */
                if(data.length()==0){
                    if(context.isVisitedSome()){
                        if(context.lastPart!=ModulePart.END){
                            moveFilePointerToNewModule(dataInput);
                            throw new SystemException(Errors.PROG_MISSING_END_RECORD);
                        }
                    }else{
                        continue;
                    }
                }
                
                try{
                    done = parseLine(context, data);
                }catch(SystemException e){
                    moveFilePointerToNewModule(dataInput);
                    throw e;
                }
                
                if(done){
                    
                    this.lastFilePointer = dataInput.getFilePointer();
                    
                    if(context.length < context.actualLength){
                        throw new SystemException(Errors.MEM_INCORRECT_RESERVED_SIZE);
                    }
                    /* validate # of data */
                    if(context.dataLines < context.dataCount){
                        throw new SystemException(Errors.PROG_EXTRA_DATA_UNUSED);
                    }else if(context.dataLines > context.dataCount){
                        throw new SystemException(Errors.PROG_MISSING_DATA_ITEMS);
                    }
                    /* make sure there's nothing left in buffer */
                    flushBuffer(context);
                    
                    break;
                }
            };
            
            /* signal the end of file to tell system that there's no more jobs*/
            if( (!done) && data==null) throw new EndOfBatchException();
            
        }catch(Exception e){
            
            memory.deallocate(context.partition);
            
            if(e instanceof RuntimeException) throw (RuntimeException)e;
            throw new SystemException(e);
            
        }finally{
            try{
                if(dataInput!=null) dataInput.close(); 
            }catch(Exception e){}
        }
        
    }
    
    private void moveFilePointerToNewModule(RandomAccessFile dataInput){
        String data = null;
        try{
            while( (data = dataInput.readLine()) !=null){
                if(data.trim().length()==0){
                    this.lastFilePointer = dataInput.getFilePointer();
                    break;
                }
            }
            if(data==null){
                throw new EndOfBatchException();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * @param context
     * @param data
     * @return true if the program is end 
     */
    private boolean parseLine(Context context, String data){
        
        if(data.startsWith("**")){
            String commands[] = data.split("\\s+");
            if(commands.length==4){
                //JOB command
                if("JOB".equals(commands[1])){
                    if(context.lastPart != null){
                        throw new SystemException(Errors.PROG_MISSING_END_RECORD);
                    }
                    context.visit(ModulePart.JOB);
                    context.dataLines = Word.fromHexString(commands[2]).toDecimal();
                    context.outputLines = Word.fromHexString(commands[3]).toDecimal();
                }else{
                    throw new SystemException(Errors.PROG_INVALID_FORMAT);
                }
            }else if(commands.length==2){
                //DATA or END command
                if("DATA".equals(commands[1])){
                    if(context.lastPart != ModulePart.JOB_PAYLOAD_END){
                        throw new SystemException(Errors.PROG_MISSING_START_ADDRESS);
                    }
                    if(!context.isVisited(ModulePart.JOB_PAYLOAD)){
                        throw new SystemException(Errors.PROG_NULL_JOB);
                    }
                    context.visit(ModulePart.DATA);
                }else if("END".equals(commands[1])){
                    if(context.lastPart==ModulePart.DATA
                            && context.dataLines>0){
                        throw new SystemException(Errors.PROG_MISSING_DATA_ITEMS);
                    }else if(
                            (context.lastPart!=ModulePart.DATA_PAYLOAD) &&
                            !(context.lastPart==ModulePart.DATA && context.dataLines==0)
                            ){
                        throw new SystemException(Errors.PROG_MISSING_DATA_RECORD);
                    }
                    context.visit(ModulePart.END);
                    return true;
                }else{
                    throw new SystemException(Errors.PROG_INVALID_FORMAT);
                }
            }else{
                throw new SystemException(Errors.PROG_INVALID_FORMAT);
            }
        }else{
            
            validateInputChars(data);
            
            if(data.length()==3){
                /* if the data length is 3 and length word hasn't been read yet,
                 * it is supposed to be the length check data */
                if(context.length==-1){
                    if(context.lastPart!=ModulePart.JOB){
                        throw new SystemException(Errors.PROG_MISSING_JOB_RECORD);
                    }
                    context.visit(ModulePart.JOB_PAYLOAD_START);
                    context.length = Word.fromHexString(data).toDecimal();
                    int size = context.length+context.dataLines+context.outputLines;
                    context.partition = memory.allocate(size);
                    if(context.partition==null){
                        throw new MemoryNotAvailableException();
                    }
                    return false;
                }
                /* if the data length is 3 and length word hasn't been read yet,
                 * it is supposed to be the length check data */
                if(context.lastPart==ModulePart.DATA
                        || context.lastPart==ModulePart.DATA_PAYLOAD){
                    context.visit(ModulePart.DATA_PAYLOAD);
                    context.dataCount++;
                    buffer.add(Word.fromHexString(data));
                    if(buffer.isFull()){
                        flushBuffer(context);
                    }
                    return false;
                }
            }
            if(data.indexOf(" ")!=-1){
                /* if there is space in the data, it suppose to be 
                 * the start address and trace switch */
                String parts[] = data.split("\\s+");
                if(parts.length!=2){
                    throw new SystemException(Errors.PROG_INVALID_TRACEBIT);
                }
                context.lastPart = ModulePart.JOB_PAYLOAD_END;
                context.visitedParts[ModulePart.JOB_PAYLOAD_END.ordinal()] = true;
                context.startAddress = Word.fromHexString(parts[0]);
                if(parts.length>1){
                    context.traceSwitch = Word.fromHexString(parts[1])
                            .toDecimal()>0?Bit.I:Bit.O;
                }
            }else{
                if(context.lastPart != ModulePart.JOB_PAYLOAD_START
                        && context.length == -1){
                    throw new SystemException(Errors.PROG_MISSING_LENGTH_CHECK);
                }
                
                if(data.length()%3!=0){/* wrong hex format */
                    throw new SystemException(Errors.PROG_INVALID_FORMAT);
                }
                
                context.lastPart = ModulePart.JOB_PAYLOAD;
                context.visitedParts[ModulePart.JOB_PAYLOAD.ordinal()] = true;
                String wordStr = null;
                for(int i=0; i<data.length();i+=3){
                    wordStr = data.substring(i, i+3);
                    context.actualLength++;
                    context.instruction = Word.fromHexString(wordStr);
                    buffer.add(context.instruction);
                    if(buffer.isFull()){
                        flushBuffer(context);
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Transfer all data in buffer to memory regardless of the 
     * number of data in buffer.
     * @param context
     */
    private void flushBuffer(Context context){
        Word words[] = buffer.flush();
        for(Word word: words){
            memory.memory(context.partition, Memory.Signal.WRIT, context.memoryIndex, word);
            context.memoryIndex++;
        }
    }
    
    /**
     * Check the input character whether it is in
     * the accepted characters or not.
     * Code
     * 0-9 = 48-57
     * A-F = 65-70
     * a-f = 97-102
     * and space (32)
     * @param input
     */
    private void validateInputChars(String data){
        for(char input : data.toCharArray()){
            int code = (int)input;
            if((code>47 && code<58)
                    || (code>64 && code<71)
                    || (code>96 && code<103)
                    || code==32 && data.split("\\s+").length==2){
                continue;
            }
            throw new SystemException(Errors.PROG_UNRECOGNIZED_CHAR);
        }
    }
    
    /**
     * Enum of module part
     * @author hussachai
     *
     */
    public static enum ModulePart {
        JOB, 
        /** the first line of job payload. It's the length check */
        JOB_PAYLOAD_START,
        JOB_PAYLOAD,
        /**
         * the last line of job payload. It has start address
         * and switch bit */
        JOB_PAYLOAD_END,
        DATA, 
        DATA_PAYLOAD, 
        END
    }
    
    /**
     * Loader Context
     * @author hussachai
     *
     */
    public static class Context {
        
        /** Allocated partition of memory */
        protected Partition partition;
        
        /** Virtual memory address*/
        protected int memoryIndex = 0;
        
        protected boolean visitedParts[] = new boolean[ModulePart.values().length];
        /**
         * The last encounter part. Loader uses this value
         * to validate the sequence of command.
         */
        protected ModulePart lastPart = null;
        
        /**
         * This value is used for validating the actual number of data 
         * with the specified number of data
         */
        protected int dataCount = 0;
        /** 
         * Number of data lines in job.
         * This is the expected number not actual number 
         * */
        protected int dataLines = -1;
        
        /** 
         * Number of output lines in job
         * This is the expected number not actual number 
         * */
        protected int outputLines = -1;
        
        protected Bit traceSwitch = null;
        
        /** 
         * Check length for validating actual number of instruction
         * in the other hand, it's expected number of instructions 
         * */
        protected int length = -1;
        /**
         * This is the actual number of instruction. It's used for
         * validating program.
         */
        protected int actualLength = 0;
        /** Hold the last instruction word */
        protected Word instruction;
        
        /** Hold the start address which will be used as PC */
        protected Word startAddress;
        
        protected int errorCode = -1;
        
        public Partition getPartition(){ return partition; }
        public int getDataLines(){ return dataLines; }
        public int getOutputLines(){ return outputLines; }
        public Bit getTraceSwitch(){ return traceSwitch; }
        public int getLength(){ return length; }
        public int getOccupiedSpace(){ 
            return length+dataLines+outputLines;
        }
        public Word getStartAddress(){ return startAddress; }
        
        public void visit(ModulePart modulePart){
            this.lastPart = modulePart;
            this.visitedParts[modulePart.ordinal()] = true;
        }
        public boolean isVisited(ModulePart modulePart){
            return this.visitedParts[modulePart.ordinal()];
        }
        public boolean isVisitedSome(){
            for(boolean visitedPart: visitedParts){
                if(visitedPart) return true;
            }
            return false;
        }
        public void setErrorCode(int errorCode){
            if(errorCode!=-1) return;
            this.errorCode = errorCode;
        }
    }
    
    /**
     * This class is used as a signal for terminating the batch 
     * when there's no more to read.
     * 
     * @author hussachai
     *
     */
    public class EndOfBatchException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
    
    public static class MemoryNotAvailableException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
    
}
