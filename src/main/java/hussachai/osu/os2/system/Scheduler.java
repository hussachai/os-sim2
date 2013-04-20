package hussachai.osu.os2.system;

import hussachai.osu.os2.system.cpu.CPU;
import hussachai.osu.os2.system.cpu.CPUSnapshot;
import hussachai.osu.os2.system.error.LogicException;
import hussachai.osu.os2.system.io.IOManager;
import hussachai.osu.os2.system.io.InterruptException;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.unit.ID;

import java.util.Comparator;
import java.util.PriorityQueue;


/**
 * Scheduler is responsible for dispatching job, maintaining ready/block queue.
 * It's also responsible for PCB initiation and termination. Whenever the PCB is 
 * created by Scheduler, it will assign the incremented job ID to the new job 
 * including other attributes such as PC, IR, base and bound, time enter.
 * 
 * @author hussachai
 *
 */
public class Scheduler {
    
    /** Initial quantum value */
    public static final int INIT_QUANTUM = 35;
    
    /** Task time limit for detecting infinite loop. */
    public static final int TIME_LIMIT = 500;
    
    /** Current running process. This value must not be null when
     *  there is a job running in CPU.
     */
    private PCB running = null;
    
    /**
     * Comparator for ready queue. It orders the ready queue based on
     * remaining of quantum descending.
     */
    private Comparator<PCB> readyQueueComparator = new Comparator<PCB>() {
        @Override
        public int compare(PCB o1, PCB o2) {
            return new Integer(o2.remainingQuantum)
                .compareTo(o1.remainingQuantum);
        }
    };
    /**
     * The ready queue is used for queuing the processes waiting for
     * CPU time. The LinkedList implements both Deque and List interface,
     * that allow the Scheduler to use it as queue or organize the order
     * of queue by random access interface being provided by List.
     * The position of PCB is based on the value of remainder of quantum
     * if it does apply. For the 0 remainder of quantum job will be inserted
     * to the end of the queue (number 7th) 
     */
    private PriorityQueue<PCB> readyQueue = new PriorityQueue<PCB>(7, readyQueueComparator);
    
    /**
     * Comparator for blocked queue. It orders the blocked queue based on
     * remaining of I/O time ascending.
     */
    private Comparator<PCB> blockedQueueComparator = new Comparator<PCB>() {
        @Override
        public int compare(PCB o1, PCB o2) {
            return new Integer(o1.remainingIOTime)
                .compareTo(o2.remainingIOTime);
        }
    };
    
    /**
     * The blocked queue is used for queuing the processes waiting for I/O completion.
     * It's organized in ascending I/O completion time order.
     */
    private PriorityQueue<PCB> blockedQueue = new PriorityQueue<PCB>(7, blockedQueueComparator);
    
    private Loader loader;
    
    private CPU cpu;
    
    private Memory memory;
    
    private IOManager io;
    
    public Scheduler(TheSystem system){
        this.loader = system.getLoader();
        this.cpu = system.getCPU();
        this.memory = system.getMemory();
        this.io = system.getIO();
    }
    
    public PCB getRunning(){ return running; }
    
    public void initiate(Loader.Context context, ID jobID){
        PCB pcb = new PCB();
        pcb.jobID = jobID;
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
        
    }
    
    /**
     * Terminate job
     * @param pcb
     */
    public void terminate(){
        
        if(running==null){
            throw new LogicException("No running job to be terminated");
        }
        memory.deallocate(running.getPartition());
        this.running = null;
        
    }
    
    public void controlTraffic(){
        
        
        if(readyQueue.isEmpty()){
            
            
        }else{
            
            PCB pcb = readyQueue.poll();
            /* Begin context switching */
            pcb.remainingQuantum = INIT_QUANTUM;
            this.running = pcb;
            /* End context switching */
            
        }
        
        CPUSnapshot snapshot = null;
        try{
            
            if(this.running!=null){
                while(true){
                    running.cpuUsageTime++;
                    running.remainingQuantum--;
                    operateIO();
                    boolean hasMore = cpu.cpu(running.pc, running.traceSwitch);
                    snapshot = cpu.createSnapshot();
                    running.pc = snapshot.getPC();
                    running.ir = snapshot.getIR();
                    if(!hasMore){
                        terminate();
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
            }else{
                cpu.idle();
                snapshot = cpu.createSnapshot();
                operateIO();
            }
            
        }catch(InterruptException e){
            
            snapshot = cpu.createSnapshot();
            e.getIOType();
            e.getTargetRegister();
        }
        
        
        
    }
    
    private void operateIO(){
        if(blockedQueue.isEmpty()) return;
        PCB pcb = blockedQueue.peek();
        pcb.remainingIOTime--;
        if(pcb.remainingIOTime==0){
            //TODO: I/O job
            readyQueue.add(blockedQueue.poll());
        }
    }
}
