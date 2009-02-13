package eionet.cr.harvest.util;

import java.io.IOException;
import java.io.Reader;

import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.arp.ARP;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class ReaderBasedARPSource implements ARPSource{
	
	/** */
	private Reader reader;
	
	/**
	 * 
	 * @param reader
	 */
	public ReaderBasedARPSource(Reader reader){
		this.reader = reader;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.util.ARPSource#load(com.hp.hpl.jena.rdf.arp.ARP, java.lang.String)
	 */
	public void load(ARP arp, String sourceUrlString) throws SAXException, IOException {
		arp.load(reader, sourceUrlString);
	}
}
