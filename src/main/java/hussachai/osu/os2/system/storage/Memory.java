/*
 * Name: Hussachai Puripunpinyo
 * Course No.:  CS 5323
 * Assignment title: PHASE I (March 5)
 * TA's Name: 
 *  - Alireza Boloorchi
 *  - Sukanya Suwisuthikasem
 * Global variables:
 *  - addresses (The array of word (12 bits) unit. It has 4096 words)
 *  - io (The reference to InputOutput)
 *  
 *  Brief Description:
 *  Memory is used for accessing the data. It's the main storage of the system.
 *  The keeps the instruction and data in the same array. The system that needs
 *  to access memory must access it via the memory method that supports 
 *  3 different operations - READ, WRITE, and DUMP.
 *  
 */
package hussachai.osu.os2.system.storage;

import hussachai.osu.os2.system.TheSystem;
import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.LogicException;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.io.InputOutput;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;

/**
 * Memory manager
 * 
 * @author hussachai
 *
 */
public class Memory {
	
	/** 
	 * Memory operation enum
	 * @author hussachai
	 *
	 */
	public static enum Signal {
		READ, WRIT, DUMP
	}
	
	/**
	 * the addresses store 4096 words = 2^12
	 * 
	 */
	public static final int SIZE = 4096;
	
	private Word addresses[] = new Word[SIZE];
	
	private InputOutput io;
	
	public Memory(TheSystem system){
		this.io = system.getIO();
		
		for(int i=0; i<SIZE;i++){
			addresses[i] = new Word();
		}
	}
	
	/**
	 * 
	 * [Specification required method]
	 * 
	 * @param signal
	 * @param memoryAddr (EA)
	 * @param variable - may be a register used by the CPU or may be a buffer used by the LOADER
	 */
	public void memory(Signal signal, Word memoryAddr, Word variable){
		int memoryIdx = Bit.toDecimal(memoryAddr.getBits());
		memory(signal, memoryIdx, variable);
	}
	
	/**
	 * 
	 * @param signal
	 * @param memoryIdx
	 * @param variable
	 */
	public void memory(Signal signal, int memoryIdx, Word variable){
		
		if(signal == Signal.READ || signal == Signal.WRIT){
			if(variable==null){
				throw new LogicException("Variable cannot be null");
			}
		}
		
		if(Signal.READ == signal){
			
			Word.copy(getCell(memoryIdx), variable);
			
		}else if(Signal.WRIT == signal){
			
			Word.copy(variable, getCell(memoryIdx));
			
		}else{
			/* dump the first xxx words*/
			int numWords = 256;
			BufferedWriter bw = null;
			StringWriter writer = new StringWriter();
			try{
				bw = new BufferedWriter(writer);
				bw.append("0000\t");
				for(int i=0,j=1; i<numWords; i++,j++){
					String hexValue = Bit.toHexString(addresses[i].getBits());
					bw.append(StringUtils.leftPad(hexValue, 3, '0'));
					if(j%8==0 && i< numWords-1){
						String lineNumHex = new BigInteger(
								String.valueOf(j), 10).toString(16);
						bw.newLine();
						bw.append(StringUtils.leftPad(lineNumHex, 4, '0'));
					}
					bw.append("\t");
				}
			}catch(Exception e){
				throw new SystemException(Errors.MEM_DUMP_FAILED);
			}finally{
				if(bw!=null){
					try{ bw.close(); }catch(Exception e){}
				}
			}
			io.getLog().info("DUMP Memory[0-255] in hex:");
			io.getLog().info(writer.toString());
		}
	}
	
	protected Word getCell(Word memoryAddr){
		int memoryIdx = Bit.toDecimal(memoryAddr.getBits());
		return getCell(memoryIdx);
	}
	
	protected Word getCell(int memoryIdx){
		if(memoryIdx>=SIZE){
			throw new SystemException(Errors.MEM_RANGE_OUT_OF_BOUND);
		}
		return addresses[memoryIdx];
	}
	
	
}
