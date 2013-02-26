package hussachai.osu.os2.system;

import hussachai.osu.os2.system.cpu.CPU;
import hussachai.osu.os2.system.error.ErrorHandler;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.storage.MemoryManager;

import org.apache.commons.lang3.StringUtils;



/**
 * I should not name this class as System because it will collide with 
 * the java.lang.System class that is imported by default.
 * 
 * @author hussachai
 *
 */
public class TheSystem {
	
	
	private CPU cpu;
	
	private Memory memory;
	
	private MemoryManager memoryManager;
	
	private Loader loader;
	
	private ErrorHandler errorHandler;
	
	public TheSystem(){
		cpu = new CPU();
		memory = new Memory();
		memoryManager = new MemoryManager(memory);
		loader = new Loader(this);
		errorHandler = new ErrorHandler();
	}
	
	public void start(){
		
		loader.loadUserProgram();
	}
	
	
	public static void main(String[] args) {
		System.out.println(StringUtils.trim("Hello World"));
	}
	
}


