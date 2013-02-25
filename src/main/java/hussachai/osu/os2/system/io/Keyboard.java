package hussachai.osu.os2.system.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 
 * @author hussachai
 *
 */
public class Keyboard {

	
	public String getKeys(){
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(System.in));
		String data = null;
		try{
			data = reader.readLine();
			return data;
		}catch(Exception e){
			//TODO: error
			return null;
		}
	}
	
}
