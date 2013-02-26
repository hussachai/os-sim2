package hussachai.osu.os2.system.cpu;

import static hussachai.osu.os2.system.unit.Bit.I;
import static hussachai.osu.os2.system.unit.Bit.O;
import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemError;
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
public class ALUTest {
	
	private CPU cpu;
	private ALU alu;
	
	@Before
	public void beforeTest(){
		cpu = new CPU();
		alu = new ALU(cpu);
	}
	
	@Test
	public void testClearBits(){
		Word zeroes = Word.fromBinString("000000000000");
		Word word = Word.fromBinString("111111111111");
		alu.clear(word);
		Assert.assertTrue(Arrays.equals(zeroes.getBits(), word.getBits()));
		word = Word.fromBinString("010101010101");
		alu.clear(word);
		Assert.assertTrue(Arrays.equals(zeroes.getBits(), word.getBits()));
	}
	
	
	
	@Test 
	public void testIncrement(){
		/* test incrementing from 1 to 4 */
		Word word = Word.fromBinString("000000000001");
		alu.increment(word);
		Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,O,I,O}, word.getBits()));
		alu.increment(word);
		alu.increment(word);
		Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,I,O,O}, word.getBits()));
		
		/* test incrementing on negative number */
		word = Word.fromBinString("111111111101");//-3
		alu.increment(word);
		Assert.assertTrue(Arrays.equals(new Bit[]{I,I,I,I,I,I,I,I,I,I,I,O}, word.getBits()));//-2
		alu.increment(word);
		Assert.assertTrue(Arrays.equals(new Bit[]{I,I,I,I,I,I,I,I,I,I,I,I}, word.getBits()));//-1
		alu.increment(word);
		Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,O,O,O}, word.getBits()));//0
		alu.increment(word);
		Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,O,O,I}, word.getBits()));//1
		
		/* test incrementing over the maximum */
		word = Word.fromBinString("011111111111");
		try{
			alu.increment(word);
			Assert.fail("Unchecked number overflow");
		}catch(SystemError e){
			Assert.assertEquals(Errors.ARITHMETIC_OVERFLOW, e.getErrorCode());
		}
		
	}
	
	@Test
	public void testOneComplement(){
		//TODO: add test
	}
	
	@Test
	public void testByteSwap(){
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
		
	}
	
	@Test
	public void testAdd(){
		
		Bit value1[] = Word.fromDecString("15").getBits();
		Bit value2[] = Word.fromDecString("23").getBits();
		
	}
	
	
	@Test 
	public void testIncrementPC(){
		Word pc = cpu.registers[CPU.R_PC];
		Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,O,O,O}, pc.getBits()));
		
		alu.incrementPC();
		Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,O,O,I}, pc.getBits()));
		alu.incrementPC();
		alu.incrementPC();
		Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,O,I,I}, pc.getBits()));
		
		cpu.registers[CPU.R_PC] = new Word(new Bit[]{I,I,I,I,I,I,I,I,I,I,I,I});
		try{
			alu.incrementPC();
			Assert.fail("Unchecked PC overflow");
		}catch(SystemError e){
			if(e.getErrorCode()!=Errors.PC_OVERFLOW){
				Assert.fail("Invalid error code");
			}
		}
	}
}
