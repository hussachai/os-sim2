package hussachai.osu.os2.system.cpu;

import hussachai.osu.os2.system.io.IOManager.IOType;
import hussachai.osu.os2.system.io.InterruptException;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.storage.Memory.Signal;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;

/**
 * Arithmetic and Logic Unit which is part of CPU
 * It has all methods for calculation regarding to the instruction type
 * and its attributes.
 * It can access the CPU attributes directly.
 * 
 * @author hussachai
 */
public class ALUnit {
    
    private CPU cpu;
    
    private Memory memory;
    
    public ALUnit(CPU cpu){
        this.cpu = cpu;
        this.memory = cpu.memory;
    }
    
    /**
     * @param opCode
     */
    public boolean execute(OpCode opCode){
        
        Word tmp1 = cpu.registers[CPU.R_TMP1];
        Word tmp2 = cpu.registers[CPU.R_TMP2];
        Word tmp3 = cpu.registers[CPU.R_TMP3];
        
        Word pc = cpu.registers[CPU.R_PC];
        Word r4 = cpu.registers[CPU.R_IDX];
        Word r5 = cpu.registers[CPU.R_ACC];
        Word instruction = cpu.registers[CPU.R_IR];
        /* targetR can be either R4 or R5 depending on bit 4th in instruction */
        Word targetR = instruction.getBits()[4]==Bit.I?r4:r5;
        Word a = tmp3;
        Word.copy(pc, a);
        
        Bit insAddrBits[] = instruction.slice(6, 11);
        Word addrWord = new Word(Bit.leftPad(insAddrBits, Word.SIZE, insAddrBits[0]));
        
        Word.add(a, addrWord);
        
        Word ea = null;
        if(opCode.getType()==1){
            /*calculate effective address*/
            Bit bitI = instruction.getBits()[0];
            Bit bitX = instruction.getBits()[5];
            if(bitI==Bit.I){
                if(bitX==Bit.I){
                    /* indirection + indexing addressing */
                    memory.memory(Signal.READ, a, tmp1);
                    memory.memory(Signal.READ, r4, tmp2);
                    Bit.add(tmp1.getBits(), tmp2.getBits(), false);
                    Word.copy(tmp1, cpu.mar);
                }else{
                    /* indirection addressing */
                    memory.memory(Signal.READ, a, cpu.mar);
                }
            }else{
                if(bitX==Bit.I){
                    /* indexing addressing */
                    memory.memory(Signal.READ, r4, tmp2);
                    Bit.add(a.getBits(), tmp2.getBits(), false);
                    Word.copy(a, cpu.mar);
                }else{
                    /* direct addressing */
                    Word.copy(a, cpu.mar);
                }
            }
            ea = cpu.mar;
        }
        
        switch(opCode){
        /* Type I */
        case AND:
            memory.memory(Signal.READ, ea, tmp1);
            Word.and(targetR, tmp1);
            break;
        case ADD:
            memory.memory(Signal.READ, ea, tmp1);
            Word.add(targetR, tmp1);
            break;
        case STR:
            memory.memory(Signal.WRIT, ea, targetR);
            break;
        case LD:
            memory.memory(Signal.READ, ea, targetR);
            break;
        case JMP:
            Word.copy(ea, pc);
            break;
        case JPL:
            Word.copy(pc, targetR);
            Word.copy(ea, pc);
            break;
        /* Type II */
        case RD:
            throw new InterruptException(IOType.Read, targetR);
        case WR:
            throw new InterruptException(IOType.Write, targetR);
        case HLT:
            return false;//stop program
        /* Type III */
        case CLR:
            Word.clear(targetR);
            break;
        case INC:
            Word.increment(targetR);
            break;
        case COM:
            Word.oneComplement(targetR);
            break;
        case BSW:
            Word.swapBytes(targetR);
            break;
        case RTL:
            if(instruction.getBits()[11]==Bit.O){
                Word.shiftLeft(targetR, 1);
            }else{
                Word.shiftLeft(targetR, 2);
            }
            break;
        case RTR:
            if(instruction.getBits()[11]==Bit.O){
                Word.shiftRight(targetR, 1);
            }else{
                Word.shiftRight(targetR, 2);
            }
            break;
            
        /* Type IV */
        case NSK:
            //no operation
            break;
        case GTR:
            if(Word.greaterThanZero(targetR)) cpu.incrementPC();
            break;
        case LSS:
            if(Word.lessThanZero(targetR)) cpu.incrementPC();
            break;
        case NEQ:
            if(!Word.equalZero(targetR)){
                cpu.incrementPC();
            }
            break;
        case EQL:
            if(Word.equalZero(targetR)) cpu.incrementPC();
            break;
        case GRE:
            if(Word.greaterThanZero(targetR) || Word.equalZero(targetR))
                cpu.incrementPC();
            break;
        case LSE:
            if(Word.lessThanZero(targetR) || Word.equalZero(targetR))
                cpu.incrementPC();
            break;
        case USK:
            cpu.incrementPC();
            break;
            
        }
        
        return true;
    }
    
}
