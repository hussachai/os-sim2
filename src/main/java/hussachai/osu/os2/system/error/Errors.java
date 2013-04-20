package hussachai.osu.os2.system.error;

/**
 * The constant of error numbers.
 * @author hussachai
 *
 */
public final class Errors {
    
    public static final int SYS_INTERNAL_ERROR = 999;
    
    /* CPU errors */
    public static final int CPU_ARITHMETIC_OVERFLOW = 101;
    
    public static final int CPU_SUSPECTED_INFINITE_JOB = 102;
    
    /* Memory errors */
    public static final int MEM_BUFFER_OVERFLOW = 201;
    
    public static final int MEM_PC_OVERFLOW = 202;

    public static final int MEM_RANGE_OUT_OF_BOUND = 203;
    
    public static final int MEM_INCORRECT_RESERVED_SIZE = 204;
    
    public static final int MEM_DUMP_FAILED = 205;
    
    /* IO errors */
    public static final int IO_FILE_NOT_FOUND = 301;
    
    public static final int IO_WRITE_ERROR = 302;
    
    public static final int IO_READ_ERROR = 303;
    
    public static final int IO_INSUFFICIENT_OUTPUT_SPACE = 304;
    
    /* Program errors */
    public static final int PROG_INVALID_OP = 501;
    
    public static final int PROG_INVALID_ACTION = 502;
    
    public static final int PROG_INVALID_TRACEBIT = 503;
    
    public static final int PROG_INVALID_FORMAT = 504;
    
    public static final int PROG_TOO_LONG = 505;
    
    public static final int PROG_MISSING_JOB_RECORD = 506;
    
    public static final int PROG_MISSING_JOB = 507;
    
    public static final int PROG_MISSING_DATA_RECORD = 508;
    
    public static final int PROG_MISSING_DATA = 509;
    
    public static final int PROG_MISSING_END_RECORD = 510;
    
    public static final int PROG_NULL_JOB = 511;
    
    public static final int PROG_UNRECOGNIZED_CHAR = 512;
    
    public static final int PROG_MISSING_DATA_ITEMS = 513;
    
    public static final int PROG_EXTRA_DATA_UNUSED = 514;
    
    public static final int PROG_MISSING_LENGTH_CHECK = 515;
    
    public static final int PROG_MISSING_START_ADDRESS = 516;
    
    /* User errors */
    public static final int USR_INVALID_DATA_TYPE = 601;
    
}
