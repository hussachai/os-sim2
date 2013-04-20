package hussachai.osu.os2.system.io;

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
    
    private SystemLog log = new SystemLog();
    
    private StdOutput output;
    
    private StdInput input;
    
    public SystemLog getLog(){
        return log;
    }
    
    public void write(String data) {
        output.write(data);
    }
    
    public String read() {
        return input.read();
    }
    
    public static enum IOType {
        Reader, Writer
    }
    
    public static interface StdInput {
        public String read();
    }
    
    public static interface StdOutput {
        public void write(String data);
    }
    
    
    
}
