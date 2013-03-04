/*
 * Name: Hussachai Puripunpinyo
 * Course No.:  CS 5323
 * Assignment title: PHASE I (March 5)
 * TA's Name: 
 *  - Alireza Boloorchi
 *  - Sukanya Suwisuthikasem
 * Global variables:
 *  - memory (the reference to Memory)
 *  - buffer (the buffer for Loader)
 *  - io (the reference to InputOutput)
 *  
 *  Brief Description:
 *  The loader load the program in hex format. Each digit hex will be convert
 *  to binary in the word unit. It also performs validation such as length checking
 *  and format checking. The load will load the data from user program and put
 *  it to buffer, the buffer will be flushed to memory whenever it's full or at
 *  the end of loading.
 *  
 *  Remark:
 *  The loader() cannot have starting address and trace-switch as its arguments
 *  which is different from specification. The reason that it takes file argument
 *  instead of starting address and trace-switch is because the load is responsible
 *  for parsing the user program file that has starting address and trace-switch data
 *  encoded into the same source file as program. The System cannot invoke Loader and
 *  passing those required arguments because it cannot know those values before Loader
 *  has finished parsing the whole program. 
 */
package hussachai.osu.os2.system;

import hussachai.osu.os2.system.error.Errors;
import hussachai.osu.os2.system.error.LogicException;
import hussachai.osu.os2.system.error.SystemException;
import hussachai.osu.os2.system.io.InputOutput;
import hussachai.osu.os2.system.storage.Buffer;
import hussachai.osu.os2.system.storage.Memory;
import hussachai.osu.os2.system.storage.Memory.Signal;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Program Loader
 * @author hussachai
 *
 */
public class Loader {
	
	public static final int BUFFER_INIT_SIZE = 10;
	
	private Memory memory;
	
	private Buffer buffer;
	
	private InputOutput io;
	
	public Loader(TheSystem system){
		
		this.memory = system.getMemory();
		this.buffer = new Buffer(BUFFER_INIT_SIZE);
		this.io = system.getIO();
	}
	
	/**
	 * The loader uses one space to indicate the end of program but if there
	 * is no space present, the end of data will be the end of program.
	 * 
	 * The only valid character after the end of program symbol is 0 or 1
	 * which is a trace bit. Trace bit must be immediate after the end of program symbol.
	 * If a trace bit is absence, the default value is 0 which means 'off'.
	 * 
	 * The new line(\n) and carriage return (\r) are ignore by loader.
	 * 
	 * [Specification required method]
	 * 
	 * return start address (PC)
	 */
	public LoaderContext loader(File file){
		
		LoaderContext context = new LoaderContext();
		BufferedReader bin = null;
		String data = null;
		
		try{
			/* this function will get rid of new line character and 
			 * handle input character encoding */
			bin = new BufferedReader(new FileReader(file));
			while((data=bin.readLine())!=null){
				if(parse(context, data)){
					break;//EOF reached
				}
			}
		}catch(SystemException e){
			throw e;
		}catch(IOException e){
			throw new SystemException(Errors.IO_READ_ERROR);
		}catch(Exception e){
			if(e instanceof LogicException) throw (LogicException)e;
			throw new LogicException(e.getMessage(), e);
		}finally{
			if(bin!=null){
				try{ bin.close(); }catch(Exception e){}
			}
		}
		
		flushBuffer(context);//make sure that there's nothing left in buffer.
		
		memory.memory(Signal.WRIT, context.memoryIndex-1, new Word());//erase PC from memory
		
		if(context.charCounter%3==0){
			int lengthCheck = Bit.toDecimal(context.checkLengthWord.getBits());
			if(lengthCheck<context.cmdCounter-1){
				throw new SystemException(Errors.MEM_INVALID_RESERVED_SIZE);
			}else if(lengthCheck>context.cmdCounter-1){
				io.getLog().info("WARNING: length check is greater than actual data");
			}
		}else{
			throw new SystemException(Errors.PROG_INVALID_FORMAT);
		}
		
		return context;
	}
	
	/**
	 * Parse the program until termination character
	 * 
	 * @param context
	 * @param data
	 * @return true if the parse reach the termination character
	 */
	private boolean parse(LoaderContext context, String data){
		
		for(int i=0;i<data.length();i++){
			char c = data.charAt(i);
			if(context.exitCharFound){
				if(c=='1' || c=='0'){
					if(c=='1') context.traceSwitch = Bit.I;
					if(data.length()-i>1){
						io.getLog().info("WARNING: input after termination symbol are ignored");
					}
					return true;
				}
				
				throw new SystemException(Errors.PROG_INVALID_TRACEBIT);
				
			}else{
				if(isValidInputChar(c)){
					context.charBuffer[context.charBufferIdx] = c;
					context.charCounter++;
					context.charBufferIdx++;
					if(context.charCounter%3==0){
						context.charBufferIdx = 0;
						String wordStr = new String(context.charBuffer); 
						context.instructionWord = Word.fromHexString(wordStr);
						if(context.checkLengthWord==null){
							context.checkLengthWord = context.instructionWord;
						}else{
							context.cmdCounter++;
							buffer.add(context.instructionWord);
							if(buffer.isFull()){
								flushBuffer(context);
							}
						}
					}
				}else{
					if(c==' '){
						context.exitCharFound = true;
					}else{
						throw new SystemException(Errors.PROG_INVALID_FORMAT);
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Transfer all data in buffer to memory regardless of the 
	 * number of data in buffer.
	 * @param context
	 */
	private void flushBuffer(LoaderContext context){
		Word words[] = buffer.flush();
		for(Word word: words){
			memory.memory(Memory.Signal.WRIT, context.memoryIndex, word);
			context.memoryIndex++;
		}
	}
	
	/**
	 * Check the input character whether it is in
	 * the accepted character or not.
	 * Code
	 * 0-9 = 48-57
	 * A-F = 65-70
	 * a-f = 97-102
	 * @param input
	 */
	private boolean isValidInputChar(char input){
		int code = (int)input;
		if(code>47 && code<58){
			return true;
		}else if(code>64 && code<71){
			return true;
		}else if(code>96 && code<103){
			return true;
		}
		return false;
	}
	
	/**
	 * LoaderContext is the collection of attributes
	 * used by parse method and it can be passed around
	 * for processing when need.
	 * 
	 * @author hussachai
	 *
	 */
	class LoaderContext {
		
		int memoryIndex = 0;
		Bit traceSwitch = Bit.O;
		Word checkLengthWord = null;
		Word instructionWord = null;
		char charBuffer[] = new char[3];
		int charCounter = 0, charBufferIdx = 0, cmdCounter = 0;
		boolean exitCharFound = false;
	}
	
	
}
