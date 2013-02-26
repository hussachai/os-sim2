package hussachai.osu.os2.system.error;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 
 * @author hussachai
 *
 */
public class ErrorHandler {
	
	private Properties errors = new Properties();
	
	public ErrorHandler(){
		InputStream in = null;
		try{
			in = ErrorHandler.class.getResourceAsStream("/errors.properties");
			if(in==null) throw new FileNotFoundException();
			errors.load(in);
		}catch(IOException e){
			throw new LogicException("Unable to load errors.properties file from classpath");
		}finally{
			if(in!=null){
				try{ in.close(); }catch(Exception e){}
			}
		}
	}
	
	public void handleError(int errorNumber){
		
	}
	
	public static void main(String[] args) {
		ErrorHandler eh = new ErrorHandler();
		System.out.println(eh.errors.containsKey("201"));
	}
}
