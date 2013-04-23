package hussachai.osu.os2.system.io;

import hussachai.osu.os2.system.io.IOManager.IOType;
import hussachai.osu.os2.system.unit.Word;

/**
 * This exception is used for I/O interrupt signal.
 * When the CPU finds that the program requests I/O whether READ or WRITE,
 * it will throw this exception and interrupt will be handled by
 * I/O manager using the I/O handle that is associated with that program.
 * If none I/O handle is specified, the standard I/O will be used instead.
 * 
 * @author hussachai
 *
 */
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