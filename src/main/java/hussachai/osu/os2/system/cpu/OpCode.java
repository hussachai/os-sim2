package hussachai.osu.os2.system.cpu;

/**
 * 
 * @author hussachai
 *
 */
public enum OpCode {
	
	/* Type I instructions */
	AND, //0
	ADD, //1
	STR, //2
	LD,  //3
	JMP, //4
	JPL, //5

	/* Type II instructions */
	RD,  //6
	WR,  //7
	HLT, //8
	
	/* Type III instructions */
	CLR, //9
	INC, //10
	COM, //11
	BSW, //12
	RTL, //13
	RTR, //14
	
	/* Type IV instructions */
	NSK, //15
	GTR, //16
	LSS, //17
	NEQ, //18
	EQL, //19
	GRE, //20
	LSE, //21
	USK; //22
	
	/**
	 * get the instruction type
	 * @return
	 */
	public byte getType(){
		int value = this.ordinal();
		if(value>=0 && value<=5){
			return 1;
		}else if(value>=6 && value<=8){
			return 2;
		}else if(value>=9 && value<=14){
			return 3;
		}
		return 4;
	}
}
