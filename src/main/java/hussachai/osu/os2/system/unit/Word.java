package hussachai.osu.os2.system.unit;

import hussachai.osu.os2.system.error.LogicException;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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
    
    public static final int NIBBLE_SIZE = 6;
    
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
    
    public void increment(){
        for(int i=Word.SIZE-1;i>=0;i--){
            if(bits[i].ordinal()==0){
                bits[i] = Bit.I;
                break;
            }else{
                bits[i] = Bit.O;
            }
        }
    }
    
    public Bit[] getBits(){
        return bits;
    }
    
    /**
     * represent word in the hex string
     */
    @Override
    public String toString(){
        return StringUtils.leftPad(Bit.toHexString(bits), 3, '0');
    }
    
    /**
     * represent word in the binary string
     */
    public String toBinString(){
        return Bit.toBinString(bits);
    }
    
    /**
     * zero-base index number slicing 
     * @param firstIdx
     * @param lastIdx
     * @return
     */
    public Bit[] slice(int firstIdx, int lastIdx){
        if( (firstIdx<0 || firstIdx>Word.SIZE-1)
            || (lastIdx<0 || lastIdx>Word.SIZE-1) || (firstIdx>lastIdx) ){
            throw new LogicException("Invalid range: ("+firstIdx+","+lastIdx+")");
        }
        Bit slice[] = new Bit[(lastIdx-firstIdx)+1];
        for(int i=firstIdx, j=0; i<=lastIdx;i++,j++){
            slice[j] = this.bits[i];
        }
        return slice;
    }
    
    /**
     * 
     * @param word
     * @return
     */
    public int toDecimal(){
        
        return Bit.toDecimal(bits);
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
     * @param src
     * @param dest
     */
    public static void copy(Word src, Word dest){
        /* Use native copy which is much more faster than using loop */
        System.arraycopy(src.getBits(), 0, dest.getBits(), 0, Word.SIZE);
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
    
}


