package hussachai.osu.os2.system.unit;

import hussachai.osu.os2.system.error.LogicException;

/**
 * ID is the special Word that has only one purpose and that
 * is to increment the last number by one. It shall not be
 * modified. The ID also be used as the factory for creating
 * the new generated ID. 
 * 
 * This object can be both generator or regular ID.
 * If it's created by new ID(), it's generator.
 * If it's created by calling nextSequence method from generator,
 * it's regular ID and it's immutable.
 * 
 * @author hussachai
 *
 */
public class ID extends Word {
    
    private Word sequence;
    
    private boolean modifiable = false;
    
    private boolean generator = false;
    
    /**
     * Create new ID generator
     */
    public ID(){
        this.generator = true;
        sequence = new Word();
    }
    
    private ID(boolean modifiable){
        super();
        this.modifiable = modifiable;
    }
    
    /**
     * Generate new ID based on the last sequence. 
     * The result will be new ID instance because 
     * ID is immutable. After it's generated, it cannot
     * be modified.
     * @return
     */
    public ID nextSequence(){
        if(!this.generator){
            throw new LogicException("This ID instance is not a generator");
        }
        ID id = new ID(true);
        /* Actually, the synchronized doesn't need in this system */
        synchronized(sequence){
            Word.increment(sequence);
            Word.copy(sequence, id);
        }
        id.modifiable = false;
        return id;
    }
    
    
    @Override
    public Bit[] getBits() {
        if(modifiable){
            return super.getBits();
        }else{
            throw new LogicException("Modification is not allowed. Thus, getBits()");
        }
    }
    
}
