package hussachai.osu.os2.system.cpu;

import static hussachai.osu.os2.system.unit.Bit.I;
import static hussachai.osu.os2.system.unit.Bit.O;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

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
	
}
