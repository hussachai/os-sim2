/*
 * Name: Hussachai Puripunpinyo
 * Course No.:  CS 5323
 * Assignment title: PHASE I (March 5)
 * TA's Name: 
 *  - Alireza Boloorchi
 *  - Sukanya Suwisuthikasem
 * Global variables:
 *  - cpu (the reference to CPU)
 *  - memory (the reference to Memory)
 *  - loader (the reference to Loader)
 *  - io (the reference to InputOutput)
 *  - errorHandler (the reference to ErrorHandler)
 *  
 *  Brief Description:
 *  The System is the main entry of program and the container for every subroutines. 
 *  It wires everything together via itself because it exposes the components
 *  via the getter method and pass itself to some components that require.
 *  
 *  Remark:
 *  I should not name this class as System because it will collide with 
 *  the java.lang.System class that is imported by default.
 *  
 */
package hussachai.osu.os2.system;

import hussachai.osu.os2.system.cpu.CPU;
import hussachai.osu.os2.system.error.ErrorHandler;
import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.io.IOManager;
import hussachai.osu.os2.system.loader.Loader;
import hussachai.osu.os2.system.loader.Loader.Context;
import hussachai.osu.os2.system.loader.Loader.EndOfBatchException;
import hussachai.osu.os2.system.loader.Loader.MemoryNotAvailableException;
import hussachai.osu.os2.system.misc.SystemStat;
import hussachai.osu.os2.system.scheduler.Scheduler;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.unit.ID;

import java.io.File;

/**
 * The system is the main entry of the simulation
 * @author hussachai
 *
 */
public class TheSystem {
    
    private ID jobIDGenerator = new ID();
    
    private Loader loader = new Loader();
    
    private CPU cpu = new CPU();
    
    private Memory memory = new Memory();
    
    private Scheduler scheduler = new Scheduler();
    
    private IOManager io = new IOManager();
    
    private ErrorHandler errorHandler = new ErrorHandler();
    
    private SystemStat stat = new SystemStat();
    
    public TheSystem(){}
    
    public void start(String fileName){
        
        loader.init(this);
        cpu.init(this);
        memory.init(this);
        scheduler.init(this);
        errorHandler.init(this);
        stat.init(this);
        
        File file = new File(fileName);
        if(!file.exists()){
            throw new SystemException(Errors.IO_FILE_NOT_FOUND);
        }
        
        io.getLog().clearInfo();
        
        try{
            while(true){
                
                while(true){
                    ID jobID = null;
                    Context context = new Context();
                    try{
                        
                        loader.loader(file, context);
                        
                        jobID = jobIDGenerator.nextSequence();
                        
                        scheduler.initiate(context, jobID);
                        
                        break;
                    }catch(MemoryNotAvailableException e){
                        /* do nothing, just skip loading */
                        break;
                    }catch(EndOfBatchException e){
                        if(scheduler.isFinished()){
                            throw e;
                        }
                        break;
                    }catch(Exception e){
                        if(jobID==null){
                            jobID = jobIDGenerator.nextSequence();
                            stat.writeLog("Job ID (hex)", jobID);
                            errorHandler.errorHandler(e);
                        }
                    }
                }
                
                scheduler.controlTraffic();
            }
            
        }catch(EndOfBatchException e){
            
            stat.onSystemShutdown();
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public CPU getCPU() { return cpu; }
    
    public Memory getMemory() { return memory; }

    public Loader getLoader() { return loader; }
    
    public IOManager getIO() { return io; }
    
    public Scheduler getScheduler(){ return scheduler; }
    
    public ErrorHandler getErrorHandler() { return errorHandler; }
    
    public SystemStat getStat(){ return stat; }
    
    /** entry point **/
    public static void main(String[] args) {
        
        if(args.length==0){
            System.out.println("Missing file argument");
            return;
        }
        TheSystem system = new TheSystem();
        try{
            system.start(args[0]);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
