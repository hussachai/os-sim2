package hussachai.osu.os2.system.cpu;

import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.LogicException;
import hussachai.osu.os2.system.error.SystemError;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;

/**
 * 
 * @author hussachai
 *
 */
public class ControlUnit {
	
	private CPU cpu;
	
	public ControlUnit(CPU cpu){
		this.cpu = cpu;
	}
	
	public Word fetch(){
		
		Word pc = cpu.registers[CPU.R_PC];
		int address = Bit.toDecimal(pc.getBits());
		
		return null;
	}
	
	/**
	 * 
	 * @param instruction
	 * @return
	 */
	public OpCode decode(Word instruction){
		
		Bit bits[] = instruction.getBits();
		if(bits[1]==Bit.O && bits[2]==Bit.O && bits[3]==Bit.O){
			return OpCode.AND;//000
		}else if(bits[1]==Bit.O && bits[2]==Bit.O && bits[3]==Bit.I){
			return OpCode.ADD;//001
		}else if(bits[1]==Bit.O && bits[2]==Bit.I && bits[3]==Bit.O){
			return OpCode.STR;//010
		}else if(bits[1]==Bit.O && bits[2]==Bit.I && bits[3]==Bit.I){
			return OpCode.LD;//011
		}else if(bits[1]==Bit.I && bits[2]==Bit.O && bits[3]==Bit.O){
			return OpCode.JMP;//100
		}else if(bits[1]==Bit.I && bits[2]==Bit.O && bits[3]==Bit.I){
			return OpCode.JPL;//101
		}else if(bits[1]==Bit.I && bits[2]==Bit.I && bits[3]==Bit.O){
			/* Type II => 110*/
			int totalBits = bits[5].ordinal()+bits[6].ordinal()
					+bits[7].ordinal();
			if(totalBits>1 || totalBits==0){
				throw new SystemError(Errors.INVALID_OP_CODE);
			}
			if(bits[5]==Bit.I){
				return OpCode.RD;
			}else if(bits[6]==Bit.I){
				return OpCode.WR;
			}
			return OpCode.HLT;
		}else if(bits[1]==Bit.I && bits[2]==Bit.I && bits[3]==Bit.I){
			if(bits[0]==Bit.O){
				/* Type III => 111*/
				
			}else{
				/* Type IV */
				int equal = bits[5].ordinal();
				int less = bits[6].ordinal();
				int greater = bits[7].ordinal();
				int sum = equal+less+greater;
				
				if(sum==0){
					return OpCode.NSK;
				}else if(equal==0 && less==0 && greater==1){
					return OpCode.GTR;
				}else if(equal==0 && less==1 && greater==0){
					return OpCode.LSS;
				}else if(equal==0 && less==1 && greater==1){
					return OpCode.NEQ;
				}else if(equal==1 && less==0 && greater==0){
					return OpCode.EQL;
				}else if(equal==1 && less==0 && greater==1){
					return OpCode.GRE;
				}else if(equal==1 && less==1 && greater==0){
					return OpCode.LSE;
				}else if(sum==3){
					return OpCode.USK;
				}
			}
		}
		
		throw new LogicException("Unrecognized Op-code :"+instruction);
		
	}
	
}
