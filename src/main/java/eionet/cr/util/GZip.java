package eionet.cr.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

public class GZip {

    public static boolean isFileGZip(File file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            try {
                new GZIPInputStream(inputStream);
                return true;
            } catch (Exception ex) {
                // Exception that the file was not recognized as GZIP
                // System.out.println("Exception 1 occured: "+ex.getMessage());
                return false;
            }
        } catch (Exception ex) {
            // Exception in case the file was not found or any other IO error occured.
            // System.out.println("Exception 2 occured: "+ex.getMessage());
            return false;
        }
    }

    public static File unPack(File sourceFile) throws IOException, DataFormatException{

        FileInputStream inputStream = new FileInputStream(sourceFile);
        GZIPInputStream packedInputStream = new GZIPInputStream(inputStream);
        File targetFile = new File(sourceFile.getAbsolutePath()+".unpacked");

        int bufferSize = 8192;

        byte [] buffer = new byte[bufferSize];
        FileOutputStream out = new FileOutputStream(targetFile);

        int length;
        while ((length = packedInputStream.read(buffer, 0, bufferSize)) != -1)
          out.write(buffer, 0, length);
        out.close();
        packedInputStream.close();

        return targetFile;
    }

}
