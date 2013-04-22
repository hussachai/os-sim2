package hussachai.osu.os2.system.misc;

import org.apache.commons.lang3.StringUtils;

/**
 * TraceFormatter utility.
 * This class contains utility methods for handling string
 * and alignment of trace data.
 * @author hussachai
 */
public class TraceFormatter {
    
    private static final int PAD_SIZE = 11;
    
    private static final char PAD_CHAR = ' ';
    
    private static final String HEADERS[] = new String[]{
        "[PC]","[INST]", "[R]", "[EA]",
        "[(R) B]","[(EA) B]","[(R) A]","[(EA) A]"
    };
    
    /**
     * 
     * @return trace data headers
     */
    public static String getTraceHeader(){
        StringBuilder str = new StringBuilder();
        for(String header: HEADERS){
            str.append(StringUtils.center(header, PAD_SIZE, PAD_CHAR));
        }
        return str.toString();
    }
    
    /**
     * Convert word array into string with perfect alignment.  
     * @param data
     * @return
     */
    public static String trace(Object... data){
        if(data==null){
            return traceEmpty();
        }
        StringBuilder str = new StringBuilder();
        for(Object o: data){
            if(o==null){
                str.append(traceEmpty());
            }else{
                str.append(StringUtils.center(
                        o.toString(), PAD_SIZE, PAD_CHAR));
            }
        }
        return str.toString();
    }
    
    /**
     * Call this method when the trace word is empty.
     * @return
     */
    public static String traceEmpty(){
        return StringUtils.center("---", PAD_SIZE, PAD_CHAR);
    }
    
}
