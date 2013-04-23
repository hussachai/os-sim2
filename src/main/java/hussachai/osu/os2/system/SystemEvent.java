package hussachai.osu.os2.system;

import hussachai.osu.os2.system.cpu.CPU;
import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.io.IOManager;
import hussachai.osu.os2.system.io.InterruptException;
import hussachai.osu.os2.system.loader.LoaderContext;
import hussachai.osu.os2.system.scheduler.PCB;
import hussachai.osu.os2.system.scheduler.Scheduler;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.storage.Memory.Partition;
import hussachai.osu.os2.system.storage.Memory.Signal;
import hussachai.osu.os2.system.unit.ID;
import hussachai.osu.os2.system.unit.Word;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author hussachai
 *
 */
public class SystemEvent {
    
    private static final int TITLE_PADDING_SIZE = 30;
    private static final int VALUE_PADDING_SIZE = 8;
    
    private CPU cpu;
    
    private Memory memory;
    
    private Scheduler scheduler;
    
    private IOManager io;
    
    /** number of jobs that terminated normally */
    private int successJobs = 0;
    /** number of jobs that terminated abnormally */
    private int errorJobs = 0;
    /** 
     * number of jobs that cannot be loaded due to format error
     * or program validation fail.
     */
    private int loadFailedJobs = 0;
    
    private int totalJobsRuntime = 0;
    private int totalJobsExecutionTime = 0;
    private int totalJobsIOTime = 0;
    /** total time of arrival until departure of each job */
    private int totalJobsTime = 0;
    
    private int totalTimeLostForErrors = 0;
    
    private int totalTimeLostForInfinited = 0;
    private List<Word> suspectedInfinitedList = new ArrayList<Word>();
    
    private int totalFragmentation = 0;
    
    private boolean eventJobInitiated = true;
    private boolean eventJobTerminated = true;
    private boolean eventLoadFailed = true;
    private boolean eventIORequest = false;
    private boolean eventContextSwitch = false;
    private boolean eventMemAlloc = false;
    
    /**
     * 
     * @param system
     */
    public void init(TheSystem system){
        this.cpu = system.getCPU();
        this.memory = system.getMemory();
        this.scheduler = system.getScheduler();
        this.io = system.getIO();
        
        if(Environment.get(Environment.EVENT_ALL)!=null){
            eventJobInitiated = true;
            eventJobTerminated = true;
            eventLoadFailed = true;
            eventIORequest = true;
            eventContextSwitch = true;
            eventMemAlloc = true;
        }
        if(Environment.get(Environment.EVENT_JOB_INIT)!=null){
            eventJobInitiated = true;
        }
        if(Environment.get(Environment.EVENT_JOB_TERM)!=null){
            eventJobTerminated = true;
        }
        if(Environment.get(Environment.EVENT_LOAD_FAIL)!=null){
            eventLoadFailed = true;
        }
        if(Environment.get(Environment.EVENT_IO_REQ)!=null){
            eventIORequest = true;
        }
        if(Environment.get(Environment.EVENT_CTX_SWITCH)!=null){
            eventContextSwitch = true;
        }
        if(Environment.get(Environment.EVENT_MALLOC)!=null){
            eventMemAlloc = true;
        }
    }
    
    /**
     * 
     * @param pcb
     */
    public void onJobInitiated(PCB pcb){
        if(!eventJobInitiated) return;
        
        if(pcb==null) return;
        
        writeLog("** [ON JOB INITIATED]");
        writeLog("Job ID (hex)", pcb.getJobID());
        
        int readySize = scheduler.getNumberOfJobsInReadyQueue();
        int blockedSize = scheduler.getNumberOfJobsInBlockedQueue();
        int running = scheduler.getRunning()!=null?1:0;
        
        int degreeOfMultiProgramming = readySize + blockedSize + running;
        
        writeLog("Number of ready jobs", readySize);
        writeLog("Number of blocked jobs", blockedSize);
        writeLog("Degree of multiprogramming", degreeOfMultiProgramming);
        
        writeLog("");
    }
    
    /**
     */
    public void onJobTerminated(Exception error){
        if(!eventJobTerminated) return;
        
        PCB pcb = scheduler.getRunning();
        if(pcb==null) return;
        
        if(error!=null){
            
            this.errorJobs++;
            this.totalTimeLostForErrors += pcb.getCPUUsageTime();
            
            if(error instanceof SystemException){                
                if(Errors.CPU_SUSPECTED_INFINITE_JOB
                        == ((SystemException)error).getErrorCode()){
                    
                    this.totalTimeLostForInfinited +=
                            pcb.getCPUUsageTime();
                    this.suspectedInfinitedList.add(pcb.getJobID());
                }
            }
        }else{
            /* Actually we can find this value from total - fails
             * but I want to use this value to check the correctness
             * of the program.
             */
            this.successJobs++;
            /* The following values are used to calculate the average
             * referring to only the jobs that completed normally.
             */
            this.totalJobsRuntime += (pcb.getCPUUsageTime()+pcb.getIOUsageTime());
            this.totalJobsIOTime += pcb.getIOUsageTime();
            this.totalJobsExecutionTime += pcb.getCPUUsageTime();
            this.totalJobsTime += (cpu.getClock()-pcb.getCreatedTime());
            
            this.totalFragmentation += pcb.getFragmentation(); 
        }
        
        writeLog("** [ON JOB TERMINATED]");
        writeLog("Job ID (hex)", pcb.getJobID());
        
        try{
            memory.memory(pcb.getPartition(), Signal.DUMP, 
                pcb.getOccupiedSpace(), null);
        }catch(SystemException e2){
            writeLog("Warning: memory dump failed due to: "+e2.getErrorCode());
        }
        
        writeLog("Input Lines", "");
        Word tmp = new Word();
        int startAddr = pcb.getLength();
        int endAddr = startAddr+pcb.getReaderIndex();//exclusive
        try{
            for(int i=startAddr;i<endAddr;i++){
                memory.memory(pcb.getPartition(), Signal.READ, i, tmp);
                writeLog("-", tmp);
            }
        }catch(SystemException e2){
            writeLog("Warning: tried to read data outside bound");
        }
        if((endAddr-startAddr) < pcb.getDataLines()){
            writeLog("Warning: provided data are more than program requires");
        }
        
        writeLog("Output Lines", "");
        startAddr = pcb.getLength()+pcb.getReaderIndex()+1;
        endAddr = startAddr+pcb.getWriterIndex();
        try{
            for(int i=startAddr;i<endAddr;i++){
                memory.memory(pcb.getPartition(), Signal.READ, i, tmp);
                writeLog("-", tmp);
            }
        }catch(SystemException e2){
            writeLog("Warning: tried to read data outside bound");
        }
        if((endAddr-startAddr) < pcb.getOutputLines()){
            writeLog("Warning: output space being reserved are more than program requires");
        }
        
        writeLog("Partition Number (dec)", pcb.getPartition().getID());
        writeLog("Occupied space (dec)", pcb.getOccupiedSpace());
        writeLog("Time entered (hex)", toHexString(pcb.getCreatedTime()));
        writeLog("Time left (hex)", toHexString(cpu.getClock()));
        writeLog("Runtime (dec)", pcb.getCPUUsageTime()+pcb.getIOUsageTime());
        writeLog("Execution time (dec)", pcb.getCPUUsageTime());
        writeLog("Time spent on I/O (dec)", pcb.getIOUsageTime());
        
    }
    
    /**
     * @param interrupt
     */
    public void onIORequested(InterruptException interrupt){
        if(!eventIORequest) return;
        
        PCB pcb = scheduler.getRunning();
        if(pcb==null) return;
        
        writeLog("** [ON I/O REQUEST]");
        writeLog("Job ID (hex)", pcb.getJobID());
        writeLog("Remaining quantum (dec)", pcb.getRemainingQuantum());
        writeLog("IO Type", interrupt.getIOType().name());
        writeLog("Current number of read (dec)", pcb.getReaderIndex());
        writeLog("Current number of write (dec)", pcb.getWriterIndex());
        writeLog("Number of blocked jobs (dec)", scheduler
                .getNumberOfJobsInBlockedQueue());
        
        writeLog("");
        
    }
    
    /**
     * 
     */
    public void onContextSwitched(){
        if(!eventContextSwitch) return;
        
        PCB pcb = scheduler.getRunning();
        if(pcb==null) return;
        
        /* Current registers in CPU being replaced */
        Word oldRegisters[] = cpu.createSnapshot();
        /* Register values to be restored to CPU */
        Word newRegisters[] = pcb.getRegisters();
        
        writeLog("** [ON CONTEXT SWITCH]");
        writeLog("Job ID (hex)", pcb.getJobID());
        writeLog("Previous CPU registers", Arrays.toString(oldRegisters));
        writeLog("New CPU registers", newRegisters==null?"N/A":
            Arrays.toString(newRegisters));
        writeLog("CPU usage time (dec)", pcb.getCPUUsageTime());
        writeLog("Number of ready jobs (dec)", scheduler
                .getNumberOfJobsInReadyQueue());
        
        writeLog("");
        
    }
    
    /**
     * 
     * @param context
     */
    public void onMemoryAllocated(LoaderContext context){
        if(!eventMemAlloc) return;
        
        writeLog("** [ON MEMORY ALLOCATION]");
        writeLog("Job ID (hex)", context.getJobID());
        if(context.getPartition()!=null){
            Partition partition = context.getPartition(); 
            writeLog("Memory partition ID", partition.getID());
            writeLog("Memory partition size (dec)", partition.getSize());
            writeLog("Occupied space (dec)", context.getOccupiedSpace());
        }
        writeLog("Free partitions [[ID] - size(dec)] : [base-bound](hex)");
        for(int i=0;i<Memory.PARTITION_NUMBERS;i++){
            Partition partition = memory.getPartition(i);
            if(partition.isFree()){
                writeLog("["+partition.getID()+"] - "+partition.getSize(), 
                        "["+toHexString(partition.getBaseAddress())
                        +"-"+toHexString(partition.getBoundAddress())+"]");
            }
        }
        
        writeLog("");
    }
    
    /**
     * @param loaderCntext
     * @param e
     */
    public void onLoadFailed(LoaderContext context, Exception e){
        if(!eventLoadFailed) return;
        
        this.loadFailedJobs++;
        
        writeLog("** [ON LOAD FAILED]");
        writeLog("Job ID (hex)", context.getJobID());
        if(context.getPartition()!=null){
            try{
                memory.memory(context.getPartition(), Signal.DUMP, 
                    context.getOccupiedSpace(), null);
            }catch(SystemException e2){
                writeLog("Warning: memory dump failed due to: "+e2.getErrorCode());
            }
            writeLog("Partition Number (dec)", 
                    context.getPartition().getID());
        }
        writeLog("Occupied space (dec)", context.getOccupiedSpace());
        
    }
    
    public void onSystemShutdown(){
        
        writeLog("** [FINAL STATISTICS]");
        writeLog("Current CPU clock (hex)", toHexString(cpu.getClock()));
        writeLog("Mean job runtime (dec)", divideSafely(
                totalJobsRuntime,successJobs));
        writeLog("Mean job execution time (dec)", 
                divideSafely(totalJobsExecutionTime,successJobs));
        writeLog("Mean job I/O (dec)", divideSafely(totalJobsIOTime,successJobs));
        writeLog("Mean job time (dec)", divideSafely(totalJobsTime,successJobs));
        writeLog("Total CPU idle time (dec)", cpu.getIdleTime());
        writeLog("Total time lost for errors (dec)", totalTimeLostForErrors);
        writeLog("Number of jobs that terminated normally (dec)", successJobs);
        writeLog("Number of jobs that terminated abnormally (dec)", errorJobs);
        writeLog("Number of jobs that loaded unsucessfully (dec)", loadFailedJobs);
        writeLog("Total time lost for infinited jobs (dec)", 
                totalTimeLostForInfinited);
        writeLog("ID of jobs considered infinited (hex)", suspectedInfinitedList);
        writeLog("Mean internal fragmentation (dec)", 
                divideSafely(totalFragmentation,successJobs));
    }
    
    public void writeLog(String message){
        io.getLog().info(message);
    }
    
    public void writeLog(ID jobID, String message){
        io.getLog().info("[Job ID (hex): "+jobID+"] : "+message);
    }
    
    public void writeLog(String title, Object value){
        if(value instanceof Double){
            value = new DecimalFormat("###,##0.00")
                .format(((Double) value));
        }else if(value instanceof Integer){
            value = new DecimalFormat("###,##0")
                .format(((Integer) value));
        }
        value = StringUtils.leftPad(String
                .valueOf(value), VALUE_PADDING_SIZE);
        if(title.length()>TITLE_PADDING_SIZE){
            title = StringUtils.rightPad(title, 
                    TITLE_PADDING_SIZE + 18);
        }else{
            title = StringUtils.rightPad(title, 
                    TITLE_PADDING_SIZE);
        }
        writeLog(title + " : " + value);
    }
    
    public String toHexString(Number number){
        String value = new BigInteger(
                number.toString(), 10).toString(16);
        return StringUtils.leftPad(value.toUpperCase(), 4, '0');
    }
    
    private double divideSafely(int num1, int num2){
        if(num2==0) return 0.0;
        return num1/num2;
    }
    
    
}
