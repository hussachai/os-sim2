package hussachai.osu.os2.system.storage;

import hussachai.osu.os2.system.error.LogicException;
import hussachai.osu.os2.system.unit.Word;

/**
 * 
 * Buffer is the temporary memory. It can be used by Loader
 * to store 2-4 words before flushing all data to memory.
 * Buffer is used in the real device to increase the performance. 
 * 
 * Buffer is not thread-safe
 * 
 * @author hussachai
 *
 */
public class Buffer {
    
    private int currentSize = 0;
    private int maxSize = 0;
    private Word words[];
    
    /**
     * 
     * @param size in word
     */
    public Buffer(int maxSize){
        this.maxSize = maxSize;
        words = new Word[maxSize];
    }
    
    /**
     * test whether the buffer is full or not
     * this operation is relatively cheap, calling it in loop doesn't 
     * effect much performance.
     * @return
     */
    public boolean isFull(){
        if(currentSize>=maxSize){
            return true;
        }
        return false;
    }
    
    /**
     * 
     * @param word
     */
    public void add(Word word){
        if(isFull()){
            throw new LogicException("Buffer is full");
        }
        words[currentSize] = word;
        currentSize++;
    }
    
    public Word[] flush(){
        Word outs[] = new Word[currentSize];
        for(int i=0;i<currentSize;i++){
            outs[i] = words[i];
        }
        currentSize = 0;
        return outs;
    }
    
    /**
     * @return number of data in buffer
     */
    public int getSize(){
        return currentSize;
    }
    
}
