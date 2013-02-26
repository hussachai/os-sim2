package hussachai.osu.os2.system;


import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * This class is not thread-safe
 * 
 * @author hussachai
 *
 */
public class Loader {
	
	public static final int BUFFER_INIT_SIZE = 10;
	
	/**
	 * Buffer is implemented using ArrayList. 
	 * ArrayList is not a linked list but it's array that
	 * has counter to keep track of the number of elements.
	 */
	private List<Word> buffer;
	
	private TheSystem system;
	
	public Loader(TheSystem system){
		this.system = system;
		this.buffer = new ArrayList<Word>(BUFFER_INIT_SIZE);
	}
	
	/**
	 *  Block transfer DMA controller takes the bus control by CPU. 
	 *  CPU has no access to bus until the transfer is complete. 
	 *  During this time CPU can perform internal operations that do not need bus. 
	 *  This is a common and popular method with modern microprocessors.
	 */
	
	/**
	 * The loader uses one space to indicate the end of program but if there
	 * is no space present, the end of data will be the end of program.
	 * 
	 * The only valid character after the end of program symbol is 0 or 1
	 * which is a trace bit. Trace bit must be immediate after the end of program symbol.
	 * If a trace bit is absence, the default value is 0 which means off.
	 * 
	 * The new line(\n) and carriage return (\r) are ignore by loader.  
	 */
	public void loadUserProgram(){
		
		boolean trace = false; 
		BufferedReader bin = null;
		String data = null;
		Word checkLengthWord = null;
		Word instructionWord = null;
		Word startWord = null;
		char charBuffer[] = new char[3];
		int charCounter = 0, charBufferIdx = 0, cmdCounter = 0;
		boolean exitCharFound = false;
		
		try{
			/* this function will get rid of new line character and 
			 * handle input character encoding
			 */
			bin = new BufferedReader(new FileReader("programs/example"));
			
			/* I decided to use break->label here because it reduces the check code
			 * and the end of for-loop. It also improve performance a little bit and 
			 * I don't think that using break here causes any design issues.
			 */
			END_OF_PROGRAM: while((data=bin.readLine())!=null){
				for(int i=0;i<data.length();i++){
					char c = data.charAt(i);
					if(exitCharFound){
						if(c=='1' || c=='0'){
							if(c=='1') trace = true;
							if(data.length()-i>1){
								//TODO: warning the rest of character in input file is ignore
								System.out.println("WARNING: input after termination symbol are ignored");
							}
						}else{
							//TODO: error here trace bit must be either 0 or 1
							System.out.println("ERROR: invalid trace bit");
						}
						break END_OF_PROGRAM;
					}else{
						if(isValidInputChar(c)){
							charBuffer[charBufferIdx] = c;
							charCounter++;
							charBufferIdx++;
							if(charCounter%3==0){
								charBufferIdx = 0;
								String wordStr = new String(charBuffer); 
								instructionWord = Word.fromHexString(wordStr);
								if(checkLengthWord==null){
									checkLengthWord = instructionWord;
								}else{
									cmdCounter++;
									buffer.add(instructionWord);
								}
							}
						}else{
							if(c==' '){
								exitCharFound = true;
							}else{
								//TODO: error invalid format
								System.out.println("ERROR: invalid format");
							}
						}
					}
				}
			}
		}catch(Exception e){
			
		}finally{
			if(bin!=null){
				try{ bin.close(); }catch(Exception e){}
			}
		}
		
		/* assign the last instruction word as start address word*/ 
		startWord = instructionWord;
		
		if(charCounter%3==0){
			int lengthCheck = Bit.toDecimal(checkLengthWord.getBits());
			if(cmdCounter!=lengthCheck){
				System.out.println("ERROR: check length error");
			}
		}else{
			System.out.println("ERROR: read error");
		}
		
		this.buffer.clear();
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
	
	
	public static void main(String[] args) {
		
		Loader l = new Loader(null);
		l.loadUserProgram();
		
	}
}
