package hussachai.osu.os2.system.error;


import hussachai.osu.os2.system.TheSystem;
import hussachai.osu.os2.system.io.InputOutput;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 
 * @author hussachai
 *
 */
public class ErrorHandler {
	
	private Properties errors = new Properties();
	
	private InputOutput io;
	
	public ErrorHandler(TheSystem system){
		
		this.io = system.getIO();
		
		InputStream in = null;
		try{
			in = ErrorHandler.class.getResourceAsStream("/errors.properties");
			if(in==null) throw new FileNotFoundException();
			errors.load(in);
		}catch(IOException e){
			throw new LogicException("Unable to load errors.properties file from classpath");
		}finally{
			if(in!=null){
				try{ in.close(); }catch(Exception e){}
			}
		}
	}
	
	/**
	 * Specification required method.
	 * @param errorNumber
	 */
	public void errorHandler(int errorNumber){
		String message = errors.getProperty(String.valueOf(errorNumber));
		io.getLog().info("Terminated with error no. "+errorNumber);
		io.getLog().info("Description: "+message);
	}
	
}
