package hussachai.osu.os2.system.io;


public class InputOutput {
	
	private Screen screen = new Screen();
	
	private Keyboard keyboard = new Keyboard();
	
	/**
	 * Print to screen
	 */
	public void print(String data){
		System.out.println(data);
	}
	
}
