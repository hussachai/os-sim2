package hussachai.osu.os2.system.misc;

import hussachai.osu.os2.system.TheSystem;
import hussachai.osu.os2.system.cpu.CPU;
import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.io.IOManager;
import hussachai.osu.os2.system.loader.Loader.Context;
import hussachai.osu.os2.system.scheduler.PCB;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.storage.Memory.Signal;
import hussachai.osu.os2.system.unit.Word;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class SystemStat {
    
    private static final int TITLE_PADDING_SIZE = 30;
    private static final int VALUE_PADDING_SIZE = 5;
    
    private CPU cpu;
    
    private Memory memory;
    
    private IOManager io;
    
    /** number of jobs that terminated normally */
    private int totalSuccesses = 0;
    /** number of jobs that terminated abnormally */
    private int totalErrors = 0;
    
    private int totalJobsRuntime = 0;
    private int totalJobsExecutionTime = 0;
    private int totalJobsIOTime = 0;
    /** total time of arrival until departure of each job */
    private int totalJobsTime = 0;
    
    private int totalTimeLostForErrors = 0;
    
    private int totalTimeLostForInfinited = 0;
    private List<Word> suspectedInfinitedList = new ArrayList<Word>();
    
    private int totalFragmentation = 0;
    
    public void init(TheSystem system){
        this.cpu = system.getCPU();
        this.memory = system.getMemory();
        this.io = system.getIO();
    }
    
    public void onJobInitiated(PCB pcb){
        
        
    }
    
    /**
     */
    public void onJobTerminated(PCB pcb, Exception e){
        
        if(e!=null){
            this.totalErrors++;
            this.totalTimeLostForErrors += pcb.getCPUUsageTime();
            if(e instanceof SystemException){                
                if(Errors.CPU_SUSPECTED_INFINITE_JOB
                        == ((SystemException)e).getErrorCode()){
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
            this.totalSuccesses++;
            /* The following values are used to calculate the average
             * referring to only the jobs that completed normally.
             */
            this.totalJobsRuntime += (pcb.getCPUUsageTime()+pcb.getIOUsageTime());
            this.totalJobsIOTime += pcb.getIOUsageTime();
            this.totalJobsExecutionTime += pcb.getCPUUsageTime();
            this.totalJobsTime += (cpu.getClock()-pcb.getCreatedTime());
            
            this.totalFragmentation += pcb.getFragmentation(); 
        }
        
        writeLog("Job ID (hex)", pcb.getJobID());
        try{
            memory.memory(pcb.getPartition(), Signal.DUMP, pcb.getOccupiedSpace()-1, null);
        }catch(Exception e2){
            System.out.println();
        }
        writeLog("Input Lines", "");
        Word tmp = new Word();
        int startAddr = pcb.getLength();
        int endAddr = startAddr+pcb.getReaderIndex();//exclusive
        for(int i=startAddr;i<endAddr;i++){
            memory.memory(pcb.getPartition(), Signal.READ, i, tmp);
            writeLog("", tmp);
        }
        if((endAddr-startAddr) < pcb.getDataLines()){
            io.getLog().info("Warning: provided data are more than program requires");
        }
        writeLog("Output Lines", "");
        startAddr = pcb.getLength()+pcb.getReaderIndex()+1;
        endAddr = startAddr+pcb.getWriterIndex();
        for(int i=startAddr;i<endAddr;i++){
            memory.memory(pcb.getPartition(), Signal.READ, i, tmp);
            writeLog("", tmp);
        }
        if((endAddr-startAddr) < pcb.getOutputLines()){
            io.getLog().info("Warning: output space being reserved are more than program requires");
        }
        writeLog("Partition Number (dec)", pcb.getPartition().getId());
        writeLog("Occupied space (dec)", pcb.getOccupiedSpace());
        writeLog("Time entered (hex)", toHexString(pcb.getCreatedTime()));
        writeLog("Time left (hex)", toHexString(cpu.getClock()));
        writeLog("Runtime (dec)", pcb.getCPUUsageTime()+pcb.getIOUsageTime());
        writeLog("Execution time (dec)", pcb.getCPUUsageTime());
        writeLog("Time spent on I/O (dec)", pcb.getIOUsageTime());
        
    }
    
    /**
     * This event will be fired when job is terminated abnormally.
     * The loaderContext is used instead of PCB because this job
     * is not initiated yet.
     * @param loaderCntext
     * @param e
     */
    public void onJobTerminated(Context loaderContext, Exception e){
        
        
    }
    
    public void onSystemShutdown(){
        
        io.getLog().info("\n");
        writeLog("Current CPU clock (hex)", toHexString(cpu.getClock()));
        writeLog("Mean job runtime (dec)", divideSafely(
                totalJobsRuntime,totalSuccesses));
        writeLog("Mean job execution time (dec)", 
                divideSafely(totalJobsExecutionTime,totalSuccesses));
        writeLog("Mean job I/O (dec)", divideSafely(totalJobsIOTime,totalSuccesses));
        writeLog("Mean job time (dec)", divideSafely(totalJobsTime,totalSuccesses));
        writeLog("Total CPU idle time (dec)", cpu.getIdleTime());
        writeLog("Total time lost for errors (dec)", totalTimeLostForErrors);
        writeLog("Number of jobs that terminated normally (dec)", totalSuccesses);
        writeLog("Number of jobs that terminated abnormally (dec)", totalErrors);
        writeLog("Total time lost for infinited jobs (dec)", 
                totalTimeLostForInfinited);
        writeLog("ID of jobs considered infinited (hex)", suspectedInfinitedList);
        writeLog("Mean internal fragmentation (dec)", 
                divideSafely(totalFragmentation,totalSuccesses));
    }
    
    public void writeLog(String title, Object value){
        if(value instanceof Double){
            value = new DecimalFormat("###,###.00")
                .format(((Double) value));
        }else if(value instanceof Integer){
            value = new DecimalFormat("###,###")
                .format(((Integer) value));
        }
        value = StringUtils.leftPad(String
                .valueOf(value), VALUE_PADDING_SIZE);
        io.getLog().info(StringUtils.rightPad(title, 
                TITLE_PADDING_SIZE)+" : "+value);
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
