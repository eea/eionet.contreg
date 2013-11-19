package eionet.cr.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.FileNameUtil;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author jaanus
 *
 */
public class CompressUtil {

    /** */
    private static final Logger LOGGER = Logger.getLogger(CompressUtil.class);

    /** */
    private static final FileNameUtil FILE_NAME_UTIL;

    /**
     *
     */
    static {
        Map<String, String> uncompressSuffix = new LinkedHashMap<String, String>();
        uncompressSuffix.put(".tgz", ".tar");
        uncompressSuffix.put(".taz", ".tar");
        uncompressSuffix.put(".svgz", ".svg");
        uncompressSuffix.put(".cpgz", ".cpio");
        uncompressSuffix.put(".wmz", ".wmf");
        uncompressSuffix.put(".emz", ".emf");
        uncompressSuffix.put(".gz", "");
        uncompressSuffix.put(".z", "");
        uncompressSuffix.put("-gz", "");
        uncompressSuffix.put("-z", "");
        uncompressSuffix.put("_z", "");
        uncompressSuffix.put(".tar.bz2", ".tar");
        uncompressSuffix.put(".tbz2", ".tar");
        uncompressSuffix.put(".tbz", ".tar");
        uncompressSuffix.put(".bz2", "");
        uncompressSuffix.put(".bz", "");
        uncompressSuffix.put(".txz", ".tar");
        uncompressSuffix.put(".xz", "");
        uncompressSuffix.put("-xz", "");
        FILE_NAME_UTIL = new FileNameUtil(uncompressSuffix, ".gz");
    }

    /**
     *
     * @param file
     * @param toFile
     * @return
     * @throws IOException
     */
    public static void uncompress(File file, File toFile) throws IOException {

        FileInputStream fis = null;
        BufferedInputStream bufferedStream = null;
        FileOutputStream outputStream = null;
        CompressorInputStream comprInputStream = null;
        try {
            fis = new FileInputStream(file);
            bufferedStream = new BufferedInputStream(fis);
            comprInputStream =
                    new CompressorStreamFactory().createCompressorInputStream(bufferedStream);

            outputStream = new FileOutputStream(toFile);
            org.apache.commons.compress.utils.IOUtils.copy(comprInputStream, outputStream);
        } catch (Throwable t) {
            throw new IOException("Uncompression failed, probably not a compressed file!", t);
        } finally {
            IOUtils.closeQuietly(comprInputStream);
            IOUtils.closeQuietly(bufferedStream);
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     *
     * @param file
     * @param entryToNewName
     * @throws IOException
     */
    public static void extract(File file, Map<String, String> entryToNewName) throws IOException {

        FileInputStream fis = null;
        BufferedInputStream bufferedStream = null;
        FileOutputStream outputStream = null;
        ArchiveInputStream archiveInputStream = null;
        try {
            fis = new FileInputStream(file);
            bufferedStream = new BufferedInputStream(fis);
            archiveInputStream =
                    new ArchiveStreamFactory().createArchiveInputStream(bufferedStream);

            String dir = file.getParent();
            ArchiveEntry entry;
            while ((entry = archiveInputStream.getNextEntry()) != null) {

                String entryName = entry.getName();
                if (entryToNewName.containsKey(entryName)) {
                    String newFileName = entryToNewName.get(entryName);
                    outputStream = new FileOutputStream(new File(dir, newFileName));
                    org.apache.commons.compress.utils.IOUtils.copy(archiveInputStream, outputStream);
                    IOUtils.closeQuietly(outputStream);
                }
            }
        } catch (Throwable t) {
            throw new IOException("Extraction failed, probably not a compressed file!", t);
        } finally {
            IOUtils.closeQuietly(archiveInputStream);
            IOUtils.closeQuietly(bufferedStream);
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     *
     * @param file
     * @return
     */
    public static String getUncompressedFileName(File file) {

        FileInputStream fis = null;
        BufferedInputStream bufferedStream = null;
        CompressorInputStream comprInputStream = null;
        try {
            fis = new FileInputStream(file);
            bufferedStream = new BufferedInputStream(fis);
            comprInputStream =
                    new CompressorStreamFactory().createCompressorInputStream(bufferedStream);
            return FILE_NAME_UTIL.getUncompressedFilename(file.getName());
        } catch (Throwable t) {
            return null;
        } finally {
            IOUtils.closeQuietly(comprInputStream);
            IOUtils.closeQuietly(bufferedStream);
            IOUtils.closeQuietly(fis);
        }
    }

    /**
     *
     * @param file
     * @return
     */
    public static List<ArchiveEntry> getArchiveEntries(File file) {

        FileInputStream fis = null;
        BufferedInputStream bufferedStream = null;
        ArchiveInputStream archiveInputStream = null;
        try {
            fis = new FileInputStream(file);
            bufferedStream = new BufferedInputStream(fis);
            archiveInputStream =
                    new ArchiveStreamFactory().createArchiveInputStream(bufferedStream);
            ArchiveEntry entry;
            ArrayList<ArchiveEntry> result = new ArrayList<ArchiveEntry>();
            while ((entry = archiveInputStream.getNextEntry()) != null) {
                result.add(entry);
            }
            return result;
        } catch (Throwable t) {
            return null;
        } finally {
            IOUtils.closeQuietly(archiveInputStream);
            IOUtils.closeQuietly(bufferedStream);
            IOUtils.closeQuietly(fis);
        }
    }
}
