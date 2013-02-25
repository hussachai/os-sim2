package hussachai.osu.os2.system;

import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemError;
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
	
	public CPU(){
		
		//allocate space for register
		for(int i=0; i<registers.length;i++){
			registers[i] = new Word();
		}
		registers[0].getBits()[Word.SIZE-1] = Bit.O;
		registers[1].getBits()[Word.SIZE-1] = Bit.I;
	}
	
	
	public void run(){
		
		
		
		Word pc = registers[R_PC];
		
		int pcValue = Bit.toDecimal(pc.getBits());
		
		/* Fetch instruction */
		
		
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
	
	/**
	 * 
	 * @param bits
	 * @return
	 */
	protected void twosComplement(Word word){
		Bit bits[] = word.getBits();
		int bitLength = bits.length;
		Bit newValues[] = new Bit[bitLength];
		for(int i=0;i<bits.length;i++){
			//flip-flop all bits
			newValues[i] = bits[i].ordinal()==0?Bit.I:Bit.O;
		}
		for(int i=bitLength-1;i>=0;i--){
			//increment by 1
			if(newValues[i].ordinal()==0){
				newValues[i] = Bit.I;
				break;
			}else{
				newValues[i] = Bit.O;
			}
		}
		System.arraycopy(newValues, 0, word.getBits(), 0, Word.SIZE);
	}
	
	/**
	 * Increment program counter by 1
	 * PC uses unsigned number.
	 */
	protected void incrementPC(){
		Bit bits[] = registers[R_PC].getBits();
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
			throw new SystemError(Errors.PC_OVERFLOW);
		}
	}
	
	/**
	 * signed number support incremental
	 */
	protected void increment(Word word){
		Bit bits[] = word.getBits();
		if(!word.isNegativeNumber()){
			Bit secondBit = bits[1];
			for(int i=Word.SIZE-1;i>=0;i--){
				if(bits[i].ordinal()==0){
					bits[i] = Bit.I;
					break;
				}else{
					bits[i] = Bit.O;
				}
			}
			if(secondBit==Bit.I && bits[1]==Bit.O){
				throw new SystemError(Errors.ARITHMETIC_OVERFLOW);
			}
		}else{
			Word one = new Word();
			one.getBits()[Word.SIZE-1] = Bit.I;
			add(word.getBits(), one.getBits(), true);
		}
	}
	
	/**
	 * Binary addition operation that supports negative number adding 
	 * in the form of 2's complement.
	 * 
	 * @param destination
	 * @param values
	 */
	protected void add(Bit destination[], Bit values[], boolean ignoreOverflow){
		if(values.length>destination.length){
			throw new SystemError(Errors.RANGE_OUT_OF_BOUND);
		}
		boolean isCarriedOver = false;
		for(int i=values.length-1;i>=0;i--){
			if ( (destination[i].ordinal() | values[i].ordinal()) == 1){//01
				if( (destination[i].ordinal() & values[i].ordinal()) == 1){//11
					if(isCarriedOver){
						destination[i] = Bit.I;
					}else{
						destination[i] = Bit.O;
					}
					isCarriedOver = true;
				}else{//01
					if(isCarriedOver){
						destination[i] = Bit.O;
						isCarriedOver = true;
					}else{
						destination[i] = Bit.I;
						isCarriedOver = false;
					}
				}
			}else{
				if(isCarriedOver){
					destination[i] = Bit.I;
					isCarriedOver = false;
				}
			}
		}
		if(!ignoreOverflow && isCarriedOver){
			throw new SystemError(Errors.ARITHMETIC_OVERFLOW);
		}
	}
	
	
}
