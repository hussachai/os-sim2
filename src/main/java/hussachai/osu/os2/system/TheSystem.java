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

import hussachai.osu.os2.system.Loader.Context;
import hussachai.osu.os2.system.Loader.MemoryNotAvailableException;
import hussachai.osu.os2.system.cpu.CPU;
import hussachai.osu.os2.system.error.ErrorHandler;
import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.io.IOManager;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.ID;
import hussachai.osu.os2.system.unit.Word;
import hussachai.osu.os2.system.util.TraceFormatter;

import java.io.File;
import java.math.BigInteger;

/**
 * The system is the main entry of the simulation
 * @author hussachai
 *
 */
public class TheSystem {
    
    private ID jobIDGenerator;
    
    private CPU cpu;
    
    private Memory memory;
    
    private Loader loader;
    
    private Scheduler scheduler;
    
    private IOManager io;
    
    private ErrorHandler errorHandler;
    
    public TheSystem(){
        jobIDGenerator = new ID();
        io = new IOManager();
        memory = new Memory(this);
        cpu = new CPU(this);
        loader = new Loader(this);
        scheduler = new Scheduler(this);
        errorHandler = new ErrorHandler(this);
    }
    
    public void start(String fileName){
        
        File file = new File(fileName);
        if(!file.exists()){
            throw new SystemException(Errors.IO_FILE_NOT_FOUND);
        }
        
        io.getLog().clearInfo();
        
        try{
            
            while(true){
                ID jobID = null;
                Context context = new Context();
                try{
                    
                    loader.loader(file, context);
                    
                    jobID = jobIDGenerator.nextSequence();
                    
                    scheduler.initiate(context, jobID);
                    
                    break;
                }catch(MemoryNotAvailableException e){
                    break;
                }catch(Exception e){
                    jobID = jobIDGenerator.nextSequence();
                    //TODO: print error
                }
            }
            
            scheduler.controlTraffic();
            
            
//            io.getLog().info("Cumulative Job ID: "+jobID+" (hex)");
//            
//            /* assign the last instruction word as start address word*/ 
//            Word pc = context.getStartAddress();
//            
//            if(context.getTraceSwitch()==Bit.I){
//                io.getLog().clearTrace();
//                /* write trace header */
//                io.getLog().trace("   Trace data in hex format");
//                io.getLog().trace(TraceFormatter.getTraceHeader());
//            }
            
        }catch(Loader.EndOfBatchException e){
            //TODO: print statistic report here
        }finally{
            
//            String clockHex = new BigInteger(String.valueOf(
//                    cpu.getClock()), 10).toString(16);
//            int inputTime = cpu.getInputTime();
//            int outputTime = cpu.getOutputTime();
//            io.getLog().info("Clock value: "+clockHex+" (hex)");
//            io.getLog().info("Input time: "+inputTime+" (decimal)");
//            io.getLog().info("Output time: "+outputTime+" (decimal)");
        }
    }
    
    public CPU getCPU() { return cpu; }
    
    public Memory getMemory() { return memory; }

    public Loader getLoader() { return loader; }
    
    public IOManager getIO() { return io; }
    
    public Scheduler getScheduler(){ return scheduler; }
    
    public ErrorHandler getErrorHandler() { return errorHandler; }
    
    /** entry point **/
    public static void main(String[] args) {
        
        TheSystem system = new TheSystem();
        IOManager io = system.getIO();
        
        try{
            system.start(args[0]);
            io.getLog().info("Terminated successfully.");
        }catch(SystemException e){
            system.errorHandler.errorHandler(e.getErrorCode());
        }catch(Throwable e){
            system.errorHandler.errorHandler(Errors.SYS_INTERNAL_ERROR);
        }
    }
    
}
