package hussachai.osu.os2.system.cpu;

import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemException;
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
        
        add(a, addrWord);
        
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
                    add(tmp1.getBits(), tmp2.getBits(), false);
                    Word.copy(tmp1, cpu.mar);
                }else{
                    /* indirection addressing */
                    memory.memory(Signal.READ, a, cpu.mar);
                }
            }else{
                if(bitX==Bit.I){
                    /* indexing addressing */
                    memory.memory(Signal.READ, r4, tmp2);
                    add(a.getBits(), tmp2.getBits(), false);
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
            and(targetR, tmp1);
            break;
        case ADD:
            memory.memory(Signal.READ, ea, tmp1);
            add(targetR, tmp1);
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
            String data = cpu.io.readLine();
            cpu.clock = cpu.clock + CPU.TIME_IO;
            cpu.inputTime = cpu.inputTime + CPU.TIME_IO;
            /* System supports integer type only */
            int inputNum = 0;
            try{
                inputNum = Integer.parseInt(data);
            }catch(NumberFormatException e){
                throw new SystemException(Errors.USR_INVALID_DATA_TYPE);
            }
            Word.copy(Word.fromDecString(String.valueOf(
                    Math.abs(inputNum))), targetR);
            if(inputNum<0){
                twosComplement(targetR);
            }
            break;
        case WR:
            /* System supports only integer type */
            String output = null;
            if(targetR.isNegativeNumber()){
                Word.copy(targetR, tmp1);
                twosComplement(tmp1);//convert to positive
                output = "-"+Bit.toDecimal(tmp1.getBits());
            }else{
                output = String.valueOf(Bit.toDecimal(targetR.getBits()));
            }
            cpu.io.display(output);
            cpu.io.getLog().info("WR value: "+output+" (decimal)");
            cpu.clock = cpu.clock + CPU.TIME_IO;
            cpu.outputTime = cpu.outputTime + CPU.TIME_IO;
            break;
        case HLT:
            return false;//stop program
        /* Type III */
        case CLR:
            clear(targetR);
            break;
        case INC:
            increment(targetR);
            break;
        case COM:
            oneComplement(targetR);
            break;
        case BSW:
            swapBytes(targetR);
            break;
        case RTL:
            if(instruction.getBits()[11]==Bit.O){
                shiftLeft(targetR, 1);
            }else{
                shiftLeft(targetR, 2);
            }
            break;
        case RTR:
            if(instruction.getBits()[11]==Bit.O){
                shiftRight(targetR, 1);
            }else{
                shiftRight(targetR, 2);
            }
            break;
            
        /* Type IV */
        case NSK:
            //no operation
            break;
        case GTR:
            if(greaterThanZero(targetR)) cpu.incrementPC();
            break;
        case LSS:
            if(lessThanZero(targetR)) cpu.incrementPC();
            break;
        case NEQ:
            if(notEqualZero(targetR)){
                cpu.incrementPC();
            }
            break;
        case EQL:
            if(equalZero(targetR)) cpu.incrementPC();
            break;
        case GRE:
            if(greaterThanZero(targetR) || equalZero(targetR))
                cpu.incrementPC();
            break;
        case LSE:
            if(lessThanZero(targetR) || equalZero(targetR))
                cpu.incrementPC();
            break;
        case USK:
            cpu.incrementPC();
            break;
            
        }
        
        return true;
    }
    
    /**
     * Logical AND between dest and value then
     * store the result in dest.
     * @param dest
     * @param value
     */
    protected void and(Word dest, Word value){
        Bit destBits[] = dest.getBits();
        Bit valueBits[] = value.getBits();
        int sum = 0;
        for(int i=0;i<Word.SIZE;i++){
            sum = destBits[i].ordinal()+valueBits[i].ordinal();
            destBits[i] = sum==2?Bit.I:Bit.O; 
        }
    }
    
    /**
     * Binary addition operation that supports negative number adding 
     * in the form of 2's complement. The overflow bit will be ignored.
     * The reason to ignore bound checking is that there's no binary 
     * solution way to do that (We can use the decimal to check but that
     * doesn't reflect to the real system) 
     * In most programming languages, bound checking for addition is 
     * also ignored.
     * 
     * @param dest
     * @param addend
     */
    protected void add(Word dest, Word addend){
        add(dest.getBits(), addend.getBits(), true);
    }
    
    /**
     * set all bits to zero
     * @param word
     */
    protected void clear(Word word){
        Bit bits[] = word.getBits();
        for(int i=0;i<Word.SIZE;i++){
            bits[i] = Bit.O;
        }
    }
    
    /**
     * increment number by 1
     * @param word
     */
    protected void increment(Word word){
        word.increment();
    }
    
    /**
     * Flip-flop all bits
     * @param word
     */
    protected void oneComplement(Word word){
        Bit bits[] = word.getBits();
        for(int i=0;i<Word.SIZE;i++){
            bits[i] = bits[i].ordinal()==0?Bit.I:Bit.O;
        }
    }
    
    /**
     * Swap the first nibble with the last nibble
     * @param word
     */
    protected void swapBytes(Word word){
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
     * @param word
     * @return true if word value >0, otherwise false
     */
    protected boolean greaterThanZero(Word word){
        Bit bits[] = word.getBits();
        if(word.isNegativeNumber()) return false;
        for(int i=1;i<Word.SIZE;i++){
            if(bits[i]==Bit.I) return true;
        }
        return false;
    }
    
    /**
     * 
     * @param word
     * @return true if word value <0, otherwise false
     */
    protected boolean lessThanZero(Word word){
        return word.isNegativeNumber();
    }
    
    /**
     * 
     * @param word
     * @return true if word value !=0, otherwise false
     */
    protected boolean notEqualZero(Word word){
        return !equalZero(word);
    }
    
    /**
     * 
     * @param word
     * @return true if word value =0, otherwise false
     */
    protected boolean equalZero(Word word){
        Bit bits[] = word.getBits();
        for(int i=0;i<Word.SIZE;i++){
            if(bits[i]==Bit.I) return false;
        }
        return true;
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
     * add dest and values and store the result in dest
     * This method performs optional overflow checking
     * @param dest
     * @param values
     */
    protected void add(Bit dest[], Bit values[], boolean ignoreOverflow){
        if(values.length>dest.length){
            throw new SystemException(Errors.MEM_RANGE_OUT_OF_BOUND);
        }
        boolean isCarriedOver = false;
        int diff = dest.length-values.length;
        for(int i=dest.length-1,j=0;i>=0;i--,j++){
            int d = dest[i].ordinal();
            int v = (values.length>j)?values[i-diff].ordinal():0;
            if ( (d | v) == 1){//0|1
                if( (d & v) == 1){//1&1
                    if(isCarriedOver){ dest[i] = Bit.I;
                    }else{ dest[i] = Bit.O; }
                    isCarriedOver = true;
                }else{//01
                    if(isCarriedOver){
                        dest[i] = Bit.O;
                        isCarriedOver = true;
                    }else{
                        dest[i] = Bit.I;
                        isCarriedOver = false;
                    }
                }
            }else{
                if(isCarriedOver){
                    dest[i] = Bit.I;
                    isCarriedOver = false;
                }
            }
        }
        
        if(!ignoreOverflow && isCarriedOver){
            throw new SystemException(Errors.CPU_ARITHMETIC_OVERFLOW);
        }
    }
    
    
}
