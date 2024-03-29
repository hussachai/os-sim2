/*
 * Name: Hussachai Puripunpinyo
 * Course No.:  CS 5323
 * Assignment title: PHASE II (April 23)
 * TA's Name: 
 *  - Alireza Boloorchi
 *  - Sukanya Suwisuthikasem
 * Global variables:
 *  - running (The PCB of job being executed in CPU)
 *  - readyQueue (The ready queue ordering by remaining quantum desc) 
 *  - blockedQueue (The blocked queue ordering by remaining I/O time asc)
 *  - cpu (The reference to CPU)
 *  - memory (The reference to Memory)
 *  - io (The reference to IOManager)
 *  - event (The reference to SystemEvent)
 *  - errorHandler (The reference to ErrorHandler)
 *  
 *  Brief Description:
 *  Scheduler is responsible for dispatching job, maintaining ready/block queue.
 *  It's also responsible for PCB initiation and termination. Whenever the PCB is 
 *  created by Scheduler, it will assign the incremented job ID to the new job 
 *  including other attributes such as PC, IR, base and bound, time enter.
 *  
 */
package hussachai.osu.os2.system.scheduler;

import hussachai.osu.os2.system.SystemEvent;
import hussachai.osu.os2.system.TheSystem;
import hussachai.osu.os2.system.cpu.CPU;
import hussachai.osu.os2.system.error.ErrorHandler;
import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.LogicException;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.io.IOHandlers.MemoryInputHandler;
import hussachai.osu.os2.system.io.IOHandlers.MemoryOutputHandler;
import hussachai.osu.os2.system.io.IOManager;
import hussachai.osu.os2.system.io.IOManager.IOType;
import hussachai.osu.os2.system.io.InterruptException;
import hussachai.osu.os2.system.loader.LoaderContext;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.unit.Word;

import java.util.Comparator;
import java.util.PriorityQueue;


/**
 * Scheduler
 * 
 * @author hussachai
 *
 */
public class Scheduler {
    
    /** Initial quantum value */
    public static final int INIT_QUANTUM = 35;
    
    /** Task time limit for detecting infinite loop. */
    public static final int TIME_LIMIT = 1000;
    
    /** Current running process. This value must not be null when
     *  there is a job running in CPU.
     */
    private PCB running = null;
    
    
    /**
     * The ready queue is used for queuing the processes waiting for
     * CPU time.
     * The position of PCB is based on the value of remainder of quantum
     * if it does apply. For the 0 remainder of quantum job will be inserted
     * to the end of the queue (number 7th) 
     */
    private PriorityQueue<PCB> readyQueue;
    
    /**
     * The blocked queue is used for queuing the processes waiting for I/O completion.
     * It's organized in ascending I/O completion time order.
     */
    private PriorityQueue<PCB> blockedQueue;
    
    private CPU cpu;
    
    private Memory memory;
    
    private IOManager io;
    
    private SystemEvent event;
    
    private ErrorHandler errorHandler;
    
    /**
     * 
     * @param system
     */
    public void init(TheSystem system){
        
        this.cpu = system.getCPU();
        this.memory = system.getMemory();
        this.io = system.getIO();
        this.errorHandler = system.getErrorHandler();
        this.event = system.getEvent();
        
        /*
         * Comparator for ready queue. It orders the ready queue based on
         * remaining of quantum descending.
         */
        Comparator<PCB> readyQueueComparator = new Comparator<PCB>() {
            @Override
            public int compare(PCB o1, PCB o2) {
                return new Integer(o2.remainingQuantum)
                    .compareTo(o1.remainingQuantum);
            }
        };
        readyQueue = new PriorityQueue<PCB>(
                Memory.MAX_PARTITION_SIZE, readyQueueComparator){
                    private static final long serialVersionUID = 1L;
                    @Override
                    public boolean add(PCB e) {
                        if(size()==Memory.MAX_PARTITION_SIZE){
                            throw new LogicException(
                                "Number of jobs exceeds the ready queue capacity");
                        }
                        return super.add(e);
                    }
            };
        
        /*
         * Comparator for blocked queue. It orders the blocked queue based on
         * remaining of I/O time ascending.
         */
        Comparator<PCB> blockedQueueComparator = new Comparator<PCB>() {
            @Override
            public int compare(PCB o1, PCB o2) {
                return new Integer(o1.remainingIOTime)
                    .compareTo(o2.remainingIOTime);
            }
        };
        blockedQueue = new PriorityQueue<PCB>(
                Memory.MAX_PARTITION_SIZE, blockedQueueComparator){
                private static final long serialVersionUID = 1L;
                @Override
                public boolean add(PCB e) {
                    if(size()==Memory.MAX_PARTITION_SIZE){
                        throw new LogicException(
                            "Number of jobs exceeds the blocked queue capacity");
                    }
                    return super.add(e);
                }
            };
    }
    
    /**
     * 
     * @return PCB of current job being executed
     */
    public PCB getRunning(){ 
        return running; 
    }
    
    /**
     * 
     * @return # of jobs in ready queue
     */
    public int getNumberOfJobsInReadyQueue(){
        return readyQueue.size();
    }
    
    /**
     * 
     * @return # of jobs in blocked queue
     */
    public int getNumberOfJobsInBlockedQueue(){
        return blockedQueue.size();
    }
    
    /**
     * 
     * @param context
     * @param jobID
     */
    public void initiate(LoaderContext context){
        
        PCB pcb = new PCB();
        pcb.jobID = context.getJobID();
        pcb.traceSwitch = context.getTraceSwitch();
        pcb.pc = context.getStartAddress();
        pcb.createdTime = cpu.getClock();
        pcb.remainingQuantum = Scheduler.INIT_QUANTUM;
        pcb.remainingIOTime = 0;
        pcb.length = context.getLength();
        pcb.dataLines = context.getDataLines();
        pcb.outputLines = context.getOutputLines();
        pcb.partition = context.getPartition();
        
        readyQueue.add(pcb);
        
        event.onJobInitiated(pcb);
        
    }
    
    
    /**
     * Terminate job
     * @param pcb
     */
    public void terminate(Exception error){
        
        if(running==null){
            throw new LogicException("No running job to be terminated");
        }
        
        event.onJobTerminated(error);
        
        if(error!=null){
            errorHandler.errorHandler(error);
        }else{
            io.getLog().info("[Terminated normally]");
            io.getLog().info("\n");
        }
        
        memory.deallocate(running.getPartition());
        this.running = null;
    }
    
    /**
     * Check whether there's a job in the system or not.
     * 
     * @return
     */
    public boolean isFinished(){
        
        return readyQueue.isEmpty() && 
                blockedQueue.isEmpty() && running==null;
    }
    
    /**
     * 
     */
    public void controlTraffic(){
        
        if(readyQueue.isEmpty()){
            
            if(blockedQueue.isEmpty()){
                if(running==null){
                    return;//nothing left
                }else{
                    throw new LogicException("Scheduler failed!");
                }
            }else{
                while(true){
                    cpu.idle();
                    if(operateIO()){
                        /* now we have job in ready queue */
                        break;
                    }
                }
            }
        }
        
        PCB pcb = readyQueue.poll();
        
        /* Begin context switching ***********/
        
        this.running = pcb;
        
        event.onContextSwitched();
        
        pcb.remainingQuantum = INIT_QUANTUM;//reset quantum
        
        cpu.restoreCPU(pcb.registers);
        
        if(pcb.pendingInputData!=null){
            Word.copy(pcb.pendingInputData, pcb.targetRegister);
            pcb.pendingInputData = null;
        }
        
        /* End context switching *************/
        
        if(this.running!=null){
            try{
                while(true){
                    running.cpuUsageTime++;
                    if(running.cpuUsageTime>=TIME_LIMIT){
                        throw new SystemException(Errors.CPU_SUSPECTED_INFINITE_JOB);
                    }
                    running.remainingQuantum--;
                    operateIO();
                    boolean hasMore = cpu.cpu(running.pc, running.traceSwitch);
                    /* snapshot must be done after calling CPU routine */
                    snapshotCPU();
                    if(!hasMore){
                        terminate(null);
                        /* go out to system for loading new job 
                         * if there exists */
                        return; 
                    }
                    if(running.remainingQuantum==0){
                        /* When remaining quantum = 0 and there's still more 
                         * instructions to be executed. Take the running
                         * job to end of the ready queue.
                         */
                        readyQueue.add(running);
                        running = null;
                        break;
                    }
                }
            }catch(InterruptException e){
                try{
                    handleInterrupt(e);
                }catch(Exception e2){
                    terminate(e2);
                }
            }catch(Exception e){
                /* terminate abnormally */
                terminate(e);
            }
        }else{
            throw new LogicException("Scheduler failed!");
        }
        
    }
    
    /**
     * 
     * @param interrupt
     */
    private void handleInterrupt(InterruptException interrupt){
        
        snapshotCPU();
        
        running.remainingIOTime = IOManager.TIME_IO;
        running.ioType = interrupt.getIOType();
        running.targetRegister = interrupt.getTargetRegister();
        
        if(interrupt.getIOType()==IOType.Read){
            
            MemoryInputHandler inputHandler = new MemoryInputHandler();
            inputHandler.setMemory(memory);
            inputHandler.setPartition(running.getPartition());
            
            if(running.readerIndex>=running.dataLines){
                throw new SystemException(Errors.PROG_MISSING_DATA);
            }
            int memoryIndex = running.length+running.readerIndex;
            inputHandler.setMemoryIndex(memoryIndex);
            running.readerIndex++;
            running.inputHandler = inputHandler;
        }else{
            
            MemoryOutputHandler outputHandler = new MemoryOutputHandler();
            outputHandler.setMemory(memory);
            outputHandler.setPartition(running.getPartition());
            
            if(running.writerIndex>=running.outputLines){
                throw new SystemException(Errors.IO_INSUFFICIENT_OUTPUT_SPACE);
            }
            int memoryIndex = running.length+running.dataLines+
                    running.writerIndex;
            outputHandler.setMemoryIndex(memoryIndex);
            running.writerIndex++;
            running.outputHandler = outputHandler;
        }
        
        blockedQueue.add(running);
        
        event.onIORequested(interrupt);
        
        running = null;
    }
    
    /**
     * Copy current CPU registers to the PCB
     */
    private void snapshotCPU(){
        
        if(running==null){
            throw new LogicException("CPU Snapshot without active PCB");
        }
        running.registers = cpu.createSnapshot();
        running.pc = running.registers[CPU.R_PC];
    }
    
    /**
     * 
     * @return true if the Job is transfered from blocked queue
     * to ready queue. otherwise, false.
     */
    private boolean operateIO(){
        
        if(blockedQueue.isEmpty()) return false;
        
        PCB pcb = blockedQueue.peek();
        pcb.remainingIOTime--;
        pcb.ioUsageTime++;
        
        if(pcb.remainingIOTime==0){
            
            if(pcb.ioType==IOType.Read){
                Word data = io.read(pcb.inputHandler);
                /* add data to pending write back */
                pcb.pendingInputData = data;
            }else{
                io.write(pcb.outputHandler, pcb.targetRegister);
//                io.write(null, pcb.targetRegister);
            }
            
            readyQueue.add(blockedQueue.poll());
            return true;
        }
        return false;
    }
    
}

