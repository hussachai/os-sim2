package hussachai.osu.os2.system.storage;

import java.util.Arrays;

import junit.framework.Assert;
import hussachai.osu.os2.system.error.LogicException;
import hussachai.osu.os2.system.unit.Word;

import org.junit.Test;

/**
 * 
 * @author hussachai
 *
 */
public class BufferTest {
    
    @Test
    public void testAll(){
        
        Word word1 = Word.fromBinString("101");
        Word word2 = Word.fromBinString("110");
        Word word3 = Word.fromBinString("111");
        
        Buffer buffer = new Buffer(3);
        buffer.add(word1);
        buffer.add(word2);
        buffer.add(word3);
        
        if(!buffer.isFull()){
            Assert.fail("Buffer size check failed");
        }
        try{
            buffer.add(new Word());
            Assert.fail("Buffer adding constraint failed");
        }catch(LogicException e){}
        
        Word words[] = buffer.flush();
        Assert.assertTrue(Arrays.equals(words[0].getBits(), word1.getBits())); 
        Assert.assertTrue(Arrays.equals(words[1].getBits(), word2.getBits()));
        Assert.assertTrue(Arrays.equals(words[2].getBits(), word3.getBits()));
        
        if(buffer.isFull()){
            Assert.fail("Buffer size check failed");
        }
        
        
    }
}
