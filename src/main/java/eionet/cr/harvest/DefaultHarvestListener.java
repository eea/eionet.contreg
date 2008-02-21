package eionet.cr.harvest;

import org.apache.log4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eionet.cr.harvest.util.RDFResource;
import eionet.cr.index.EncodingSchemes;
import eionet.cr.index.IndexException;
import eionet.cr.index.Indexer;
import eionet.cr.index.MainIndexer;

/**
 * 
 * @author heinljab
 *
 */
public class DefaultHarvestListener implements HarvestListener, ErrorHandler {
	
	/** */
	private static Logger logger = Logger.getLogger(DefaultHarvestListener.class);
	
	/** */
	private Indexer indexer = new MainIndexer();
	private HarvestException fatalException = null;

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.HarvestListener#resourceHarvested(eionet.cr.harvest.util.RDFResource)
	 */
	public void resourceHarvested(RDFResource resource){
		
		try{
			indexer.indexRDFResource(resource);
		}
		catch (IndexException e){
			logger.error(e.toString(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.HarvestListener#harvestStarted()
	 */
	public void harvestStarted() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.HarvestListener#harvestFinished()
	 */
	public void harvestFinished(){
		
		indexer.close();
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.HarvestListener#hasFatalError()
	 */
	public boolean hasFatalError() {
		return fatalException!=null;
	}

	/**
	 * @return the fatalException
	 */
	public HarvestException getFatalException() {
		return fatalException;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
	 */
	public void error(SAXParseException e) throws SAXException {
		logger.error("SAX error encountered: " + e.toString(), e);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	public void fatalError(SAXParseException e) throws SAXException {
		
		if (this.fatalException==null)
			this.fatalException = new HarvestException("SAX fatal error encountered: " + e.toString(), e);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	public void warning(SAXParseException e) throws SAXException {
		logger.warn("SAX warning encountered: " + e.toString(), e);
	}
}
