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
import hussachai.osu.os2.system.io.IOManager;

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
    
    private IOManager io;
    
    public void init(TheSystem system){
        
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
    public void errorHandler(Exception e){
        String message = null;
        int errorNumber = Errors.SYS_INTERNAL_ERROR;
        if(e instanceof SystemException){
            errorNumber = ((SystemException)e).getErrorCode();
            message = errors.getProperty(String.valueOf(errorNumber));
        }else{
            message = e.getMessage();
        }
        
        io.getLog().info("[Terminated with error no. "+errorNumber+"]");
        io.getLog().info("Description: "+message);
        io.getLog().info("\n");
    }
    
}
