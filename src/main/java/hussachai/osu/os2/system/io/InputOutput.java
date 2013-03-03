package hussachai.osu.os2.system.io;

/**
 * I/O object registry
 * 
 * We can imagine it as a data bus wiring from the caller device
 * to the destination I/O object.
 * 
 * @author hussachai
 *
 */
public class InputOutput {
	
	private SystemLog log = new SystemLog();
	
	private Screen screen = new Screen();
	
	private Keyboard keyboard = new Keyboard();
	
	public SystemLog getLog(){
		return log;
	}
	
	public Screen getScreen() {
		return screen;
	}

	public Keyboard getKeyboard() {
		return keyboard;
	}
	
}
