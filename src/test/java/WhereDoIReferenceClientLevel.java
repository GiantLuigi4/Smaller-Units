import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class WhereDoIReferenceClientLevel {
    public static void main(String[] args) throws IOException {
        File[] file = new File[]{new File("src/main/java")};
        ArrayList<File> fis = new ArrayList<>();
        int ic = 0;
        int fc = 0;
        int lc = 0;
        int tlc = 0;
        while (file.length != 0) {
            for (File file1 : file) {
                for (File listFile : file1.listFiles()) {
                    if (listFile.isDirectory()) {
                        fc++;
                        fis.add(listFile);
                    } else {
                        if (listFile.getName().endsWith(".java"))
                            ic++;
                        FileInputStream reader = new FileInputStream(listFile);
                        int b;
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        while ((b = reader.read()) != -1) outputStream.write(b);
                        String str = outputStream.toString();
                        outputStream.close();
                        outputStream.flush();
                        reader.close();
                        lc += str.split("\n").length;

                        for (String string : str.split("\n")) {
                            if (string.trim().isEmpty()) continue;
                            if (string.trim().startsWith("//")) continue;
                            // TODO: account for block comments and javadocs
                            tlc++;
                        }

                        if (str.contains("LOGGER")) System.out.println(listFile);
                    }
                }
            }
            file = fis.toArray(new File[0]);
            fis.clear();
        }
        System.out.println(fc + " folders");
        System.out.println(ic + " files");
        System.out.println(lc + " lines");
        System.out.println(tlc + " code lines (kinda)");
    }
}
