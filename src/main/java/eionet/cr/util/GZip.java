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

public final class GZip {

    /**
     * Buffer size for unpacking.
     */
    private static final int BUFFER_SIZE  = 8192;
    /**
     * no public instancing.
     */
    private GZip() {

    }
    /**
     * Determines if the given file is in GZip format.
     * @param file File
     * @return boolean
     */
    public static boolean isFileGZip(final File file) {
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
    /**
     * Unpacks the given file.
     * @param sourceFile File
     * @return File
     * @throws IOException if unpacking fails
     * @throws DataFormatException if data is not in correct format
     */
    public static File unPack(final File sourceFile) throws IOException, DataFormatException {

        FileInputStream inputStream = new FileInputStream(sourceFile);
        GZIPInputStream packedInputStream = new GZIPInputStream(inputStream);
        File targetFile = new File(sourceFile.getAbsolutePath() + ".unpacked");

//        int bufferSize = 8192;

        byte [] buffer = new byte[BUFFER_SIZE];
        FileOutputStream out = new FileOutputStream(targetFile);

        int length;
        while ((length = packedInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
          out.write(buffer, 0, length);
        }
        out.close();
        packedInputStream.close();

        return targetFile;
    }

}
