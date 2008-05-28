package eionet.cr.util.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class ConversionsParser {
	
	/** */
	private String rdfConversionId = null;

	/**
	 * 
	 * @param inputStream
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public void parse(InputStream inputStream) throws SAXException, IOException, ParserConfigurationException{
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom = db.parse(inputStream);
		
		Element rootElem = dom.getDocumentElement();
		if (rootElem!=null){
			NodeList nl = rootElem.getElementsByTagName("conversion");
			for(int i=0; nl!=null && i<nl.getLength(); i++){
				readConversion((Element)nl.item(i));
			}
		}
	}
	
	/**
	 * 
	 * @param conversionElement
	 */
	private void readConversion(Element conversionElement){
		
		if (conversionElement!=null){
			
			boolean isRDFConversion = false;
			NodeList nl = conversionElement.getElementsByTagName("result_type");
			if (nl!=null && nl.getLength()>0){
				Element element = (Element)nl.item(0);
				Text text = (Text)element.getFirstChild();
				if (text!=null){
					String resultType = text.getData();
					if (resultType!=null && resultType.equals("RDF"))
						isRDFConversion = true;
				}
			}
			
			if (isRDFConversion && (rdfConversionId==null || rdfConversionId.length()==0)){
				
				nl = conversionElement.getElementsByTagName("convert_id");
				if (nl!=null && nl.getLength()>0){
					Element element = (Element)nl.item(0);
					Text text = (Text)element.getFirstChild();
					if (text!=null){
						this.rdfConversionId = text.getData();
					}
				}
			}
		}
	}

	/**
	 * @return the rdfConversionId
	 */
	public String getRdfConversionId() {
		return rdfConversionId;
	}
}
