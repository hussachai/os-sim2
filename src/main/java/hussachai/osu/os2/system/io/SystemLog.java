package hussachai.osu.os2.system.io;

import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The logical device for handling system log
 * There are 2 different logs - info and trace which
 * will be redirect to system.log and trace.log respectively
 * 
 * @author hussachai
 */
public class SystemLog {
	
	public static final String SYSTEM_LOG = "system.log";
	
	public static final String TRACE_LOG = "trace.log";
	
	/** platform independent new line characters */
	private String newLine = System.getProperty("line.separator");
	
	public void clearInfo(){
		write(SYSTEM_LOG, null, false);
	}
	
	public void info(String data){
		write(SYSTEM_LOG, data, true);
	}
	
	public void clearTrace(){
		write(TRACE_LOG, null, false);
	}
	
	public void trace(String data){
		write(TRACE_LOG, data, true);
	}
	
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