package hussachai.osu.os2.system;

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
public class CPUTest {
	
	private CPU cpu;
	
	@Before
	public void beforeTest(){
		cpu = new CPU();
	}
	
	@Test
	public void testRegistersInit(){
		
		Assert.assertEquals(10, cpu.registers.length);
		
		for(Word register: cpu.registers){
			Assert.assertNotNull(register);
			Assert.assertEquals(Word.SIZE, register.getBits().length);
		}
		Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,O,O,O}, 
				cpu.registers[0].getBits()));
		Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,O,O,I}, 
				cpu.registers[1].getBits()));
		
		for(int i=2;i<cpu.registers.length;i++){
			Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,O,O,O}, 
					cpu.registers[i].getBits()));
		}
	}
	
	@Test 
	public void testIncrementPC(){
		Word pc = cpu.registers[CPU.R_PC];
		Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,O,O,O}, pc.getBits()));
		
		cpu.incrementPC();
		Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,O,O,I}, pc.getBits()));
		cpu.incrementPC();
		cpu.incrementPC();
		Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,O,I,I}, pc.getBits()));
		
		cpu.registers[CPU.R_PC] = new Word(new Bit[]{I,I,I,I,I,I,I,I,I,I,I,I});
		try{
			cpu.incrementPC();
			Assert.fail("Unchecked PC overflow");
		}catch(SystemError e){
			if(e.getErrorCode()!=Errors.PC_OVERFLOW){
				Assert.fail("Invalid error code");
			}
		}
	}
	
	@Test 
	public void testIncrement(){
		/* test incrementing from 1 to 4 */
		Word word = Word.fromBinString("000000000001");
		cpu.increment(word);
		Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,O,I,O}, word.getBits()));
		cpu.increment(word);
		cpu.increment(word);
		Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,I,O,O}, word.getBits()));
		
		/* test incrementing on negative number */
		word = Word.fromBinString("111111111101");//-3
		cpu.increment(word);
		Assert.assertTrue(Arrays.equals(new Bit[]{I,I,I,I,I,I,I,I,I,I,I,O}, word.getBits()));//-2
		cpu.increment(word);
		Assert.assertTrue(Arrays.equals(new Bit[]{I,I,I,I,I,I,I,I,I,I,I,I}, word.getBits()));//-1
		cpu.increment(word);
		Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,O,O,O}, word.getBits()));//0
		cpu.increment(word);
		Assert.assertTrue(Arrays.equals(new Bit[]{O,O,O,O,O,O,O,O,O,O,O,I}, word.getBits()));//1
		
		/* test incrementing over the maximum */
		word = Word.fromBinString("011111111111");
		try{
			cpu.increment(word);
			Assert.fail("Unchecked number overflow");
		}catch(SystemError e){
			Assert.assertEquals(Errors.ARITHMETIC_OVERFLOW, e.getErrorCode());
		}
		
	}
	
	
	
	@Test
	public void testTwosComplement(){
		
	}
	
	@Test
	public void testAdd(){
		
		Bit value1[] = Word.fromDecString("15").getBits();
		Bit value2[] = Word.fromDecString("23").getBits();
		
	}
	
	public static void main(String[] args) {
		
		CPU cpu = new CPU();
		Word word1 = Word.fromDecString("38");
		System.out.println("Word1:"+word1);
		Word word2 = new Word();
		System.arraycopy(word1.getBits(), 0, word2.getBits(), 0, Word.SIZE);
		cpu.twosComplement(word2);
		System.out.println("Word2:"+word2);
		
		cpu.increment(word2);
		
		cpu.add(word2.getBits(), word1.getBits(), true);
		
		System.out.println("Result:"+word2);
	}
}
