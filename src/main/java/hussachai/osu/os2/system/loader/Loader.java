/*
 * Name: Hussachai Puripunpinyo
 * Course No.:  CS 5323
 * Assignment title: PHASE II (April 23)
 * TA's Name: 
 *  - Alireza Boloorchi
 *  - Sukanya Suwisuthikasem
 * Global variables:
 *  - jobIDGenerator (The word-size job ID generator)
 *  - buffer (The small memory to hold the data for improving performance) 
 *  - memory (The reference to Memory)
 *  - event (The reference to SystemEvent)
 *  - lastFilePointer (The variable that is used for memorizing the last pointer in file)
 *  Brief Description:
 *  The Loader is responsible for loading new job from batch file. When the job is loaded
 *  successfully, the Loader will call Memory method to allocate the memory for that job.
 *  If the memory allocation is failed, the Loader will skip all operations and return 
 *  the control back to System then Scheduler will continue. 
 *  Sometimes, Loader detects the error after it has already done the memory allocation,
 *  it will deallocate memory and report the error to SystemEvent and the error will be
 *  propagated to the System then the Error Handler will take control after that. 
 *  Loader also performs the validation on syntax and data. 
 *  
 *  Remark:
 *  The loader() cannot have starting address and trace-switch as its arguments
 *  which is different from specification. The reason that it takes file argument
 *  instead of starting address and trace-switch is because the load is responsible
 *  for parsing the user program file that has starting address and trace-switch data
 *  encoded into the same source file as program. The System cannot invoke Loader and
 *  passing those required arguments because it cannot know those values before Loader
 *  has finished parsing the whole program.  
 *  
 */
package hussachai.osu.os2.system.loader;
import hussachai.osu.os2.system.SystemEvent;
import hussachai.osu.os2.system.TheSystem;
import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.loader.LoaderContext.ModulePart;
import hussachai.osu.os2.system.storage.Buffer;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.ID;
import hussachai.osu.os2.system.unit.Word;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Loader
 * @author hussachai
 *
 */
public class Loader {
    
    private ID jobIDGenerator = new ID();
    
    private Buffer buffer;
    
    private Memory memory;
    
    private SystemEvent event;
    
    /* Keep tracking last position file has been read */
    private long lastFilePointer = 0;
    
    public void init(TheSystem system){
        this.buffer = new Buffer(8);
        this.memory = system.getMemory();
        this.event = system.getEvent();
    }
    
    /**
     * [Specification required method]
     * 
     * @param file
     * @return context if the job is already transfered to memory 
     * or null if memory is not available.
     */
    @SuppressWarnings("resource")/* JDK bug: false resource leak warning */
    public void loader(File file, LoaderContext context){
        
        boolean done = false;
        String data = null;
        RandomAccessFile dataInput = null;
        
        context.jobID = jobIDGenerator.nextSequence();
        
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
                    }else if(context.length > context.actualLength){
                        event.writeLog(context.jobID, "Declared length is greater than actual length");
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
            
            if( !(e instanceof MemoryNotAvailableException) &&
                    !(e instanceof EndOfBatchException)){
                event.onLoadFailed(context, e);
            }
            
            memory.deallocate(context.partition);
            
            if(e instanceof RuntimeException) throw (RuntimeException)e;
            throw new SystemException(e);
            
        }finally{
            try{
                if(dataInput!=null) dataInput.close(); 
            }catch(Exception e){}
        }
        
    }
    
    /**
     * Move cursor to the next start point or throw EndOfBatchException
     * if the cursor reaches the end of file (EOF).
     * 
     * @param dataInput
     */
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
    private boolean parseLine(LoaderContext context, String data){
        
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
                    
                    /* After knowing total size of the program, Loader has
                     * to allocate the memory immediately because the buffer is
                     * too small to store all program data. In the latter part,
                     * Loader will store the instruction to the buffer and 
                     * the buffer will be flushed to memory when it's full. 
                     */
                    context.partition = memory.allocate(size);
                    
                    if(context.partition==null){
                        throw new MemoryNotAvailableException();
                    }else{
                        event.onMemoryAllocated(context);
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
    private void flushBuffer(LoaderContext context){
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
     * This class is used as a signal for terminating the batch 
     * when there's no more to read.
     * 
     * @author hussachai
     *
     */
    public class EndOfBatchException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
    
    /**
     * 
     * @author hussachai
     *
     */
    public static class MemoryNotAvailableException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
    
}
