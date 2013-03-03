package hussachai.osu.os2.system.cpu;

import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.LogicException;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;

/**
 * 
 * @author hussachai
 *
 */
public class ControlUnit {
	
	private CPU cpu;
	
	private Memory memory;
	
	public ControlUnit(CPU cpu){
		this.cpu = cpu;
		this.memory = cpu.memory;
	}
	
	/**
	 * Fetch instruction from memory to MBR (Memory Buffer Register)
	 * Then CPU will read data from MBR to IR
	 * @return
	 */
	public void fetch(){
		
		Word pc = cpu.registers[CPU.R_PC];
		
		memory.memory(Memory.Signal.READ, pc, cpu.mbr);
		
		Word.copy(cpu.mbr, cpu.registers[CPU.R_IR]);
		
	}
	
	/**
	 * 
	 * @param instruction
	 * @return
	 */
	public OpCode decode(){
		
		Word instruction = cpu.registers[CPU.R_IR];
		
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
				throw new SystemException(Errors.PROG_INVALID_OP);
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
				if(bits[5]==Bit.I) return OpCode.CLR;
				if(bits[6]==Bit.I) return OpCode.INC;
				if(bits[7]==Bit.I) return OpCode.COM;
				if(bits[8]==Bit.I) return OpCode.BSW;
				if(bits[9]==Bit.I) return OpCode.RTL;
				if(bits[10]==Bit.I) return OpCode.RTR;
				//bits[11] not exist in specification
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
