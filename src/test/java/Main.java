import hussachai.osu.os2.system.TheSystem;

import org.junit.Ignore;

/**
 * 
 * @author hussachai
 *
 */
@Ignore
public class Main {
    
    public static void main(String[] args) {
        args = new String[]{
            "programs/sum-of-sequence.hex"
//          "programs/error-test/infinite-loop.hex"
        };
        TheSystem.main(args);
    }
}
