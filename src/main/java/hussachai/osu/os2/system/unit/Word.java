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
        return bits[0]==Bit.I?true:false;
    }
    
    public Bit[] getBits(){
        return bits;
    }
    
    /**
     * represent word in the hex string
     */
    @Override
    public String toString(){
        return StringUtils.leftPad(Bit.
                toHexString(bits), 3, '0').toUpperCase();
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
    
    /**
     * set all bits to zero
     * @param word
     */
    public static void clear(Word word){
        Bit bits[] = word.getBits();
        for(int i=0;i<Word.SIZE;i++){
            bits[i] = Bit.O;
        }
    }
    
    /**
     * increment number by 1
     * @param word
     */
    public static void increment(Word word){
        Bit bits[] = word.getBits();
        for(int i=Word.SIZE-1;i>=0;i--){
            if(bits[i].ordinal()==0){
                bits[i] = Bit.I;
                break;
            }else{
                bits[i] = Bit.O;
            }
        }
    }
    
    /**
     * Logical AND between dest and value then
     * store the result in dest.
     * @param dest
     * @param value
     */
    public static void and(Word dest, Word value){
        Bit destBits[] = dest.getBits();
        Bit valueBits[] = value.getBits();
        int sum = 0;
        for(int i=0;i<Word.SIZE;i++){
            sum = destBits[i].ordinal()+valueBits[i].ordinal();
            destBits[i] = sum==2?Bit.I:Bit.O; 
        }
    }
    
    /**
     * Binary addition operation that supports negative number adding 
     * in the form of 2's complement. The overflow bit will be ignored.
     * The reason to ignore bound checking is that there's no binary 
     * solution way to do that (We can use the decimal to check but that
     * doesn't reflect to the real system) 
     * In most programming languages, bound checking for addition is 
     * also ignored.
     * 
     * @param dest
     * @param addend
     */
    public static void add(Word dest, Word addend){
        Bit.add(dest.getBits(), addend.getBits(), true);
    }
    
    /**
     * Flip-flop all bits
     * @param word
     */
    public static void oneComplement(Word word){
        Bit bits[] = word.getBits();
        for(int i=0;i<Word.SIZE;i++){
            bits[i] = bits[i].ordinal()==0?Bit.I:Bit.O;
        }
    }
    
    /**
     * 
     * @param word
     * @return
     */
    public static void twosComplement(Word word){
        Bit bits[] = word.getBits();
        oneComplement(word);
        for(int i=Word.SIZE-1;i>=0;i--){
            //increment by 1
            if(bits[i].ordinal()==0){
                bits[i] = Bit.I;
                break;
            }else{
                bits[i] = Bit.O;
            }
        }
    }
    
    /**
     * 
     * @param word
     * @return true if word value =0, otherwise false
     */
    public static boolean equalZero(Word word){
        Bit bits[] = word.getBits();
        for(int i=0;i<Word.SIZE;i++){
            if(bits[i]==Bit.I) return false;
        }
        return true;
    }
    
    /**
     * 
     * @param word
     * @return true if word value <0, otherwise false
     */
    public static boolean lessThanZero(Word word){
        return word.isNegativeNumber();
    }
    
    /**
     * @param word
     * @return true if word value >0, otherwise false
     */
    public static boolean greaterThanZero(Word word){
        Bit bits[] = word.getBits();
        if(word.isNegativeNumber()) return false;
        for(int i=1;i<Word.SIZE;i++){
            if(bits[i]==Bit.I) return true;
        }
        return false;
    }
    
    /**
     * 
     * In-place array shit right
     * @param word
     * @param number
     */
    public static void shiftRight(Word word, int number){
        Bit bits[] = word.getBits();
        for(int x=0;x<number;x++){
            Bit lastBit = bits[Word.SIZE-1];
            for(int i=Word.SIZE-1;i>0;i--){
                bits[i] = bits[i-1];
            }
            bits[0] = lastBit;
        }
    }
    
    /**
     * 
     * In-place array shit left
     * @param word
     * @param number
     */
    public static void shiftLeft(Word word, int number){
        Bit bits[] = word.getBits();
        for(int x=0;x<number;x++){
            Bit firstBit = bits[0];
            //loop to the second last bit
            for(int i=0;i<Word.SIZE-1;i++){
                bits[i] = bits[i+1];
            }
            bits[Word.SIZE-1] = firstBit;
        }
    }
    
    /**
     * Swap the first nibble with the last nibble
     * @param word
     */
    public static void swapBytes(Word word){
        Bit bits[] = word.getBits();
        Bit tmp = null;
        for(int i=0;i<Word.SIZE;i+=Word.NIBBLE_SIZE){
            tmp = bits[i];
            bits[i] = bits[i+Word.NIBBLE_SIZE];
            bits[i+Word.NIBBLE_SIZE] = tmp;
        }
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
    
}


