package hussachai.osu.os2.system.unit;

import hussachai.osu.os2.system.error.LogicException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * I use enum to represent the bit instead of actual bit
 * because it's easy to debug and the purpose of this program
 * is to simulate how the basic operating system works.
 * 
 * Bit enum is thread-safe, and immutable.
 * 
 * @author hussachai
 *
 */
public enum Bit {
    
    O,I;
    
    /**
     * Return 0 or 1 (String type) for display only
     *  
     */
    @Override
    public String toString(){
        return String.valueOf(this.ordinal());
    }
    
    /**
     * The less memory greedy alternative to toString()
     * @return
     */
    public char toChar(){
        return this.ordinal()==0?'0':'1';
    }
    
    
    
    /**
     * convert integer to Bit
     * @param i
     * @return
     */
    public static Bit toBit(int i){
        if(i==0){
            return Bit.O;
        }else if(i==1){
            return Bit.I; 
        }
        throw new LogicException("Bit conversion error! input out of range: "+i);
    }
    
    /**
     * convert char to Bit
     * @param c
     * @return
     */
    public static Bit toBit(char c){
        if(c=='0' || c=='O'){
            return Bit.O;
        }else if(c=='1' || c=='I'){
            return Bit.I;
        }
        throw new LogicException("Bit conversion error! unknown input format: "+c);
    }
    
    /**
     * 
     * @param bits
     * @param size
     * @return
     */
    public static Bit[] leftPad(Bit[] bits, int size, Bit padBit){
        if(bits.length>=size){
            return bits;
        }
        Bit newValues[] = new Bit[size];
        int dataOffset = size-bits.length;
        for(int i=0;i<dataOffset;i++){
            newValues[i] = padBit;
        }
        for(int i=dataOffset, j=0;i<size;i++,j++){
            newValues[i] = bits[j];
        }
        return newValues;
    }
    
    /**
     * 
     * @param binStr
     * @return
     */
    public static Bit[] fromBinString(String binStr, int size){
        binStr = StringUtils.trimToNull(binStr);
        if(binStr==null) throw new LogicException("Input string cannot be empty");
        
        Bit bits[] = new Bit[binStr.length()];
        for(int i=0; i<binStr.length();i++){
            bits[i] = toBit(binStr.charAt(i));
        }
        return leftPad(bits, size, Bit.O);
    }
    
    /**
     * 
     * @param bits
     * @return
     */
    public static String toBinString(Bit... bits){
        if(Bit.isEmpty(bits)) throw new LogicException("Bits cannot be empty");
        StringBuilder strBuilder = new StringBuilder();
        for(Bit bit: bits){
            strBuilder.append(bit.toChar());
        }
        return strBuilder.toString();
    }
    
    /**
     * 
     * @param decStr
     * @return
     */
    public static Bit[] fromDecString(String decStr, int size){
        decStr = StringUtils.trimToNull(decStr);
        if(decStr==null) throw new LogicException("Input string cannot be empty");
        
        String binStr = null;
        try{
            binStr = new BigInteger(decStr, 10).toString(2);
        }catch(NumberFormatException e){
            throw new LogicException("Invalid input characters for decimal: "+decStr);
        }
        return fromBinString(binStr, size);
    }
    
    /**
     * 
     * @param bits
     * @return
     */
    public static String toDecString(Bit... bits){
        String binStr = toBinString(bits);
        return new BigInteger(binStr, 2).toString(10);
    }
    
    /**
     * 
     * @param bits
     * @return
     */
    public static int toDecimal(Bit... bits){
        return Integer.parseInt(toDecString(bits));
    }
    
    /**
     * less strict version of fromHex
     * @param hexStr
     * @return
     */
    public static Bit[] fromHexString(String hexStr, int size){
        hexStr = StringUtils.trimToNull(hexStr);
        if(hexStr==null) throw new LogicException("Input string cannot be empty");
        String binStr = null;
        try{
            binStr = new BigInteger(hexStr, 16).toString(2);
        }catch(NumberFormatException e){
            throw new LogicException("Invalid input characters for hex: "+hexStr);
        }
        return fromBinString(binStr, size);
    }
    
    /**
     * 
     * @param hexStr
     * @param unitSize is the length of hex digit set; for example, 020 EA1 01A (3 sets and each set has length=3)
     * @param bitsPerUnit is the number of bits to store the hex value in each set as binary number
     * @return
     */
    public static Bit[] fromHexString(String hexStr, int unitSize, int bitsPerUnit){
        hexStr = StringUtils.trimToNull(hexStr);
        if(hexStr==null) throw new LogicException("Input string cannot be empty");
        
        if(hexStr.length()% unitSize!=0){
            throw new LogicException("the hex value length set is not "+unitSize);
        }
        List<Bit> bitList = new ArrayList<Bit>();
        for(int i=0; i<hexStr.length(); i+=unitSize){
            String subHexStr = hexStr.substring(i, i+unitSize);
            String subBinStr = new BigInteger(subHexStr, 16).toString(2);
            if(subBinStr.length()>bitsPerUnit){
                throw new LogicException("bitsPerUnit is not enough for conversion");
            }
            subBinStr = StringUtils.leftPad(subBinStr, bitsPerUnit, '0');
            for(int j=0; j<subBinStr.length();j++){
                Bit bit = toBit(subBinStr.charAt(j));
                bitList.add(bit);
            }
        }
        return bitList.toArray(new Bit[0]);
    }
    
    /**
     * 
     * @param bits
     * @return
     */
    public static String toHexString(Bit...bits){
        String binStr = toBinString(bits);
        return new BigInteger(binStr, 2).toString(16);
    }
    
    /**
     * 
     * @param bits
     * @return
     */
    public static boolean isEmpty(Bit... bits){
        if(bits==null || bits.length==0){
            return true;
        }else{
            for(Bit bit: bits){
                if(bit==null){
                    return true;
                }
            }
        }
        return false;
    }
}
