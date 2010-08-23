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
package eionet.cr.harvest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.stripes.action.FileBean;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import eionet.cr.common.Predicates;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.harvest.util.arp.ARPSource;
import eionet.cr.harvest.util.arp.InputStreamBasedARPSource;
import eionet.cr.util.URLUtil;
import eionet.cr.util.xml.ConversionsParser;
import eionet.cr.util.xml.XmlAnalysis;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class UploadHarvest extends Harvest{

	/** */
	private FileBean fileBean;
	
	/** */
	private File convertedFile;
	
	/**
	 * 
	 * @param sourceUrlString
	 * @param fileBean
	 */
	public UploadHarvest(String sourceUrlString, FileBean fileBean){
		
		super(sourceUrlString);
		if (fileBean==null){
			throw new IllegalArgumentException("File bean must not be null");
		}
		this.fileBean = fileBean;
		
		// set auto-generated triples for this source
		
		sourceMetadata.addObject(Predicates.CR_BYTE_SIZE,
				new ObjectDTO(String.valueOf(fileBean.getSize()), true));
		
		String contentType = fileBean.getContentType();
		if (!StringUtils.isBlank(contentType)){
			sourceMetadata.addObject(Predicates.CR_MEDIA_TYPE,
					new ObjectDTO(fileBean.getContentType(), true));
		}

		sourceMetadata.addObject(Predicates.CR_LAST_MODIFIED, new ObjectDTO(
				lastRefreshedDateFormat.format(new Date()), true));
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.Harvest#doExecute()
	 */
	protected void doExecute() throws HarvestException {
		
		File convertedFile = null;
		InputStream inputStream = null;
		
		try{
			ARPSource arpSource = null;
			XmlAnalysis xmlAnalysis = parse();
			
			// if file is XML 
			if (xmlAnalysis!=null){
				
				// convert file to RDF, if success then construct input stream from converted file,				
				// otherwise construct it from original file
				
				convertedFile = convert(xmlAnalysis, fileBean);
				if (convertedFile!=null && convertedFile.exists()){
					inputStream = new FileInputStream(convertedFile);
				}
				else{
					inputStream = fileBean.getInputStream();
				}
				
				// create ARP source from input stream
				arpSource = new InputStreamBasedARPSource(inputStream);
			}

			// harvest ARP source (which in the case of non-XML files is null)
			harvest(arpSource, true);
		}
		catch (ParserConfigurationException e){
			throw new HarvestException(e.toString(), e);
		}
		catch (IOException e){
			throw new HarvestException(e.toString(), e);
		}
		catch (SAXException e){
			throw new HarvestException(e.toString(), e);
		}
		finally{
			
			if (inputStream!=null){
				try{inputStream.close();}catch (IOException ioe){}
			}
			
			if (convertedFile!=null && convertedFile.exists()){
				convertedFile.delete();
			}
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws ParserConfigurationException
	 */
	private XmlAnalysis parse() throws ParserConfigurationException{
		
		Exception parsingException = null;
		XmlAnalysis xmlAnalysis = new XmlAnalysis();
		try {
			xmlAnalysis.parse(fileBean);
		}
		catch (SAXException e) {
			parsingException = e;
		}
		catch (IOException e) {
			parsingException = e;
		}

		if (parsingException!=null){

			logger.debug("Assuming the file is not XML, due to following error on parsing",
					parsingException);
			return null;
		}
		
		return xmlAnalysis;
	}

	/**
	 * 
	 * @param xmlAnalysis
	 * @param fileBean
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws IOException 
	 */
	private File convert(XmlAnalysis xmlAnalysis, FileBean fileBean) throws IOException, SAXException, ParserConfigurationException{
		
		String conversionId = getConversionId(xmlAnalysis);
		if (StringUtils.isBlank(conversionId)){
			return null;
		}
		
		// TODO convert fileBean using the given conversionId, return resulting file
		return null;
	}

	/**
	 * 
	 * @param xmlAnalysis
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	private String getConversionId(
			XmlAnalysis xmlAnalysis) throws IOException,SAXException, ParserConfigurationException{
		
		String result = null;
		
		// Get schema uri, if it's not found then fall back to DTD. 
		String schemaOrDtd = xmlAnalysis.getSchemaLocation();
		if (schemaOrDtd==null || schemaOrDtd.length()==0){
			schemaOrDtd = xmlAnalysis.getSystemDtd();
			if (schemaOrDtd==null || !URLUtil.isURL(schemaOrDtd)){
				schemaOrDtd = xmlAnalysis.getPublicDtd();
			}
		}
		
		// If no schema or DTD still found, assume the URI of the starting element
		// to be the schema by which conversions should be looked for.
		if (schemaOrDtd==null || schemaOrDtd.length()==0){
			schemaOrDtd = xmlAnalysis.getStartElemUri();
		}
		
		// If schema or DTD found, and it's not rdf:DRF, then get its RDF conversion ID,
		// otherwise assume the file is RDF and return.
		if (schemaOrDtd!=null && schemaOrDtd.length()>0
				&& !schemaOrDtd.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#RDF")){
			
			sourceMetadata.addObject(Predicates.CR_SCHEMA, new ObjectDTO(schemaOrDtd, false));
			result = ConversionsParser.getRdfConversionId(schemaOrDtd);
		}
		
		return result;
	}
}
