package hussachai.osu.os2.program.example;

import java.io.IOException;

import org.junit.Ignore;

/**
 * 
 * @author hussachai
 *
 */
@Ignore
public class TowersOfHanoi {
	
	static void moveDish(int n, int start, int finish, int helper) {
		if (n == 1){
			System.out.println(">>Move a coin from " + start + " to " + finish);
		}else if (n > 1) {
			moveDish(n - 1, start, helper, finish);
			System.out.println(">>Move a coin from " + start + " to " + finish);
			moveDish(n - 1, helper, finish, start);
			System.out.println("FUCK"+n);
		} else if (n < 0) {
			System.out.println("Input must be positive");
		}
	}
	
	public static void main(String[] args) throws IOException {
		moveDish(4, 1, 3, 2);
	}
	
}