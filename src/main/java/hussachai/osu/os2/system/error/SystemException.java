package hussachai.osu.os2.system.error;


/**
 * 
 * @author hussachai
 *
 */
public class SystemException extends RuntimeException {
	 
	private static final long serialVersionUID = 1L;
	
	private int errorCode;
	
	public SystemException(int errorCode) {
		this.errorCode = errorCode;
	}
	
	public SystemException(Throwable e){
		super(e);
	}
	
	public int getErrorCode(){
		return errorCode;
	}
	
}
