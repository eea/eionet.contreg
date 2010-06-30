/*
* The contents of this file are subject to the Mozilla Public
* 
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
* Agency. Portions created by Tieto Eesti are Copyright
* (C) European Environment Agency. All Rights Reserved.
* 
* Contributor(s):
* Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.harvest.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class MimeTypeConverter {
	
	/** */
	private static final String MAPPINGS_FILENAME = "mimeTypeToRdfType.xml";

	/** */
	private static Log logger = LogFactory.getLog(MimeTypeConverter.class);
	
	/** */
	private static Map<String,String> mimeToRdfMap;
	
	/** */
	private static Object initializationLock = new Object();
	
	/**
	 * 
	 */
	private static void initialize() {
		
		mimeToRdfMap = new HashMap<String,String>();
		
		InputStream inputStream = null;
		Properties properties = new Properties();
		try{
			inputStream =
				MimeTypeConverter.class.getClassLoader().getResourceAsStream(MAPPINGS_FILENAME);
			properties.loadFromXML(inputStream);
		}
		catch (IOException e){
			logger.error("Failed to load XML-formatted properties from " + MAPPINGS_FILENAME, e);
		}
		finally{
			if (inputStream!=null){
				try{
					inputStream.close();
				}
				catch (IOException e){}
			}
		}
		
		if (!properties.isEmpty()){
			
			for (Map.Entry entry : properties.entrySet()){
				
				String rdfType = entry.getKey().toString();
				String[] mediaTypes = entry.getValue().toString().split("\\s+");
				
				if (!StringUtils.isBlank(rdfType) && mediaTypes!=null && mediaTypes.length>0){
					
					for (int i=0; i<mediaTypes.length; i++){
						
						if (!StringUtils.isBlank(mediaTypes[i])){
							
							mimeToRdfMap.put(mediaTypes[i].trim(), rdfType.trim());
						}
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param mimeType
	 * @return
	 */
	public static String getRdfTypeFor(String mimeType){
		
		if (mimeToRdfMap==null){
			
			synchronized (initializationLock) {
				if (mimeToRdfMap==null){
					initialize();
				}
			}
		}
		
		// Try to find exact match first.
		// If no exact match found, loop through all entries
		// and do regex matching with each.
		
		String result = mimeToRdfMap.get(mimeType);
		if (StringUtils.isBlank(result)){
			
			for (Map.Entry<String,String> entry : mimeToRdfMap.entrySet()){
				
				// treat the key as if it was a Java regular expression
				if (mimeType.matches(entry.getKey())){
					result = entry.getValue();
				}
			}
		}
		
		return StringUtils.isBlank(result) ? null : result.trim();
	}
}
