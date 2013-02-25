package hussachai.osu.os2.system.unit;

import hussachai.osu.os2.system.error.LogicException;

import java.util.ArrayList;
import java.util.List;

/**
 * Word is the logical unit. In this case I implemented it as Bit[] wrapper.
 * The word class provides convenient methods to operate on Bit[] 
 * 
 * Word is mutable
 * 
 * @author hussachai
 *
 */
public class Word {
	
	/**
	 * The size of word. The specification says that 1 word = 12 bits. 
	 */
	public static final int SIZE = 12;
	
	private Bit bits[];
	
	/**
	 * Default constructor is used to create default bit array
	 * which are all zero
	 */
	public Word(){
		bits = new Bit[SIZE];
		for(int i=0; i<bits.length;i++){
			bits[i] = Bit.O;
		}
	}
	
	/**
	 * 
	 * @param bits
	 */
	public Word(Bit[] bits){
		if(bits.length!=SIZE){
			throw new LogicException("1 word contains "+SIZE
					+" bits but input is "+bits.length);
		}
		this.bits = bits;
	}
	
	/**
	 * if the MSB is 1, it's negative number.
	 * @param bits
	 * @return
	 */
	public boolean isNegativeNumber(){
		return getBits()[0]==Bit.I?true:false;
	}
	
	/**
	 * 
	 * @param binStr
	 * @return
	 */
	public static Word fromBinString(String binStr){
		
		return new Word(Bit.fromBinString(binStr, SIZE));
	}
	
	/**
	 * 
	 * @param decStr
	 * @return
	 */
	public static Word fromDecString(String decStr){
		
		return new Word(Bit.fromDecString(decStr, SIZE));
	}
	
	/**
	 * 
	 * @param hexStr
	 * @return
	 */
	public static Word fromHexString(String hexStr){
		
		return new Word(Bit.fromHexString(hexStr, SIZE));
	}
	
	/**
	 * 
	 * @param bits
	 * @return
	 */
	public static Word[] toWords(Bit[] bits){
		if(bits.length%SIZE!=0){
			throw new LogicException("the bits length is not multiple of "+SIZE);
		}
		List<Word> wordList = new ArrayList<Word>();
		for(int i=0; i<bits.length;i+=SIZE){
			Bit subBits[] = new Bit[SIZE];
			for(int j=0;j<SIZE;j++){
				subBits[j] = bits[i+j];
			}
			wordList.add(new Word(subBits));
		}
		return wordList.toArray(new Word[0]);
	}
	
	public Bit[] getBits(){
		return bits;
	}
	
	@Override
	public String toString(){
		return Bit.toBinString(bits);
	}
	
}


