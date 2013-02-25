package hussachai.osu.os2.system;

import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.Word;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author hussachai
 *
 */
public class Memory {

	/**
	 * the addresses store 4096 words = 2^12
	 * 
	 */
	public static final int SIZE = 4096;
	
	private Word addresses[] = new Word[SIZE];
	
	public static enum Signal {
		READ, WRIT, DUMP
	}
	
	public Memory(){
		for(int i=0; i<SIZE;i++){
			addresses[i] = new Word();
		}
	}
	
	/**
	 * 
	 * @param signal
	 * @param ea - memory address (EA)
	 * @param variable
	 */
	public void memory(Signal signal, Word ea, Word variable){
		if(Signal.READ == signal){
			int index = Bit.toDecimal(ea.getBits());
			if(index>=SIZE){
				//TODO: error
			}
			System.arraycopy(addresses[index].getBits(), 0, 
					variable.getBits(), 0, Word.SIZE);
		}else if(Signal.WRIT == signal){
			
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
				
			}finally{
				if(bw!=null){
					try{ bw.close(); }catch(Exception e){}
				}
			}
			System.out.println(writer.toString());
		}
	}
	
	public static void main(String[] args) {
		Memory m = new Memory();
		m.memory(Signal.DUMP, null, null);
	}
}
