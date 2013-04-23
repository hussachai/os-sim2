package hussachai.osu.os2.system.io;

import hussachai.osu.os2.system.io.IOHandlers.StandardInputHandler;
import hussachai.osu.os2.system.io.IOHandlers.StandardOutputHandler;
import hussachai.osu.os2.system.unit.Word;

/**
 * I/O object registry
 * 
 * We can imagine it as a data bus wiring from the caller device
 * to the destination I/O object.
 * 
 * @author hussachai
 *
 */
public class IOManager {
    
    /** I/O time **/
    public static final int TIME_IO = 10;
    
    private SystemLog log;
    
    private StandardInputHandler stdInputHandler;
    
    private StandardOutputHandler stdOutputHandler;
    
    public IOManager(){
        log = new SystemLog();
        stdInputHandler = new StandardInputHandler();
        stdOutputHandler = new StandardOutputHandler();
    }
    
    public SystemLog getLog(){
        return log;
    }
    
    /**
     * 
     * @param input if null, standard input will be used
     * @return
     */
    public Word read(IOHandlers.Input input){
        if(input==null){
            return stdInputHandler.read();
        }
        return input.read();
    }
    
    /**
     * 
     * @param output if null, standard output will be used
     * @param data
     */
    public void write(IOHandlers.Output output, Word data){
        if(output==null){
            stdOutputHandler.write(data);
        }else{
            output.write(data);
        }
    }
    
    /**
     * I/O type enumeration
     * @author hussachai
     *
     */
    public static enum IOType {
        Read, Write
    }
    
}
