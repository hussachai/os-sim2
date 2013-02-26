package hussachai.osu.os2.system.unit;

import static hussachai.osu.os2.system.unit.Bit.I;
import static hussachai.osu.os2.system.unit.Bit.O;
import hussachai.osu.os2.system.error.LogicException;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;
import junit.framework.Assert;

import org.junit.Test;

/**
 * 
 * @author hussachai
 *
 */
public class WordTest {
	
	@Test
	public void testWordConstuction(){
		
		Word word = null;
		try{
			word = new Word(new Bit[]{I,I,I,I,I,I,O,O,O,O,O,O});
		}catch(LogicException e){
			Assert.fail("Correct size is not accepted");
		}
		
		try{
			word = new Word(new Bit[]{I});
			Assert.fail("Wrong size is accepted");
		}catch(LogicException e){}
		
		word = new Word();
		Assert.assertEquals(12, word.getBits().length);
		Assert.assertNotNull(word.getBits()[0]);
		Assert.assertNotNull(word.getBits()[11]);
		for(Bit bit: word.getBits()){
			/* make sure that all bits are 0 */
			Assert.assertEquals(O, bit);
		}
	}
	
	
	@Test
	public void testFromBinString(){
		Word word = null;
		try{
			word = Word.fromBinString("111111111111");
			Assert.assertNotNull(word);
			Assert.assertEquals(12, word.getBits().length);
		}catch(LogicException e){
			Assert.fail();
		}
		
		/* Always returns 12 bits */
		Assert.assertEquals(12, Word.fromBinString("1").getBits().length);
		
		/* Input length exceed the valid word size */
		try{
			word = Word.fromBinString("1111111111111");
			Assert.fail("Exceed word is allowed");
		}catch(LogicException e){}
		
		
		/* Test invalid bit */
		try{
			Word.fromBinString("G");
			Assert.fail("Unchecked invalid characters");
		}catch(LogicException e){}
		
		/* Test invalid bit mixed with valid bit */
		try{
			Word.fromBinString("11G1");
			Assert.fail("Unchecked invalid characters");
		}catch(LogicException e){}
		
		try{
			Word.fromBinString(null);
			Assert.fail("Unchecked input error");
		}catch(LogicException e){}
		
		try{
			Word.fromBinString(" ");
			Assert.fail("Unchecked input error");
		}catch(LogicException e){}
	}
	
	@Test
	public void testFromDecString(){
		Word word = null;
		try{
			word = Word.fromDecString("4095");
			Assert.assertNotNull(word);
			Assert.assertEquals(12, word.getBits().length);
		}catch(LogicException e){
			Assert.fail();
		}
		
		/* Always returns 12 bits */
		Assert.assertEquals(12, Word.fromDecString("1").getBits().length);
		
		try{
			word = Word.fromDecString("4096");
			Assert.fail("Exceeded size is allowed");
		}catch(LogicException e){}
		
		/* Test invalid bit */
		try{
			Word.fromDecString("A");
			Assert.fail("Unchecked invalid characters");
		}catch(LogicException e){}
		
		/* Test invalid bit mixed with valid bit */
		try{
			Word.fromDecString("2A3");
			Assert.fail("Unchecked invalid characters");
		}catch(LogicException e){}
		
		try{
			Word.fromDecString(null);
			Assert.fail("Unchecked input error");
		}catch(LogicException e){}
		
		try{
			Word.fromDecString(" ");
			Assert.fail("Unchecked input error");
		}catch(LogicException e){}
	}
	
	@Test
	public void testFromHexString(){
		Word word = null;
		try{
			word = Word.fromHexString("FFF");
			Assert.assertNotNull(word);
			Assert.assertEquals(12, word.getBits().length);
		}catch(LogicException e){
			Assert.fail();
		}
		
		/* Always returns 12 bits */
		Assert.assertEquals(12, Word.fromHexString("1").getBits().length);
		
		try{
			word = Word.fromHexString("1000");
			Assert.fail("Exceeded size is allowed");
		}catch(LogicException e){}
		
		/* Test invalid bit */
		try{
			Word.fromHexString("G");
			Assert.fail("Unchecked invalid characters");
		}catch(LogicException e){}
		
		/* Test invalid bit mixed with valid bit */
		try{
			Word.fromHexString("2G3");
			Assert.fail("Unchecked invalid characters");
		}catch(LogicException e){}
		
		try{
			Word.fromHexString(null);
			Assert.fail("Unchecked input error");
		}catch(LogicException e){}
		
		try{
			Word.fromHexString(" ");
			Assert.fail("Unchecked input error");
		}catch(LogicException e){}
	}
}
