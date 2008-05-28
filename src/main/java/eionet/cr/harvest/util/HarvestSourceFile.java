package eionet.cr.harvest.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.MessageFormat;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import eionet.cr.common.Identifiers;
import eionet.cr.config.GeneralConfig;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;
import eionet.cr.util.xml.ConversionsParser;
import eionet.cr.util.xml.XmlAnalysis;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestSourceFile extends File {
	
	/** */
	private Boolean isXml = null;
	
	/** */
	private XmlAnalysis xmlAnalysis = null;
	
	/** */
	private String rdfConversionId = null;
	
	/**
	 * 
	 * @param pathname
	 */
	public HarvestSourceFile(String pathname) {
		super(pathname);
	}
	
	/**
	 * 
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	public boolean isXml() throws IOException, ParserConfigurationException{
		
		if (this.isXml==null)
			isXml = new Boolean(Util.isValidXmlFile(getAbsolutePath()));
		
		return this.isXml.booleanValue();
	}

	/**
	 * @return 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private XmlAnalysis getXmlAnalysis() throws ParserConfigurationException, SAXException, IOException {
		
		if (!isXml())
			return null;
		
		if (this.xmlAnalysis==null){
			XmlAnalysis a = new XmlAnalysis();
			a.parse(this);
			this.xmlAnalysis = a;
		}
		
		return this.xmlAnalysis;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public boolean isRDF() throws ParserConfigurationException, SAXException, IOException{
		
		XmlAnalysis analysis = getXmlAnalysis();
		if (analysis!=null){
			StringBuffer buf = new StringBuffer();
			buf.append(analysis.getStartTagNamespace()).append(analysis.getStartTag());
			return buf.toString().equalsIgnoreCase(Identifiers.RDF_RDF);
		}
		else
			return false;
	}
	
	/**
	 * @return the rdfConversionId
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public String getRdfConversionId() throws IOException, ParserConfigurationException, SAXException {
		
		if (!isXml())
			return null;

		if (rdfConversionId==null){
			
			XmlAnalysis analysis = getXmlAnalysis();
			String schemaOrDtd = analysis.getSchemaLocation();
			if (schemaOrDtd==null || schemaOrDtd.length()==0){
				schemaOrDtd = analysis.getSystemDtd();
				if (schemaOrDtd==null || !URLUtil.isURL(schemaOrDtd)){
					schemaOrDtd = analysis.getPublicDtd();
				}
			}
			
			if (schemaOrDtd!=null && schemaOrDtd.length()>0){
				
				String listConversionsUrl = GeneralConfig.getRequiredProperty(GeneralConfig.XMLCONV_LIST_CONVERSIONS_URL);
				listConversionsUrl = MessageFormat.format(listConversionsUrl, Util.toArray(URLEncoder.encode(schemaOrDtd)));
				
				URL url = new URL(listConversionsUrl);
				URLConnection httpConn = url.openConnection();
				
				InputStream inputStream = null;
				try{
					inputStream = httpConn.getInputStream();
					ConversionsParser conversionsParser = new ConversionsParser();
					conversionsParser.parse(inputStream);
					this.rdfConversionId = conversionsParser.getRdfConversionId();
				}
				finally{
					try{
						if (inputStream!=null) inputStream.close();
					}
					catch (IOException e){}
				}
			}
		}
		
		return rdfConversionId;
	}
}
