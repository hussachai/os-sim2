package hussachai.osu.os2.system.error;


/**
 * 
 * @author hussachai
 *
 */
public class SystemError extends RuntimeException {
	 
	private static final long serialVersionUID = 1L;
	
	private int errorCode;
	
	public SystemError(int errorCode) {
		this.errorCode = errorCode;
	}
	
	public SystemError(Throwable e){
		super(e);
	}
	
	public int getErrorCode(){
		return errorCode;
	}
	
}
