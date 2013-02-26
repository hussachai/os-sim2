package hussachai.osu.os2.system.cpu;

import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemError;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;

/**
 * 
 * @author hussachai
 *
 */
public class ALU {
	
	private CPU cpu;
	
	public ALU(CPU cpu){
		this.cpu = cpu;
	}
	
	public void execute(OpCode opCode, Word instruction){
		
		switch(opCode){
		/* Type I */
		case AND:
			
			break;
		case ADD:
			break;
		case STR:
			break;
		case LD:
			break;
		case JMP:
			break;
		case JPL:
			break;
		/* Type II */
		case RD:
			break;
		case WR:
			break;
		case HLT:
			break;
		/* Type III */
		case CLR:
			break;
		case INC:
			
			break;
		case COM:
			break;
		case BSW:
			break;
		case RTL:
			break;
		case RTR:
			break;
			
		/* Type IV */
		case NSK:
			break;
		case GTR:
			break;
		case LSS:
			break;
		case NEQ:
			break;
		case EQL:
			break;
		case GRE:
			break;
		case LSE:
			break;
		case USK:
			break;
			
		}
	}
	
	/**
	 * 
	 * @param destination
	 * @param value
	 */
	protected void and(Word destination, Word value){
		Bit destBits[] = destination.getBits();
		Bit valueBits[] = value.getBits();
		int sum = 0;
		for(int i=0;i<Word.SIZE;i++){
			sum = destBits[i].ordinal()+valueBits[i].ordinal();
			destBits[i] = sum==2?Bit.I:Bit.O; 
		}
	}
	
	/**
	 * 
	 * @param destination
	 * @param value
	 */
	protected void add(Word destination, Word value){
		add(destination.getBits(), value.getBits(), false);
	}
	
	/**
	 * 
	 * @param word
	 */
	protected void clear(Word word){
		Bit bits[] = word.getBits();
		for(int i=0;i<Word.SIZE;i++){
			bits[i] = Bit.O;
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
	 * 
	 * @param word
	 */
	protected void oneComplement(Word word){
		Bit bits[] = word.getBits();
		for(int i=0;i<Word.SIZE;i++){
			//flip-flop all bits
			bits[i] = bits[i].ordinal()==0?Bit.I:Bit.O;
		}
	}
	
	/**
	 * 
	 * @param word
	 */
	protected void byteSwap(Word word){
		Bit bits[] = word.getBits();
		Bit tmp = null;
		for(int i=0;i<Word.SIZE;i+=Word.NIBBLE_SIZE){
			tmp = bits[i];
			bits[i] = bits[i+Word.NIBBLE_SIZE];
			bits[i+Word.NIBBLE_SIZE] = tmp;
		}
	}
	
	/**
	 * 
	 * In-place array shit left
	 * @param word
	 * @param number
	 */
	protected void shiftLeft(Word word, int number){
		Bit bits[] = word.getBits();
		for(int x=0;x<number;x++){
			Bit firstBit = bits[0];
			//loop to the second last bit
			for(int i=0;i<Word.SIZE-1;i++){
				bits[i] = bits[i+1];
			}
			bits[Word.SIZE-1] = firstBit;
		}
	}
	
	/**
	 * 
	 * In-place array shit right
	 * @param word
	 * @param number
	 */
	protected void shiftRight(Word word, int number){
		Bit bits[] = word.getBits();
		for(int x=0;x<number;x++){
			Bit lastBit = bits[Word.SIZE-1];
			for(int i=Word.SIZE-1;i>0;i--){
				bits[i] = bits[i-1];
			}
			bits[0] = lastBit;
		}
	}
	
	/**
	 * 
	 * @param word
	 * @return
	 */
	protected void twosComplement(Word word){
		Bit bits[] = word.getBits();
		oneComplement(word);
		for(int i=Word.SIZE-1;i>=0;i--){
			//increment by 1
			if(bits[i].ordinal()==0){
				bits[i] = Bit.I;
				break;
			}else{
				bits[i] = Bit.O;
			}
		}
	}
	
	/**
	 * Increment program counter by 1
	 * PC uses unsigned number.
	 */
	protected void incrementPC(){
		Bit bits[] = cpu.registers[CPU.R_PC].getBits();
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
