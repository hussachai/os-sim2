package hussachai.osu.os2.system;

/**
 * This class stores the constant of all supported system properties
 *  
 * @author hussachai
 *
 */
public final class Environment {
    
    public static final String EVENT_JOB_INIT = "event.jobInit";
    
    public static final String EVENT_JOB_TERM = "event.jobTerm";
    
    public static final String EVENT_LOAD_FAIL = "event.loadFail";
    
    public static final String EVENT_IO_REQ = "event.ioReq";
    
    public static final String EVENT_CTX_SWITCH = "event.ctxSwitch";
    
    public static final String EVENT_MALLOC = "event.malloc";
    
    public static final String EVENT_ALL = "event.all";
    
    /**
     * This class shouldn't be instantiated.
     */
    private Environment(){}
    
    /**
     * 
     * @param key
     * @return
     */
    public static String get(String key){
        return System.getProperty(key);
    }
    
}
