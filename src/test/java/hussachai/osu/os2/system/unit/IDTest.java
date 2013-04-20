package hussachai.osu.os2.system.unit;

import junit.framework.Assert;
import hussachai.osu.os2.system.error.LogicException;

import org.junit.Test;

public class IDTest {

    @Test
    public void testAll(){
        
        ID idGenerator = new ID();//create new generator
        
        try{
            idGenerator.getBits();
            Assert.fail("ID must be unmodifiable");
        }catch(LogicException e){
            Assert.assertTrue("Modification is not allowed. Thus, getBits()"
                    .equals(e.getMessage()));
        }
        
        try{
            idGenerator.increment();
            Assert.fail("ID must be unmodifiable");
        }catch(LogicException e){
            Assert.assertTrue("Modification is not allowed"
                    .equals(e.getMessage()));
        }
        
        ID id = null;
        for(int i=1;i<10;i++){
            id = idGenerator.nextSequence();
            Assert.assertTrue(id.toDecimal()==i);
        }
        
        id = idGenerator.nextSequence();
        Assert.assertTrue(id.toDecimal()==10);
        
        try{
            id.increment();
            Assert.fail("ID must be unmodifiable");
        }catch(LogicException e){
            Assert.assertTrue("Modification is not allowed"
                    .equals(e.getMessage()));
        }
        
        try{
            id.nextSequence();
            Assert.fail("This ID is not a generator");
        }catch(LogicException e){
            Assert.assertTrue("This ID instance is not a generator"
                    .equals(e.getMessage()));
        }
        
    }
}
