package hussachai.osu.os2.system.error;

/**
 * System exception is the expected exception when the system
 * found the error during execution. 
 * 
 * @author hussachai
 *
 */
public class SystemException extends RuntimeException {
     
    private static final long serialVersionUID = 1L;
    
    private int errorCode;
    
    /**
     * The error code can be obtain from hussachai.osu.os2.system.error.Errors
     * @param errorCode
     */
    public SystemException(int errorCode) {
        this.errorCode = errorCode;
    }
    
    /**
     * It can wrap other exception in case the system
     * require to throw SystemException but it's got other
     * kind of exception.
     * 
     * @param e
     */
    public SystemException(Throwable e){
        super(e);
    }
    
    public int getErrorCode(){
        return errorCode;
    }
    
}
