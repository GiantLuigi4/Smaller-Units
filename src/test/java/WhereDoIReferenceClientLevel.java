import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class WhereDoIReferenceClientLevel {
	public static void main(String[] args) throws IOException {
		File[] file = new File[]{new File("src/main/java")};
		ArrayList<File> fis = new ArrayList<>();
		while (file.length != 0) {
			for (File file1 : file) {
				for (File listFile : file1.listFiles()) {
					if (listFile.isDirectory())
						fis.add(listFile);
					else {
						FileInputStream reader = new FileInputStream(listFile);
						int b;
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						while ((b = reader.read()) != -1) outputStream.write(b);
						String str = outputStream.toString();
						outputStream.close();
						outputStream.flush();
						reader.close();
						if (str.contains("LOGGER")) System.out.println(listFile);
					}
				}
			}
			file = fis.toArray(new File[0]);
			fis.clear();
		}
	}
}
