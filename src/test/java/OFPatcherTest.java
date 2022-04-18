import java.io.File;
import java.io.IOException;

public class OFPatcherTest {
	public static void main(String[] args) throws IOException {
		PathRemover.main(new File("run/test/").getAbsolutePath(), new String[]{});
	}
}
