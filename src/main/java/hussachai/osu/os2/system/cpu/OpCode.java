package hussachai.osu.os2.system.cpu;

/**
 * 
 * @author hussachai
 *
 */
public enum OpCode {
	
	/* Type I instructions */
	AND,
	ADD,
	STR,
	LD,
	JMP,
	JPL,

	/* Type II instructions */
	RD,
	WR,
	HLT,
	
	/* Type III instructions */
	CLR,
	INC,
	COM,
	BSW,
	RTL,
	RTR,
	
	/* Type IV instructions */
	NSK,
	GTR,
	LSS,
	NEQ,
	EQL,
	GRE,
	LSE,
	USK,
	
}
