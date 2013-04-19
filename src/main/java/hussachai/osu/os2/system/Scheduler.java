package hussachai.osu.os2.system;

import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.unit.Word;

import java.util.LinkedList;
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
    
    /** Counter is used for generating new Job ID */
    private Word counter = new Word();
    
    /** Current running process. This value must not be null when
     *  there is a job running in CPU.
     */
    private PCB running = null;
    
    /**
     * The ready queue is used for queuing the processes waiting for
     * CPU time. The LinkedList implements both Deque and List interface,
     * that allow the Scheduler to use it as queue or organize the order
     * of queue by random access interface being provided by List.
     * The position of PCB is based on the value of remainder of quantum
     * if it does apply. For the 0 remainder of quantum job will be inserted
     * to the end of the queue (number 7th) 
     */
    private LinkedList<PCB> readyQueue = new LinkedList<PCB>();
    
    /**
     * The blocked queue is used for queuing the processes waiting for I/O completion.
     * It's organized in ascending I/O completion time order.
     */
    private PriorityQueue<PCB> blockedQueue = new PriorityQueue<PCB>();
    
    private Memory memory;
    
    public Scheduler(TheSystem system){
        this.memory = system.getMemory();
    }
    
    public PCB getRunning(){ return running; }
    
    public void initiate(){
        PCB pcb = new PCB();
        Word jobId = new Word();
        counter.increment();
        Word.copy(counter, jobId);
        
    }
    
    /**
     * Terminate job
     * @param pcb
     */
    public void terminate(PCB pcb){
        
        running = null;
        memory.deallocate(pcb.getBlock());
        
    }
    
    
}
