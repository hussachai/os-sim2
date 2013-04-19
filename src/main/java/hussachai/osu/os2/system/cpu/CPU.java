/*
 * Name: Hussachai Puripunpinyo
 * Course No.:  CS 5323
 * Assignment title: PHASE I (March 5)
 * TA's Name: 
 *  - Alireza Boloorchi
 *  - Sukanya Suwisuthikasem
 * Global variables:
 *  - registers (The array of registers R0-R9)
 *  - mar (Memory address register)
 *  - mbr (Memory buffere register)
 *  - clock (The clock is used as a counter for CPU cycle and IO)
 *  - inputTime (The total virtual time spent for input)
 *  - outputTime (The total virtual time spent for output) 
 *  - execTime (The total virtual time spent for CPU cycle)
 *  - traceSwitch
 *  - controlUnit (The reference to Control Unit which is part of CPU) 
 *  - alUnit (The reference to Arithmetic and Logic Unit which is part of CPU)
 *  - memory (The reference to Memory)
 *  - io (The reference to InputOutput)
 *   
 *  Brief Description:
 *  CPU is the central processing unit which contains 2 subunit - Control unit
 *  and Arithmetic & Logic Unit (ALU). The control unit will handle fetching and
 *  decoding of the instruction respectively. The ALU will handle the execution
 *  of instruction.
 *  
 */
package hussachai.osu.os2.system.cpu;

import static hussachai.osu.os2.system.util.TraceFormatter.trace;
import static hussachai.osu.os2.system.util.TraceFormatter.traceEmpty;
import hussachai.osu.os2.system.Scheduler;
import hussachai.osu.os2.system.TheSystem;
import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.io.InputOutput;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.storage.Memory.Signal;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;

/**
 * Central processing unit
 * @author hussachai
 *
 */
public class CPU {
    
    /** Instruction execution time **/
    public static final int TIME_IE = 1;
    /** I/O time **/
    public static final int TIME_IO = 10;
    /** Task time limit for detecting infinite loop. */
    public static final int TIME_LIMIT = 500;
    
    /** Alias for register R0 - constant number 0*/
    public static final int R_0 = 0;
    /** Alias for register R1 - constant number 1*/
    public static final int R_1 = 1;
    /** Alias for register R2 - program counter (PC)*/
    public static final int R_PC = 2;
    /** Alias for register R3 - instruction register (IR) */
    public static final int R_IR = 3;
    /** Alias for register R4 - index register*/
    public static final int R_IDX = 4;
    /** Alias for register R5 - accumulator (default destination of calculation)*/
    public static final int R_ACC = 5;
    
    /** Alias for register R6 - R9 (scratch-pad) */
    public static final int R_TMP1 = 6;
    public static final int R_TMP2 = 7;
    public static final int R_TMP3 = 8;
    public static final int R_TMP4 = 9;
    
    protected Word registers[] = new Word[10];
    
    /** Memory address register (MAR) */
    protected Word mar = new Word();
    
    /** Memory buffer register (MBR) */
    protected Word mbr = new Word();
    
    protected int clock = 0;
    
    protected int inputTime = 0, outputTime = 0, execTime = 0;//TODO: must be kept in PCB or something
    
    protected Bit traceSwitch = Bit.O;
    
    protected ControlUnit controlUnit;
    
    protected ALUnit alUnit;
    
    protected Memory memory;
    
    protected InputOutput io;
    
    protected Scheduler scheduler;
    
    public CPU(TheSystem system){
        
        this.memory = system.getMemory();
        this.io = system.getIO();
        this.scheduler = system.getScheduler();
        
        //allocate space for register
        for(int i=0; i<registers.length;i++){
            registers[i] = new Word();
        }
        registers[0].getBits()[Word.SIZE-1] = Bit.O;
        registers[1].getBits()[Word.SIZE-1] = Bit.I;
        
        controlUnit = new ControlUnit(this);
        alUnit = new ALUnit(this);
    }
    
    /** get clock value in decimal **/
    public int getClock(){ return this.clock; }
    
    public int getInputTime(){ return this.inputTime; }
    
    public int getOutputTime(){ return this.outputTime; }
    
    /**
     * Specification required method name.
     */
    public void cpu(Word pc, Bit traceSwitch){
        
        this.registers[R_PC] = pc;
        this.traceSwitch = traceSwitch;
        
        while(true){
            boolean hasNext = runCycle();
            clock += TIME_IE;
            execTime += TIME_IE;
            if(execTime>=TIME_LIMIT){
                throw new SystemException(Errors.CPU_SUSPECTED_INFINITE_JOB);
            }
            if(!hasNext) break;
        }
        
    }
    
    /**
     * Do nothing except incrementing clock
     */
    public void idle(){
        clock += TIME_IE;
    }
    
    protected boolean runCycle(){
        
        boolean hasNext = false;
        
        Word pc = registers[R_PC];
        Word instruction = registers[R_IR];
        Word tmp1 = registers[R_TMP1];
        Word tmp2 = registers[R_TMP2];
        
        /* Fetch instruction to IR*/
        controlUnit.fetch();
        
        OpCode opCode = controlUnit.decode();
        
        Word targetR = null;
        Word ea = null;
        String traceInfo = null;
        
        if(traceSwitch==Bit.I){
            
            Bit rBit = instruction.getBits()[4];
            targetR = (rBit==Bit.I)?registers[R_IDX]:registers[R_ACC];
            ea = (opCode.getType()==1)?this.mar:null;
            traceInfo = trace(pc, instruction, targetR);
            
            if(ea!=null){
                memory.memory(Signal.READ, ea, tmp2);
                traceInfo += trace(ea);
            }else{
                traceInfo += traceEmpty();
            }
            
            memory.memory(Signal.READ, targetR, tmp1);
            
            traceInfo += trace(tmp1);
            if(ea!=null){
                traceInfo += trace(tmp2);
            }else{
                traceInfo += traceEmpty();
            }
        }
        
        /* Increment PC before executing instruction */
        incrementPC();
        
        hasNext = alUnit.execute(opCode);
        
        if(traceSwitch==Bit.I){
            memory.memory(Signal.READ, targetR, tmp1);
            traceInfo += trace(tmp1);
            if(ea!=null){
                memory.memory(Signal.READ, ea, tmp2);
                traceInfo += trace(tmp2);
            }else{
                traceInfo += traceEmpty();
            }
            
            io.getLog().trace(traceInfo);
        }
        
        return hasNext;
    }
    
    /**
     * Increment program counter by 1
     * PC uses unsigned number.
     */
    protected void incrementPC(){
        Bit bits[] = registers[CPU.R_PC].getBits();
        Bit firstBit = bits[0];
        registers[CPU.R_PC].increment();
        if(firstBit==Bit.I && bits[0]==Bit.O){
            throw new SystemException(Errors.MEM_PC_OVERFLOW);
        }
    }
    
}
