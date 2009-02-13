package eionet.cr.harvest.util;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.arp.ARP;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public interface ARPSource {

	/**
	 * 
	 * @param arp
	 * @param sourceUrlString
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void load(ARP arp, String sourceUrlString) throws SAXException, IOException;
}
