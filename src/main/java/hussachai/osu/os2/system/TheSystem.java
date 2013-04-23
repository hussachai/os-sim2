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
import hussachai.osu.os2.system.loader.Loader.EndOfBatchException;
import hussachai.osu.os2.system.loader.Loader.MemoryNotAvailableException;
import hussachai.osu.os2.system.loader.LoaderContext;
import hussachai.osu.os2.system.scheduler.Scheduler;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.ID;
import hussachai.osu.os2.system.util.TraceFormatter;

import java.io.File;

/**
 * The system is the main entry of the simulation
 * @author hussachai
 *
 */
public class TheSystem {
    
    private Loader loader = new Loader();
    
    private CPU cpu = new CPU();
    
    private Memory memory = new Memory();
    
    private Scheduler scheduler = new Scheduler();
    
    private IOManager io = new IOManager();
    
    private ErrorHandler errorHandler = new ErrorHandler();
    
    private SystemEvent event = new SystemEvent();
    
    public TheSystem(){}
    
    public void start(String fileName){
        
        loader.init(this);
        cpu.init(this);
        memory.init(this);
        scheduler.init(this);
        errorHandler.init(this);
        event.init(this);
        
        File file = new File(fileName);
        if(!file.exists()){
            throw new SystemException(Errors.IO_FILE_NOT_FOUND);
        }
        
        io.getLog().clearInfo();
        
        try{
            while(true){
                
                while(true){
                    
                    LoaderContext context = new LoaderContext();
                    
                    try{
                        
                        loader.loader(file, context);
                        
                        scheduler.initiate(context);
                        
                        writeTraceHeader(context.getJobID(), 
                                context.getTraceSwitch());
                        
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
                        writeTraceHeader(context.getJobID(), 
                                context.getTraceSwitch());
                        
                        errorHandler.errorHandler(e);
                    }
                }
                
                scheduler.controlTraffic();
            }
            
        }catch(EndOfBatchException e){
            
            event.onSystemShutdown();
            
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
    
    public SystemEvent getEvent(){ return event; }
    
    /**
     * 
     * @param jobID
     * @param traceSwitch
     */
    private void writeTraceHeader(ID jobID, Bit traceSwitch){
        if(traceSwitch==Bit.I){
            io.getLog().clearTrace(jobID);
            /* write trace header */
            io.getLog().trace(jobID, "   Trace data in hex format");
            io.getLog().trace(jobID, TraceFormatter.getTraceHeader());
        }
    }
    
    /** entry point **/
    public static void main(String[] args) {
        
        if(args.length==0){
            System.out.println("java basic-os-sim2.jar filename [-Devent.$eventName] ...");
            System.out.println("Available event environments");
            System.out.println("-D"+Environment.EVENT_ALL);
            System.out.println("-D"+Environment.EVENT_JOB_INIT);
            System.out.println("-D"+Environment.EVENT_JOB_TERM);
            System.out.println("-D"+Environment.EVENT_LOAD_FAIL);
            System.out.println("-D"+Environment.EVENT_CTX_SWITCH);
            System.out.println("-D"+Environment.EVENT_IO_REQ);
            System.out.println("-D"+Environment.EVENT_MALLOC);
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
