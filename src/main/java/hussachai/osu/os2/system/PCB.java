package hussachai.osu.os2.system;

import hussachai.osu.os2.system.io.IOType;
import hussachai.osu.os2.system.storage.Memory.Block;
import hussachai.osu.os2.system.unit.Word;

/**
 * Process Control Block
 * 
 * @author hussachai
 *
 */
public class PCB implements Comparable<PCB>{
    
    /** Unique job id in the system at a time */ 
    protected Word jobId = new Word();
    /** Snapshot of current PC */
    protected Word pc = new Word();
    /** Snapshot of current IR */
    protected Word ir = new Word();
    /** Time that the job entered the system */
    protected int createdTime;
    /** CPU time used by the job */
    protected int cpuUsageTime;
    /**
     * Time that has already spent by the I/O
     * This value is counted down from 10 to 0
     * 10 is virtual I/O completion time. 
     * */
    protected int ioUsageTime;
    /** Remainder of the last quantum */
    protected int remainingQuantum;
    
    /* extra attributes in addition to the specification
     * because the specification says that PCB must contain AT LEAST 
     * above information. so, PCB can contain more information than 
     * it's specified in specification.   
     * */
    /* ========================================= */
    /** The occupied space in the allocated block.
     * This fragmentation will be the result of
     * occupied is subtracted from block.getSize() 
     */
    protected int occupied;
    
    /** Block is the logical unit that is managed by Memory Manager.
     *  Base and bound address can be obtained from block including
     *  the block size and available status
     */
    protected Block block;
    
    protected IOType ioType;
    
    public Word getJobId(){ return jobId; }
    public Word getPc(){ return pc; }
    public Word getIr(){ return ir; }
    public Block getBlock(){ return block; }
    public int getCreatedTime(){ return createdTime; }
    public int getCpuUsageTime(){ return cpuUsageTime; }
    public int getIoUsageTime(){ return ioUsageTime; }
    public int getRemainingQuantum(){ return remainingQuantum; }
    
    @Override
    public int compareTo(PCB o) {
        return new Integer(ioUsageTime)
            .compareTo(o.ioUsageTime);
    }
    
    
}
