package hussachai.osu.os2.system;

import hussachai.osu.os2.system.io.IOHandlers;
import hussachai.osu.os2.system.io.IOManager.IOType;
import hussachai.osu.os2.system.storage.Memory.Partition;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.ID;
import hussachai.osu.os2.system.unit.Word;

/**
 * Process Control Block
 * 
 * @author hussachai
 *
 */
public class PCB implements Comparable<PCB>{
    
    /** Unique job id in the system at a time */
    protected ID jobID = null;
    
    protected Bit traceSwitch = null;
    protected Word pc;
    /** Snapshot of CPU registers */
    protected Word registers[] = null;
    /** Time that the job entered the system */
    protected int createdTime = 0;
    /** 
     * CPU time used by the job. 
     * */
    protected int cpuUsageTime = 0;
    /**
     * I/O time used by the job
     */
    protected int ioUsageTime = 0;
    /** 
     * Remainder of the last quantum 
     * The Scheduler will give 35 times and it will be decremented 
     * by CPU time until the value is 0 or I/O interrupt occurs 
     * whichever comes first.
     * */
    protected int remainingQuantum = 0;
    /**
     * Time that has already spent by the I/O
     * This value is counted down from 10 to 0
     * 10 is virtual I/O completion time. 
     * */
    protected int remainingIOTime = 0;
    
    /* ========================================= */
    protected int length = 0;
    protected int dataLines = 0;
    protected int outputLines = 0;
    
    /** 
     * Partition is the logical unit that is managed by Memory Manager.
     * Base and bound address can be obtained from partition including
     * the partition size and available status
     */
    protected Partition partition;
    
    
    protected IOType ioType;
    
    /**
     * The input handler. In  phase 2, this input handler is
     * operated by in-memory. It needs pendingInputData to store
     * temporary data before writing it back to the register when
     * the Job is dispatched (running state).
     * If this value is null, the StandardInputHandler will be active
     * which is the same as phase 1, the keyboard.
     */
    protected IOHandlers.Input inputHandler;
    /** 
     * This attribute will be set when Input handler read data 
     * from memory. The data will stay here until the job is dispatched,
     * then data will be transfered to according register depending
     * on the pointer of targetRegister which can be R4 or R5.
     */
    protected Word pendingInputData;
    protected IOHandlers.Output outputHandler;
    /**
     * If the I/O is reader, this value will be CPU register reference
     * If the I/O is writer, this value will be CPU register copy
     */
    protected Word targetRegister;
    /** 
     * Index for memory reader. It will be incremented by 1
     * every time I/O reads until it equals to dataLines-1
     */
    protected int readerIndex = 0;
    /** 
     * Index for memory writer. It will be incremented by 1
     * every time I/O writes until it equals to outputLines-1
     */
    protected int writerIndex = 0;
    
    
    public ID getJobID(){ return jobID; }
    public Word[] getRegisters(){ return registers; }
    public int getCreatedTime(){ return createdTime; }
    public int getCpuUsageTime(){ return cpuUsageTime; }
    public int getIoUsageTime(){ return ioUsageTime; }
    public int getRemainingQuantum(){ return remainingQuantum; }
    public Partition getPartition(){ return partition; }
    
    /** The occupied space in the allocated partition. */
    public int getOccupiedSpace(){ 
        return length+dataLines+outputLines; 
    } 
    
    /**
     * The fragmentation will be the result of
     * fragmentation = partition.getSize() - occupied space 
     * @return
     */
    public int getFragmentation(){
        return partition.getSize()-getOccupiedSpace();
    }
    
    /**
     * This method is used for ordering the PCB in blocked queue
     * based on natural order of the remaining I/O time.
     */
    @Override
    public int compareTo(PCB o) {
        return new Integer(remainingIOTime)
            .compareTo(o.remainingIOTime);
    }
    
    
}
