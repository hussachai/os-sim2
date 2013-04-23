package hussachai.osu.os2.system.loader;

import hussachai.osu.os2.system.storage.Memory.Partition;
import hussachai.osu.os2.system.unit.Bit;
import hussachai.osu.os2.system.unit.ID;
import hussachai.osu.os2.system.unit.Word;

/**
 * Loader Context
 * 
 * @author hussachai
 * 
 */
public class LoaderContext {
    
    /**
     * Enum of module part
     * @author hussachai
     *
     */
    public static enum ModulePart {
        JOB, 
        /** the first line of job payload. It's the length check */
        JOB_PAYLOAD_START,
        JOB_PAYLOAD,
        /**
         * the last line of job payload. It has start address
         * and switch bit */
        JOB_PAYLOAD_END,
        DATA, 
        DATA_PAYLOAD, 
        END
    }
    
    protected ID jobID;

    /** Allocated partition of memory */
    protected Partition partition;

    /** Virtual memory address */
    protected int memoryIndex = 0;

    protected boolean visitedParts[] = new boolean[ModulePart.values().length];
    /**
     * The last encounter part. Loader uses this value to validate the sequence
     * of command.
     */
    protected ModulePart lastPart = null;

    /**
     * This value is used for validating the actual number of data with the
     * specified number of data
     */
    protected int dataCount = 0;
    /**
     * Number of data lines in job. This is the expected number not actual
     * number
     * */
    protected int dataLines = -1;

    /**
     * Number of output lines in job This is the expected number not actual
     * number
     * */
    protected int outputLines = -1;

    protected Bit traceSwitch = null;

    /**
     * Check length for validating actual number of instruction in the other
     * hand, it's expected number of instructions
     * */
    protected int length = -1;
    /**
     * This is the actual number of instruction. It's used for validating
     * program.
     */
    protected int actualLength = 0;
    /** Hold the last instruction word */
    protected Word instruction;

    /** Hold the start address which will be used as PC */
    protected Word startAddress;

    protected int errorCode = -1;

    public ID getJobID() {
        return jobID;
    }

    public Partition getPartition() {
        return partition;
    }

    public int getDataLines() {
        return dataLines;
    }

    public int getOutputLines() {
        return outputLines;
    }

    public Bit getTraceSwitch() {
        return traceSwitch;
    }

    public int getLength() {
        return length;
    }

    public int getOccupiedSpace() {
        return length + dataLines + outputLines;
    }

    public Word getStartAddress() {
        return startAddress;
    }

    public void visit(ModulePart modulePart) {
        this.lastPart = modulePart;
        this.visitedParts[modulePart.ordinal()] = true;
    }

    public boolean isVisited(ModulePart modulePart) {
        return this.visitedParts[modulePart.ordinal()];
    }

    public boolean isVisitedSome() {
        for (boolean visitedPart : visitedParts) {
            if (visitedPart)
                return true;
        }
        return false;
    }
    
    public void setErrorCode(int errorCode) {
        if (errorCode != -1)
            return;
        this.errorCode = errorCode;
    }
}
