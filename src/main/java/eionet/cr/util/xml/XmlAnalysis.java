package eionet.cr.util.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import eionet.cr.common.CRException;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class XmlAnalysis {
	
	/** */
	private static Log logger = LogFactory.getLog(XmlAnalysis.class);

	/** */
	private Handler handler = new Handler();
	private SAXDoctypeReader doctypeReader = new SAXDoctypeReader();

	/**
	 * 
	 * @param file
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws CRException
	 */
	public void parse(File file) throws ParserConfigurationException, SAXException, IOException{
		
		FileInputStream inputStream = null;
		try{
			inputStream = new FileInputStream(file);
			parse(inputStream);
		}
		finally{
			try{
				if (inputStream!=null) inputStream.close();
			}
			catch (IOException e){}
		}
	}
	
	/**
	 * 
	 * @param inputStream
	 * @return
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws GDEMException
	 * @throws SAXException
	 * @throws IOException 
	 */
	public void parse(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
		
		// set up the parser and reader
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		XMLReader reader = parser.getXMLReader();

		// turn off validation against schema or dtd (we only need the document to be well-formed XML)
		parserFactory.setValidating(false);
		reader.setFeature("http://xml.org/sax/features/validation", false); 
		reader.setFeature("http://apache.org/xml/features/validation/schema", false);
		reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		reader.setFeature("http://xml.org/sax/features/namespaces", true);
		
		// turn on dtd handling
		doctypeReader = new SAXDoctypeReader();
		try {
			parser.setProperty("http://xml.org/sax/properties/lexical-handler", doctypeReader);
		}
		catch (SAXNotRecognizedException e) {
			logger.warn("Installed XML parser does not provide lexical events", e);
		}
		catch (SAXNotSupportedException e) {
			logger.warn("Cannot turn on comment processing here", e);
		}       

		// set the handler and do the parsing
		handler = new Handler();
		reader.setContentHandler(handler);
		try{
			reader.parse(new InputSource(inputStream));
		}
		catch (SAXException e){
			Exception ee = e.getException();
			if (ee==null || !(ee instanceof CRException))
				throw e;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public String getStartTag(){
		return handler.getStartTag();
	}

	/**
	 * 
	 * @return
	 */
	public String getStartTagNamespace(){
		return handler.getStartTagNamespace();
	}
	
	/**
	 * 
	 * @return
	 */
	public String getSchemaLocation(){
		return handler.getSchemaLocation();
	}

	/**
	 * 
	 * @return
	 */
	public String getSchemaNamespace(){
		return handler.getSchemaNamespace();
	}
	
	/**
	 * 
	 * @return
	 */
	public String getSystemDtd(){
		return doctypeReader.getDtdSystemId();
	}

	/**
	 * 
	 * @return
	 */
	public String getPublicDtd(){
		return doctypeReader.getDtdPublicId();
	}

	/**
	 * 
	 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
	 *
	 */
	private class Handler extends DefaultHandler{
		
		/** */
		private String startTag = null;
		private String startTagNamespace = null;
		private String schemaLocation = null;
		private String schemaNamespace = null;
		private HashMap<String,String> usedNamespaces = new HashMap<String,String>();

		/*
		 * (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		public void startElement(String uri, String localName, String name, Attributes attrs) throws SAXException {
			
			this.startTagNamespace = uri;
			this.startTag = (localName==null || localName.length()==0) ? name : localName;
			 
			int attrCount = attrs!=null ? attrs.getLength() : 0;
			for (int i=0; i<attrCount; i++) {
				
				String attrName =  attrs.getLocalName(i);
				if (attrName.equalsIgnoreCase("noNamespaceSchemaLocation"))
					this.schemaLocation = attrs.getValue(i);
				else if (attrName.equalsIgnoreCase("schemaLocation")){
					
					String attrValue = attrs.getValue(i);
					if (attrValue!=null && attrValue.length()>0){
						
						StringTokenizer tokens = new StringTokenizer(attrValue);
						if (tokens.countTokens()>=2){
							this.schemaNamespace = tokens.nextToken();
							this.schemaLocation = tokens.nextToken();
						}
						else
							this.schemaNamespace = attrValue;
					}
				}
				
				String attrQName = attrs.getLocalName(i);
				if (attrQName.startsWith("xmlns:")){
					usedNamespaces.put(attrName, attrs.getValue(i));
				}
			}
			
			// quit parsing now, since we don't need any more info
			throw new SAXException(new CRException());
		}

		/*
		 * (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
		 */
		public void error(SAXParseException e){
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
		 */
		public void fatalError(SAXParseException e){
		}

		/*
		 * (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#warning(org.xml.sax.SAXParseException)
		 */
		public void warning (SAXParseException e) throws SAXException {
	    }

		/**
		 * @return the startTag
		 */
		public String getStartTag() {
			return startTag;
		}

		/**
		 * @return the startTagNamespace
		 */
		public String getStartTagNamespace() {
			return startTagNamespace;
		}

		/**
		 * @return the schemaLocation
		 */
		public String getSchemaLocation() {
			return schemaLocation;
		}

		/**
		 * @return the schemaNamespace
		 */
		public String getSchemaNamespace() {
			return schemaNamespace;
		}
	}
	
	public static void main(String[] args){
		
		XmlAnalysis info = new XmlAnalysis();
		try{
			info.parse(new File("C:/temp/schema.rdf"));
			System.out.println(info.getStartTag());
			System.out.println(info.getStartTagNamespace());
		}
		catch (Throwable t){
			t.printStackTrace();
		}
	}
}
