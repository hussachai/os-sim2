package hussachai.osu.os2.system.cpu;

import static hussachai.osu.os2.system.unit.Bit.I;
import static hussachai.osu.os2.system.unit.Bit.O;
import hussachai.osu.os2.system.TheSystem;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author hussachai
 *
 */
public class ALUnitTest {
    
    private Memory memory;
    private CPU cpu;
    private ALUnit alUnit;
    
    @Before
    public void beforeTest(){
        TheSystem system = new TheSystem();
        memory = system.getMemory();
        cpu = system.getCPU();
        alUnit = cpu.alUnit;
    }
    
    @Test
    public void testClearBits(){
        Word zeroes = Word.fromBinString("000000000000");
        Word word = Word.fromBinString("111111111111");
        alUnit.clear(word);
        Assert.assertTrue(Arrays.equals(zeroes.getBits(), word.getBits()));
        word = Word.fromBinString("010101010101");
        alUnit.clear(word);
        Assert.assertTrue(Arrays.equals(zeroes.getBits(), word.getBits()));
    }
    
    
    
    @Test 
    public void testIncrement(){
        /* test incrementing from 1 to 4 */
        Word word = Word.fromBinString("000000000001");
        alUnit.increment(word);
        Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,O,I,O}, word.getBits()));
        alUnit.increment(word);
        alUnit.increment(word);
        Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,I,O,O}, word.getBits()));
        
        /* test incrementing on negative number */
        word = Word.fromBinString("111111111101");//-3
        alUnit.increment(word);
        Assert.assertTrue(Arrays.equals(new Bit[]{I,I,I,I,I,I,I,I,I,I,I,O}, word.getBits()));//-2
        alUnit.increment(word);
        Assert.assertTrue(Arrays.equals(new Bit[]{I,I,I,I,I,I,I,I,I,I,I,I}, word.getBits()));//-1
        alUnit.increment(word);
        Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,O,O,O}, word.getBits()));//0
        alUnit.increment(word);
        Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,O,O,I}, word.getBits()));//1
        
    }
    
    @Test
    public void testOneComplement(){
        //TODO: add test
    }
    
    @Test
    public void testSwapBytes(){
        //TODO: add test
    }
    
    @Test
    public void testShiftLeft(){
        
    }
    
    @Test
    public void testShiftRight(){
        
    }
    
    
    
    @Test
    public void testTwosComplement(){
        Word word = Word.fromBinString("111111101110");
        alUnit.twosComplement(word);
        System.out.println(Bit.toDecimal(word.getBits()));
    }
    
    @Test
    public void testAdd(){
        
        Bit one[] = Word.fromBinString("1").getBits();
        Bit value1[] = Word.fromDecString("15").getBits();
        Bit value2[] = Word.fromDecString("23").getBits();
        alUnit.add(value1, value2, false);
        Assert.assertTrue(Arrays.equals(Word.fromDecString("38").getBits(), value1));
        
        value1 = Word.fromBinString("011111111111").getBits();
        alUnit.add(value1, one, false);
    }
    
    
}
