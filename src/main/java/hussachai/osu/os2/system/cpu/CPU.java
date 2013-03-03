package hussachai.osu.os2.system.cpu;

import hussachai.osu.os2.system.TheSystem;
import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.io.InputOutput;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.storage.Memory.Signal;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;

/**
 * 
 * @author hussachai
 *
 */
public class CPU {
	
	/** Instruction execution time **/
	public static final int TIME_IE = 1;
	/** I/O time **/
	public static final int TIME_IO = 10;
	
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
	
	protected int inputTime = 0, outputTime = 0;
	
	protected Bit traceSwitch = Bit.O;
	
	protected ControlUnit controlUnit;
	
	protected ALUnit alUnit;
	
	protected Memory memory;
	
	protected InputOutput io;
	
	public CPU(TheSystem system){
		
		this.memory = system.getMemory();
		this.io = system.getIO();
		
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
			if(!hasNext) break;
		}
		
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
			traceInfo = pc+"\t"+instruction+"\t"+targetR+"\t";
			
			if(ea!=null){
				memory.memory(Signal.READ, ea, tmp2);
				traceInfo += ea+"\t";
			}else{
				traceInfo += "------------\t";
			}
			
			memory.memory(Signal.READ, targetR, tmp1);
			
			traceInfo += tmp1+"\t";
			if(ea!=null){
				traceInfo += tmp2+"\t";
			}else{
				traceInfo += "------------\t";
			}
		}
		
		/* Increment PC before executing instruction */
		incrementPC();
		clock = clock + TIME_IE;
		
		hasNext = alUnit.execute(opCode);
		
		if(traceSwitch==Bit.I){
			memory.memory(Signal.READ, targetR, tmp1);
			traceInfo += tmp1+"\t";
			if(ea!=null){
				memory.memory(Signal.READ, ea, tmp2);
				traceInfo += tmp2+"\t";
			}else{
				traceInfo += "------------\t";
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
		for(int i=Word.SIZE-1;i>=0;i--){
			if(bits[i].ordinal()==0){
				bits[i] = Bit.I;
				break;
			}else{
				bits[i] = Bit.O;
			}
		}
		if(firstBit==Bit.I && bits[0]==Bit.O){
			throw new SystemException(Errors.MEM_PC_OVERFLOW);
		}
	}
	
}
