package hussachai.osu.os2.system.cpu;

import static hussachai.osu.os2.system.unit.Bit.I;
import static hussachai.osu.os2.system.unit.Bit.O;
import hussachai.osu.os2.system.TheSystem;
import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class CPUTest {
    
    private Memory memory;
    private CPU cpu;
    
    @Before
    public void beforeTest(){
        TheSystem system = new TheSystem();
        memory = system.getMemory();
        cpu = system.getCPU();
        memory.init(system);
        cpu.init(system);
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
        }catch(SystemException e){
            if(e.getErrorCode()!=Errors.MEM_PC_OVERFLOW){
                Assert.fail("Invalid error code");
            }
        }
    }
    
}
