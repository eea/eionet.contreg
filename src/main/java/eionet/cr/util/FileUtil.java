package eionet.cr.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class FileUtil {
	
	/** */
	private static Log logger = LogFactory.getLog(FileUtil.class);

	/**
	 * Downloads the contents of the given URL into the given file and closes the file.
	 * 
	 * @param urlString
	 * @param toFile
	 * @throws IOException
	 */
	public static void downloadUrlToFile(String urlString, File toFile) throws IOException{
		
		InputStream inputStream = null;
		try{
			URL url = new URL(urlString);
			URLConnection httpConn = url.openConnection();
			inputStream = httpConn.getInputStream();
			FileUtil.streamToFile(inputStream, toFile);
		}
		finally{
			try{
		        if (inputStream!=null) inputStream.close();
			}
			catch (IOException e){
				logger.error("Failed to close URLConnection's input stream: " + e.toString(), e);
			}
		}
	}

	/**
	 * Writes the content of the given InputStream into the given file and closes the file.
	 * The caller is responsible for closing the InputStream!
	 * 
	 * @param inputStream
	 * @param toFile
	 * @throws IOException
	 */
	public static void streamToFile(InputStream inputStream, File toFile) throws IOException{
		
		FileOutputStream fos = null;
		try{
			int i = -1;
			byte[] bytes = new byte[1024];
			fos = new FileOutputStream(toFile);
	        while ((i = inputStream.read(bytes, 0, bytes.length)) != -1){
	        	fos.write(bytes, 0, i);
	        }
		}
		finally{
			try{
				if (fos!=null) fos.close();
			}
			catch (IOException e){
				logger.error("Failed to close file output stream: " + e.toString(), e);
			}
		}
	}
}
