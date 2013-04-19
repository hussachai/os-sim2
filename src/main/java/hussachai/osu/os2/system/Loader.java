package hussachai.osu.os2.system;
import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.storage.Buffer;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Loader {
    
    private Buffer buffer;
    
    /* Keep tracking last position file has been read */
    private long lastFilePointer = 0;
    
    public Loader(TheSystem system){
        this.buffer = new Buffer(128);
    }
    
    public Context loader(File file){
        
        Context context = new Context();
        RandomAccessFile dataInput = null;
        try{
            
            dataInput = new RandomAccessFile(file, "r");
            
            /* resume reading from the last point */
            dataInput.seek(this.lastFilePointer);
            boolean done = false;
            String data = null;
            while( (data = dataInput.readLine()) !=null){
                /*guarantee that no space surrounding data*/
                data = data.trim();
                
                done = parseLine(context, data);
                
                if(done){
                    this.lastFilePointer = dataInput.getFilePointer();
                    /*validate*/
                    int dataTotal = context.dataList.size();
                    if(context.dataLines<dataTotal){
                        System.out.println("missing data items");//TODO
                    }else if(context.dataLines>dataTotal){
                        System.out.println("extra data unused");//TODO
                    }
                    //TODO: add dataList to the simulatedKeyboard
                    Word jobData[] = buffer.flush();
                    System.out.println(Arrays.toString(jobData));
                    
                }
            };
            
            /* Signal the end of file and no more job*/
            if( (!done) && data==null) return null;
            
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            try{ 
                if(dataInput!=null) dataInput.close(); 
            }catch(Exception e){}
        }
        
        return context;
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
                    if(context.part!=ModulePart.JOB){
                        //TODO: illegalstate
                    }
                    context.dataLines = Word.fromHexString(commands[2]).toDecimal();
                    context.outputLines = Word.fromHexString(commands[3]).toDecimal();
                    context.part = ModulePart.JOB_PAYLOAD;
                }else{
                    //TODO: error
                }
            }else if(commands.length==2){
                //DATA or END command
                if("DATA".equals(commands[1])){
                    if(context.part!=ModulePart.DATA){
                        System.out.println("error expected DATA part");//TODO
                    }
                    if(context.dataLines>0){
                        context.part = ModulePart.DATA_PAYLOAD;
                    }else{
                        context.part = ModulePart.END;
                    }
                }else if("END".equals(commands[1])){
                    if(context.part!=ModulePart.END){
                        System.out.println("error expected END part");//TODO
//                        throw new SystemException(errorCode)
                    }
                    return true;
                }else{
                    System.out.println("invalid "+commands[1]);//TODO
                }
            }else{
                System.out.println("invalid commands expected 2 or 4 parts");//TODO
            }
        }else{
            if(data.length()==3){
                if(context.length==-1 && context.part==ModulePart.JOB_PAYLOAD){
                    /* If the data length is 3 and length word hasn't been read
                     * and part is DATA_PAYLOAD, it is probably the length check data */
                    context.length = Word.fromHexString(data).toDecimal();
                    return false;
                }else if(context.part==ModulePart.DATA_PAYLOAD){
                    context.dataList.add(Word.fromHexString(data));
                    if(context.dataList.size()==context.dataLines){
                        context.part = ModulePart.END;
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
                context.startAddress = Word.fromHexString(parts[0]);
                if(parts.length>1){
                    context.traceSwitch = Word.fromHexString(parts[1])
                            .toDecimal()>0?Bit.I:Bit.O;
                }
                context.part = ModulePart.DATA;
            }else{
                if(data.length()%3!=0){/* wrong hex format */
                    throw new SystemException(Errors.PROG_INVALID_FORMAT);
                }
                String wordStr = null;
                for(int i=0; i<data.length();i+=3){
                    wordStr = data.substring(i, i+3);
                    context.instruction = Word.fromHexString(wordStr);
                    buffer.add(context.instruction);
                }
            }
        }
        
        return false;
    }
    
    public static enum ModulePart {
        JOB, JOB_PAYLOAD, DATA, DATA_PAYLOAD, END
    }
    
    public static class Context {
        
        /*
         * Expected part that is going to encounter in the next line of reading.
         */
        private ModulePart part = ModulePart.JOB;
        
        /* number of data lines in job
         * this is the expected number not actual number 
         * */
        private int dataLines = 0;
        
        private List<Word> dataList = new ArrayList<Word>();
        
        /* number of output lines in job
         * this is the expected number not actual number 
         * */
        private int outputLines = 0;
        
        private Bit traceSwitch = Bit.O;//off by default
        
        /* check length for validating actual number of instruction
         * in the other hand, it's expected number of instructions 
         * */
        private int length = -1;
        
        /* hold the last instruction word */
        private Word instruction;
        
        private Word startAddress;
        
        public int getDataLines(){ return dataLines; }
        public List<Word> getDataList(){ return dataList; }
        public int getOutputLines(){ return outputLines; }
        public Bit getTraceSwitch(){ return traceSwitch; }
        public int getLength(){ return length; }
        public Word getStartAddress(){ return startAddress; }
        
        void clear(){
            part = ModulePart.JOB;
            dataLines = 0;
            dataList.clear();
            outputLines = 0;
            traceSwitch= Bit.O;
            length = -1;
            instruction = null;
            startAddress = null;
        }
    }
    
    public static void main(String[] args) {
        Loader loader = new Loader(new TheSystem());
//        for(int i=0;i<3;i++){
            Context context = null;
            while((context=loader.loader(new File("programs/tb")))!=null){
                System.out.println("Part:"+context.part);
                System.out.println("DataLines:"+context.dataLines);
                System.out.println("DataList:"+context.dataList);
                System.out.println("OutputLines:"+context.outputLines);
                System.out.println("TraceSwitch:"+context.traceSwitch);
                System.out.println("Length:"+context.length);
                System.out.println("LastInstruction:"+context.instruction);
                System.out.println("StartAddress:"+context.startAddress.toBinString());
            }
//        }
    }
}
