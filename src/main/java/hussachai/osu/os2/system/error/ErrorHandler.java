/*
 * Name: Hussachai Puripunpinyo
 * Course No.:  CS 5323
 * Assignment title: PHASE I (March 5)
 * TA's Name: 
 *  - Alireza Boloorchi
 *  - Sukanya Suwisuthikasem
 * Global variables:
 *  - errors (The hash map of error code and its message)
 *  - io (The reference to InputOutput)
 *  - memory (The reference to Memory)
 *  
 *  Brief Description:
 *  Error handler is the module for translating error by error number.
 *  It also dump memory data to a file when error occurs.
 *  
 */
package hussachai.osu.os2.system.error;


import hussachai.osu.os2.system.TheSystem;
import hussachai.osu.os2.system.io.InputOutput;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.storage.Memory.Signal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Error handler
 * @author hussachai
 *
 */
public class ErrorHandler {
	
	private Properties errors = new Properties();
	
	private InputOutput io;
	private Memory memory;
	
	public ErrorHandler(TheSystem system){
		
		this.io = system.getIO();
		this.memory = system.getMemory();
		
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
		
		memory.memory(Signal.DUMP, 0, null);
		
	}
	
}
