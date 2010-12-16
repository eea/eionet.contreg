/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang.StringUtils;
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
			URL url = new URL(urlString==null ? urlString : StringUtils.replace(urlString, " ", "%20"));
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
	public static int streamToFile(InputStream inputStream, File toFile) throws IOException{
		
		FileOutputStream fos = null;
		try{
			int i = -1;
			int totalBytes = 0;
			byte[] bytes = new byte[1024];
			fos = new FileOutputStream(toFile);
	        while ((i = inputStream.read(bytes, 0, bytes.length)) != -1){
	        	fos.write(bytes, 0, i);
	        	totalBytes = totalBytes + i;
	        }
	        
	        return totalBytes;
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
