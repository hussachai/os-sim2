package hussachai.osu.os2.system.unit;

import static hussachai.osu.os2.system.unit.Bit.I;
import static hussachai.osu.os2.system.unit.Bit.O;
import hussachai.osu.os2.system.error.LogicException;
import hussachai.osu.os2.system.unit.Bit;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

/**
 * 
 * @author hussachai
 *
 */
public class BitTest {
    
    @Test
    public void testToString(){
        Assert.assertEquals("1", Bit.I.toString());
        Assert.assertEquals("0", Bit.O.toString());
    }
    
    @Test
    public void testToChar(){
        Assert.assertEquals('1', Bit.I.toChar());
        Assert.assertEquals('0', Bit.O.toChar());
    }
    
    
    /**
     * Test string to bits conversion
     */
    @Test
    public void testFromBinString(){
        
        Bit expected[] = new Bit[]{O,O,O,I,I,I};
        Bit actual[] = Bit.fromBinString("000111", 6);
        
        Assert.assertTrue(Arrays.equals(expected, actual));
        
        /* If the specified values are less than size, the size will be applied.
         * The bits will be padded with all 0 bits.
         */
        Assert.assertEquals(12, Bit.fromBinString("111", 12).length);
        
        /* If the specified values are larger than size, the specified value length
         * will be used instead. 
         */
        Assert.assertEquals(3, Bit.fromBinString("111", 1).length);
        
        /* Test invalid bit */
        try{
            Bit.fromBinString("f", 0);
            Assert.fail("Unchecked invalid characters");
        }catch(LogicException e){}
        
        /* Test invalid bit mixed with valid bit */
        try{
            Bit.fromBinString("100H002", 0);
            Assert.fail("Unchecked invalid characters");
        }catch(LogicException e){}
        
        /* Input string cannot be empty*/
        try{
            Bit.fromBinString(null, 0);
            Assert.fail("Unchecked input error");
        }catch(LogicException e){}
        try{
            Bit.fromBinString("   ", 0);
            Assert.fail("Unchecked input error");
        }catch(LogicException e){}
        
    }
    
    @Test
    public void testToBinString(){
        
        Assert.assertEquals("1", Bit.toBinString(new Bit[]{I}));
        Assert.assertEquals("111000", Bit.toBinString(new Bit[]{I,I,I,O,O,O}));
        
        try{
            Bit.toBinString((Bit[])null);
            Assert.fail("Unchecked input error");
        }catch(LogicException e){}
        
        try{
            Bit.toBinString(new Bit[2]);
            Assert.fail("Unchecked input error");
        }catch(LogicException e){}
        
    }
    
    @Test
    public void testFromDecString(){
        
        for(int i=0;i<20;i++){
            int randNum = (int)(Math.random()*1000);
            Assert.assertEquals(randNum, Bit.toDecimal(
                    Bit.fromDecString(String.valueOf(randNum), 0)));
        }
        
        /* Test invalid bit */
        try{
            Bit.fromDecString("TEST", 0);
            Assert.fail("Unchecked invalid characters");
        }catch(LogicException e){}
        
        /* Test invalid bit mixed with valid bit */
        try{
            Bit.fromDecString("10E00", 0);
            Assert.fail("Unchecked invalid characters");
        }catch(LogicException e){}
        
        try{
            Bit.fromDecString(null, 0);
            Assert.fail("Unchecked input error");
        }catch(LogicException e){}
        
        try{
            Bit.fromDecString(" ", 0);
            Assert.fail("Unchecked input error");
        }catch(LogicException e){}
        
    }
    
    @Test
    public void testToDecString(){
        Assert.assertEquals("1", Bit.toDecString(new Bit[]{I}));
        Assert.assertEquals("56", Bit.toDecString(new Bit[]{I,I,I,O,O,O}));
        
        try{
            Bit.toDecString((Bit[])null);
            Assert.fail("Unchecked input error");
        }catch(LogicException e){}
        
        try{
            Bit.toDecString(new Bit[2]);
            Assert.fail("Unchecked input error");
        }catch(LogicException e){}
    }
    
    @Test
    public void testToDecimal(){
        
        Bit bits[] = new Bit[]{I};
        
        Assert.assertEquals(1, Bit.toDecimal(bits));
        
        bits = new Bit[]{O};
        Assert.assertEquals(0, Bit.toDecimal(bits));
        
        bits = new Bit[]{I,O,I,I,I,O,I,I};
        
        Assert.assertEquals(187, Bit.toDecimal(bits));
        
        try{
            Bit.toDecimal((Bit[])null);
            Assert.fail("Unchecked input error");
        }catch(LogicException e){}
        
    }
    
    
    @Test
    public void testFromHexString(){
        
        Assert.assertEquals(0, 
                Bit.toDecimal(Bit.fromHexString("0", 0)));
        
        Assert.assertEquals(1, 
                Bit.toDecimal(Bit.fromHexString("1", 0)));
        
        Assert.assertEquals(15, 
                Bit.toDecimal(Bit.fromHexString("F", 0)));
        
        /* 6F156(hex) => 454998(dec) */
        Assert.assertEquals(454998, 
                Bit.toDecimal(Bit.fromHexString("6F156", 0)));
        Assert.assertEquals(454998, 
                Bit.toDecimal(Bit.fromHexString("6f156", 0)));
        
        /* Test invalid bit */
        try{
            Bit.fromHexString("G", 12);
            Assert.fail("Unchecked invalid characters");
        }catch(LogicException e){}
        
        /* Test invalid bit mixed with valid bit */
        try{
            Bit.fromHexString("FFGF", 12);
            Assert.fail("Unchecked invalid characters");
        }catch(LogicException e){}
    
        try{
            Bit.fromHexString(null, 0);
            Assert.fail("Unchecked input error");
        }catch(LogicException e){}
        
        try{
            Bit.fromHexString(" ", 0);
            Assert.fail("Unchecked input error");
        }catch(LogicException e){}
    }
    
    @Test
    public void testToHexString(){
        Assert.assertEquals("1", Bit.toHexString(new Bit[]{I}));
        Assert.assertEquals("38", Bit.toHexString(new Bit[]{I,I,I,O,O,O}));
        
        try{
            Bit.toHexString((Bit[])null);
            Assert.fail("Unchecked input error");
        }catch(LogicException e){}
        
        try{
            Bit.toHexString(new Bit[2]);
            Assert.fail("Unchecked input error");
        }catch(LogicException e){}
    }
    
}
