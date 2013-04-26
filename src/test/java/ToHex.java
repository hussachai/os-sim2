import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;


public class ToHex {

    public static void main(String[] args) {
        String data = "39A3DFFD041431721770401621331370570401210E20D38B3D018E1892883867A0284429303620610000000000FFEFF8FD0031034038FFF";
        for(int i=0,j=0;i<data.length();i+=3,j++){
            String hex = data.substring(i, i+3);
            String bin = new BigInteger(hex, 16).toString(2);
            bin = StringUtils.leftPad(bin, 12,'0');
            String binF = bin.charAt(0)+"";
            binF += " "+bin.substring(1, 4);
            binF += " "+bin.charAt(4);
            binF += " "+bin.charAt(5);
            binF += " "+bin.charAt(6);
            binF += " "+bin.charAt(7);
            binF += " "+bin.charAt(8);
            binF += " "+bin.charAt(9);
            binF += " "+bin.charAt(10);
            binF += " "+bin.charAt(11);
            System.out.println("["+(j)+"]\t"+binF+"\t"+hex);
        }
    }
}
