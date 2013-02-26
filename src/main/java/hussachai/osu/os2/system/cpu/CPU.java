package hussachai.osu.os2.system.cpu;

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
	
	/** Alias for register R2 - program counter*/
	public static final int R_PC = 2;
	/** Alias for register R3 - instruction register */
	public static final int R_IR = 3;
	/** Alias for register R4 - index register*/
	public static final int R_IDX = 4;
	/** Alias for register R5 - accumulator*/
	public static final int R_ACC = 5;
	
	
	/**
	 * Register R0 is used for storing constant number 0
	 * Register R1 is used for storing constant number 1
	 * Register R2 is the program counter (PC)
	 * Register R3 is instruction register (IR)
	 * Register R4 can be used as index register
	 * Register R5 is accumulator (default destination of calculation)
	 * Register R4-R9 are scratch pad
	 */
	protected Word registers[] = new Word[10];
	
	/**
	 * Memory address register
	 */
	protected Word mar = new Word();
	
	/**
	 * Memory buffer register
	 */
	protected Word mbr = new Word();
	
	protected int clock = 0;
	
	protected ControlUnit controlUnit;
	
	protected ALU alUnit;
	
	public CPU(){
		
		//allocate space for register
		for(int i=0; i<registers.length;i++){
			registers[i] = new Word();
		}
		registers[0].getBits()[Word.SIZE-1] = Bit.O;
		registers[1].getBits()[Word.SIZE-1] = Bit.I;
		
		controlUnit = new ControlUnit(this);
		alUnit = new ALU(this);
	}
	
	
	public void run(){
		
		Word pc = registers[R_PC];
		
		int pcValue = Bit.toDecimal(pc.getBits());
		
		/* Fetch instruction */
		
		Word instruction = controlUnit.fetch();
		
		OpCode opCode = controlUnit.decode(instruction);
		
		alUnit.execute(opCode, instruction);
		
		/* Calculate EA */
		/* Use R9 as scratch-pad to store adding result
		 * For op-code add, we will use R5 instead.
		 * The reason why we use scratch-pad because in CPU,
		 * we cannot create a new memory to store value.
		 * It's a hardware.
		 */
		Word r9 = registers[9];
		/* Use native copy which is much more faster than using loop */
		System.arraycopy(pc, 0, r9, 0, Word.SIZE);
		
	}
	
	
}
