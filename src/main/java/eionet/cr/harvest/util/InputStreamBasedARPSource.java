package eionet.cr.harvest.util;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.arp.ARP;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class InputStreamBasedARPSource implements ARPSource{
	
	/** */
	private InputStream inputStream;
	
	/**
	 * 
	 * @param inputStream
	 */
	public InputStreamBasedARPSource(InputStream inputStream){
		this.inputStream = inputStream;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.util.ARPSource#load(com.hp.hpl.jena.rdf.arp.ARP, java.lang.String)
	 */
	public void load(ARP arp, String sourceUrlString) throws SAXException, IOException {
		arp.load(inputStream, sourceUrlString);
	}

}
