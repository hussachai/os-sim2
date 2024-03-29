package hussachai.osu.os2.system.io;

import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.storage.Memory.Partition;
import hussachai.osu.os2.system.storage.Memory.Signal;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * I/O Handler is introduced in Phase II because Phase II is the extension
 * of Phase I. So, I don't want to remove the standard I/O which is screen
 * and keyboard that is required in Phase I but I want to add more 2 handlers
 * into the system - in-memory input and in-memory output. 
 * 
 * @author hussachai
 *
 */
public class IOHandlers {
    
    /**
     * Interface that all I/O input peripherals must implement
     * @author hussachai
     *
     */
    public static interface Input {
        public Word read();
    }
    
    /**
     * Interface that all I/O output peripherals must implement
     * @author hussachai
     *
     */
    public static interface Output {
        public void write(Word data);
    }
    
    /**
     * Standard input is key event from keyboard
     * @author hussachai
     *
     */
    public static class StandardInputHandler implements Input {
        
        @Override
        public Word read() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    System.in));
            String input = null;
            try {
                input = reader.readLine();
                int inputNum = 0;
                try {
                    inputNum = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    throw new SystemException(Errors.USR_INVALID_DATA_TYPE);
                }
                String valueStr = String.valueOf(Math.abs(inputNum));
                Word value = Word.fromDecString(valueStr);;
                if(inputNum<0){
                  Word.twosComplement(value);
                }
                return value;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * Standard output is screen. It will convert hexadecimal value
     * to decimal automatically before displaying.
     * 
     * @author hussachai
     *
     */
    public static class StandardOutputHandler implements Output {

        @Override
        public void write(Word data) {
            /* System supports only integer type */
          String value = null;
          if(data.isNegativeNumber()){
              Word tmp = new Word();
              Word.copy(data, tmp);
              Word.twosComplement(tmp);//convert to positive
              value = "-"+tmp.toDecimal();
          }else{
              value = String.valueOf(Bit.toDecimal(data.getBits()));
          }
          
          System.out.println(value);
          
        }
        
    }
    
    /**
     * The base class for in-memory I/O
     * @author hussachai
     *
     */
    public static abstract class MemoryHandler {
        
        private Memory memory;
        
        private Partition partition;
        
        private int memoryIndex;

        public Memory getMemory() {
            return memory;
        }
        
        public void setMemory(Memory memory) {
            this.memory = memory;
        }

        public Partition getPartition() {
            return partition;
        }

        public void setPartition(Partition partition) {
            this.partition = partition;
        }

        public int getMemoryIndex() {
            return memoryIndex;
        }

        public void setMemoryIndex(int memoryIndex) {
            this.memoryIndex = memoryIndex;
        }
        
    }
    
    /**
     * In-memory input handler
     * @author hussachai
     *
     */
    public static class MemoryInputHandler extends MemoryHandler 
        implements Input {
        
        @Override
        public Word read() {
            Word data = new Word();
            getMemory().memory(getPartition(), Signal.READ, 
                    getMemoryIndex(), data);
            return data;
        }
    }
    
    /**
     * In-memory output handler
     * @author hussachai
     *
     */
    public static class MemoryOutputHandler extends MemoryHandler 
        implements Output {
        
        @Override
        public void write(Word data) {
            getMemory().memory(getPartition(), Signal.WRIT, 
                    getMemoryIndex(), data);
        }
        
    }
    
}
