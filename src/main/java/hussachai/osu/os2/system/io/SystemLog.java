package hussachai.osu.os2.system.io;

import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.unit.ID;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * The logical device for handling system log
 * There are 2 different logs - info and trace which
 * will be redirect to system.log and trace.log respectively
 * 
 * @author hussachai
 */
public class SystemLog {
    
    public static final String SYSTEM_LOG = "system.log";
    
    public static final String TRACE_LOG = "trace-{0}.log";
    
    /** platform independent new line characters */
    private String newLine = System.getProperty("line.separator");
    
    /**
     * Empty the system log
     */
    public void clearInfo(){
        write(SYSTEM_LOG, null, false);
    }
    
    /**
     * 
     * @param data
     */
    public void info(String data){
        write(SYSTEM_LOG, data, true);
    }
    
    /**
     * Empty the trace file
     * @param jobID
     */
    public void clearTrace(ID jobID){
        if(jobID==null) return;
        String fileName = MessageFormat.format(
                TRACE_LOG, jobID.toString());
        write(fileName, null, false);
    }
    
    /**
     * Trace file will be created for each jobID and jobID
     * will be the part of file name
     * @param jobID if this value is null, trace will not be output
     * @param data
     */
    public void trace(ID jobID, String data){
        if(jobID==null) return;
        String fileName = MessageFormat.format(
                TRACE_LOG, jobID.toString());
        write(fileName, data, true);
    }
    
    /**
     * 
     * @param fileName
     * @param data
     * @param append
     */
    private void write(String fileName, String data, boolean append){
        FileWriter writer = null;
        try{
            File logFile = new File(fileName);
            if(!logFile.exists()){
                logFile.createNewFile();
            }
            writer = new FileWriter(logFile, append);
            if(data!=null){
                writer.write(data+newLine);
            }
            writer.flush();
        }catch(IOException e){
            throw new SystemException(Errors.IO_WRITE_ERROR);
        }finally{
            if(writer!=null){
                try{ writer.close(); }catch(Exception e){}
            }
        }
    }
}

