package hussachai.osu.os2.system;

import hussachai.osu.os2.system.Loader.LoaderContext;
import hussachai.osu.os2.system.cpu.CPU;
import hussachai.osu.os2.system.error.ErrorHandler;
import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.io.InputOutput;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;

import java.io.File;
import java.math.BigInteger;

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
	
	private Loader loader;
	
	private InputOutput io;
	
	private ErrorHandler errorHandler;
	
	public TheSystem(){
		io = new InputOutput();
		memory = new Memory(this);
		cpu = new CPU(this);
		loader = new Loader(this);
		errorHandler = new ErrorHandler(this);
	}
	
	public void start(String[] fileNames){
		
		File files[] = new File[fileNames.length];
		
		for(int i=0;i<files.length;i++){
			File file = new File(fileNames[i]);
			if(!file.exists()){
				throw new SystemException(Errors.IO_FILE_NOT_FOUND);
			}
			files[i] = file;
		}
		
		for(File file: files){
			
			io.getLog().clearInfo();
			
			LoaderContext context = loader.loader(file);
			/* assign the last instruction word as start address word*/ 
			Word pc = context.instructionWord;
			
			if(context.traceSwitch==Bit.I){
				io.getLog().clearTrace();
				/* write trace header */
				StringBuilder str = new StringBuilder();
				str.append(StringUtils.center("[PC]", 12, ' ')).append("\t");
				str.append("[Instruction]").append("\t");
				str.append(StringUtils.center("[R]", 12, ' ')).append("\t");
				str.append(StringUtils.center("[EA]", 12, ' ')).append("\t");
				str.append("[(R) before]").append("\t");
				str.append("[(EA) before]").append("\t");
				str.append("[(R) after] ").append("\t");
				str.append("[(EA) after]").append("\t");
				io.getLog().trace(str.toString());
			}
			
			io.getLog().info("Cumulative Job ID: 1");
			
			try{
				
				cpu.cpu(pc, context.traceSwitch);
			}finally{
				
				String clockHex = new BigInteger(String.valueOf(
						cpu.getClock()), 10).toString(16);
				int inputTime = cpu.getInputTime();
				int outputTime = cpu.getOutputTime();
				io.getLog().info("Clock value: "+clockHex+" (hex)");
				io.getLog().info("Input time: "+inputTime+" (decimal)");
				io.getLog().info("Output time: "+outputTime+" (decimal)");
			}
		}
	}
	
	public CPU getCPU() { return cpu; }
	
	public Memory getMemory() { return memory; }

	public InputOutput getIO() { return io; }

	public ErrorHandler getErrorHandler() { return errorHandler; }
	
	public static void main(String[] args) {
		
		args = new String[]{"programs/example"};
		
		TheSystem system = new TheSystem();
		InputOutput io = system.getIO();
		
		try{
			system.start(args);
			io.getLog().info("Terminated successfully.");
		}catch(SystemException e){
			system.errorHandler.errorHandler(e.getErrorCode());
		}catch(Throwable e){
			system.errorHandler.errorHandler(Errors.SYS_INTERNAL_ERROR);
		}
	}
	
}
