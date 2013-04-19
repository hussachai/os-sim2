package hussachai.osu.os2.system.error;

/**
 * This exception is for the internal implementation and should
 * not expose to System. This error will be not relate to System after all.
 * However, if there is a bug, this exception may be throw and system
 * must catch this exception and wrap it in SystemError exception
 * 
 * @author hussachai
 *
 */
public class LogicException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public LogicException() {
        super();
    }

    public LogicException(String message, Throwable cause) {
        super(message, cause);
    }

    public LogicException(String message) {
        super(message);
    }

    public LogicException(Throwable cause) {
        super(cause);
    }
    
}
