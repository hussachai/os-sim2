/*
 * Name: Hussachai Puripunpinyo
 * Course No.:  CS 5323
 * Assignment title: PHASE II (April 16)
 * TA's Name: 
 *  - Alireza Boloorchi
 *  - Sukanya Suwisuthikasem
 * Global variables:
 *  - addresses (The array of word (12 bits) unit. It has 512 words)
 *  - io (The reference to InputOutput)
 *  
 *  Brief Description:
 *  Memory is used for accessing the data. It's the main storage of the system.
 *  The keeps the instruction and data in the same array. The system that needs
 *  to access memory must access it via the memory method that supports 
 *  3 different operations - READ, WRITE, and DUMP.
 *  
 */
package hussachai.osu.os2.system.storage;

import hussachai.osu.os2.system.PCB;
import hussachai.osu.os2.system.Scheduler;
import hussachai.osu.os2.system.TheSystem;
import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.LogicException;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.io.IOManager;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;

/**
 * Memory manager
 * 
 * @author hussachai
 *
 */
public class Memory {
    
    /**
     * the addresses store 512 words
     */
    public static final int MEMORY_SIZE = 512;
    
    public static final int PARTITION_NUMBERS = 7;
    
    public static final int MAX_PARTITION_SIZE = 128;
    
    private Word addresses[] = new Word[MEMORY_SIZE];
    
    /* Memory manager groups each consecutive addresses into 7 partitions
     * with different size.
     */
    private Partition partitions[] = new Partition[PARTITION_NUMBERS];
    
    private IOManager io;
    
    private Scheduler scheduler;
    
    public void init(TheSystem system){
        
        for(int i=0; i<MEMORY_SIZE;i++){
            addresses[i] = new Word();
        }
        /* initialize partitions */
        partitions[0] = new Partition(0, 0, 31);//32 words
        partitions[1] = new Partition(1, 32, 63);//32 words
        partitions[2] = new Partition(2, 64, 127);//64 words
        partitions[3] = new Partition(3, 128, 191);//64 words
        partitions[4] = new Partition(4, 192, 255);//64 words
        partitions[5] = new Partition(5, 256, 383);//128 words
        partitions[6] = new Partition(6, 384, 511);//128 words
        
        this.io = system.getIO();
        this.scheduler = system.getScheduler();
    }
    
//    public void debug(){
//        for(int i=0;i<MEMORY_SIZE;i++){
//            System.out.println("["+i+"]"+addresses[i]);
//        }
//    }
    /**
     * Try to allocate available partition based on the best fit policy.
     * If the allocation is success, the allocate method will return
     * the partition pointer. Otherwise, null will return.
     * @param size
     * @return
     */
    public Partition allocate(int size){
        if(size>MAX_PARTITION_SIZE){
            throw new SystemException(Errors.PROG_TOO_LONG);
        }
        for(Partition partition: partitions){
            if(partition.getSize()>=size && partition.free){
                partition.free = false;
                return partition;
            }
        }
        return null;
    }
    
    /**
     * Deallocate the memory using partition pointer to indicate
     * the range of the addresses that will be erased and marked as free. 
     * @param partition
     */
    public void deallocate(Partition partition){
        if(partition==null) return;
        partition.free = true;
        /* clear memory */
        for(int i=0;i<partition.getSize();i++){
            memory(partition, Signal.WRIT, i, new Word());
        }
    }
    
    
    /**
     * 
     * [Specification required method]
     * 
     * @param signal
     * @param memoryAddr (EA)
     * @param variable - may be a register used by the CPU or may be a buffer used by the LOADER
     */
    public void memory(Signal signal, Word memoryAddr, Word variable){
        int memoryIdx = Bit.toDecimal(memoryAddr.getBits());
        
        if(scheduler.getRunning()==null){
            throw new LogicException("Accessing memory by non process!!!");
        }
        
        PCB pcb = scheduler.getRunning();
        
        memory(pcb.getPartition(), signal, memoryIdx, variable);
    }
    
    /**
     * This memory function should be accessed directly by Loader. Because when
     * the Loader allocates memory successfully, it will get the Partition back.
     * No process is related to that action; so, Loader must not call this function
     * instead of the overload one.  
     * 
     * @param partition
     * @param signal
     * @param memoryIdx
     * @param variable
     */
    public void memory(Partition partition, Signal signal, int memoryIdx, Word variable){
        
        if(signal == Signal.READ || signal == Signal.WRIT){
            if(variable==null){
                throw new LogicException("Variable cannot be null");
            }
        }
        
        /* Convert virtual address to physical address */
        memoryIdx += partition.getBaseAddress();
        if(memoryIdx>partition.getBoundAddress()){
            throw new SystemException(Errors.MEM_RANGE_OUT_OF_BOUND);
        }
        
        if(Signal.READ == signal){
            
            Word.copy(getCell(memoryIdx), variable);
            
        }else if(Signal.WRIT == signal){
            
            Word.copy(variable, getCell(memoryIdx));
            
        }else{
            /* dump the first xxx words*/
            PCB pcb = scheduler.getRunning();
            if(pcb!=null){
                
            }
            int numWords = 256;
            BufferedWriter bw = null;
            StringWriter writer = new StringWriter();
            try{
                bw = new BufferedWriter(writer);
                bw.append("0000\t");
                for(int i=0,j=1; i<numWords; i++,j++){
                    String hexValue = Bit.toHexString(addresses[i].getBits());
                    bw.append(StringUtils.leftPad(hexValue, 3, '0'));
                    if(j%8==0 && i< numWords-1){
                        String lineNumHex = new BigInteger(
                                String.valueOf(j), 10).toString(16);
                        bw.newLine();
                        bw.append(StringUtils.leftPad(lineNumHex, 4, '0'));
                    }
                    bw.append("\t");
                }
            }catch(Exception e){
                throw new SystemException(Errors.MEM_DUMP_FAILED);
            }finally{
                if(bw!=null){
                    try{ bw.close(); }catch(Exception e){}
                }
            }
            io.getLog().info("DUMP Memory[0-255] in hex:");
            io.getLog().info(writer.toString());
        }
    }
    
    protected Word getCell(Word memoryAddr){
        int memoryIdx = Bit.toDecimal(memoryAddr.getBits());
        return getCell(memoryIdx);
    }
    
    protected Word getCell(int memoryIdx){
        if(memoryIdx>=MEMORY_SIZE){
            throw new SystemException(Errors.MEM_RANGE_OUT_OF_BOUND);
        }
        return addresses[memoryIdx];
    }
    
    /** 
     * Memory operation enum
     * @author hussachai
     *
     */
    public static enum Signal {
        READ, WRIT, DUMP
    }
    
    /**
     * 
     * @author hussachai
     *
     */
    public static class Partition {
        
        private int id = 0;
        
        private int baseAddress = -1;
        
        private int boundAddress = -1;
        
        private boolean free = true;
        
        public Partition(int id, int baseAddress, int boundAddress){
            this.id = id;
            this.baseAddress = baseAddress;
            this.boundAddress = boundAddress;
        }
        
        public int getSize(){
            return boundAddress-baseAddress+1;
        }
        
        public int getId(){ return id; }
        public int getBaseAddress(){ return baseAddress; }
        public int getBoundAddress(){ return boundAddress; }
        public boolean isFree(){ return free; }
    }
}
