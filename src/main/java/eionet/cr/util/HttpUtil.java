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
public class HttpUtil {
	
	/** */
	private static Log logger = LogFactory.getLog(HttpUtil.class);

	/**
	 * 
	 * @param urlString
	 * @param toFile
	 * @throws IOException
	 */
	public static void downloadUrlToFile(String urlString, File toFile) throws IOException{
		
		InputStream istream = null;
		FileOutputStream fos = null;
		try{
			URL url = new URL(urlString);
			URLConnection httpConn = url.openConnection();
			
			istream = httpConn.getInputStream();
			fos = new FileOutputStream(toFile);
	        
	        int i = -1;
	        byte[] bytes = new byte[1024];
	
	        while ((i = istream.read(bytes, 0, bytes.length)) != -1)
	        	fos.write(bytes, 0, i);
		}
		finally{
			try{
		        if (istream!=null) istream.close();
			}
			catch (IOException e){
				logger.error("Failed to close URLConnection's input stream: " + e.toString(), e);
			}
			try{
				if (fos!=null) fos.close();
			}
			catch (IOException e){
				logger.error("Failed to close file output stream: " + e.toString(), e);
			}
		}
	}

}
