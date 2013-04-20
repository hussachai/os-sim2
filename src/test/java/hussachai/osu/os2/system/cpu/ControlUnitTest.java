package hussachai.osu.os2.system.cpu;

import hussachai.osu.os2.system.TheSystem;
import hussachai.osu.os2.system.storage.Memory;

import org.junit.Before;
import org.junit.Test;

public class ControlUnitTest {
    
    private Memory memory;
    private CPU cpu;
    private ControlUnit controlUnit;
    
    @Before
    public void beforeTest(){
        TheSystem system = new TheSystem();
        memory = system.getMemory();
        cpu = system.getCPU();
        controlUnit = cpu.controlUnit;
    }
    
    @Test
    public void testNothing(){
        
    }
    
}
