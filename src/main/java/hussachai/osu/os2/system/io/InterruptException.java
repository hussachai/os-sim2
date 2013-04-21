package hussachai.osu.os2.system.io;

import hussachai.osu.os2.system.io.IOManager.IOType;
import hussachai.osu.os2.system.unit.Word;

public class InterruptException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private IOType ioType;
    
    private Word targetRegister;
    
    /**
     * 
     * @param ioType
     * @param targetRegister
     */
    public InterruptException(IOType ioType, Word targetRegister){
        this.ioType = ioType;
        if(ioType==IOType.Read){
            this.targetRegister = targetRegister;            
        }else{
            Word word = new Word();
            Word.copy(targetRegister, word);
            this.targetRegister = word;
        }
    }
    
    public IOType getIOType() {
        return ioType;
    }
    
    public Word getTargetRegister() {
        return targetRegister;
    }
    
}