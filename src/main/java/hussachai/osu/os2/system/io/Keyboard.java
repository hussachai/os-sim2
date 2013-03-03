package hussachai.osu.os2.system.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 
 * @author hussachai
 *
 */
public class Keyboard {

	/**
	 * Collect the data from keyboard until new line character found.
	 * The collection of character without new line character form a string.
	 * @return
	 */
	public String readLine(){
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(System.in));
		String data = null;
		try{
			data = reader.readLine();
			return data;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
}
