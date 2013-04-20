package hussachai.osu.os2.system.cpu;

import hussachai.osu.os2.system.unit.Word;

public class CPUSnapshot {
    
    private Word pc = new Word();
    
    private Word ir = new Word();

    public Word getPC() {
        return pc;
    }

    public void setPC(Word pc) {
        Word.copy(pc, this.pc);
    }
    
    public Word getIR() {
        return ir;
    }

    public void setIR(Word ir) {
        Word.copy(ir, this.ir);
    }
    
    
}
