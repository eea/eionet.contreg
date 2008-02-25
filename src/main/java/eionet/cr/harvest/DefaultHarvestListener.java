package eionet.cr.harvest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.util.RDFResource;
import eionet.cr.index.EncodingSchemes;
import eionet.cr.index.IndexException;
import eionet.cr.index.Indexer;
import eionet.cr.index.MainIndexer;
import eionet.cr.web.security.CRUser;

/**
 * 
 * @author heinljab
 *
 */
public class DefaultHarvestListener implements HarvestListener, org.xml.sax.ErrorHandler {
	
	/** */
	private static Log logger = LogFactory.getLog(DefaultHarvestListener.class);
	
	/** */
	private Indexer indexer = new MainIndexer();
	private HarvestException fatalException = null;
	
	/** */
	private HarvestSourceDTO harvestSourceDTO = null;
	private String harvestType = null;
	private CRUser crUser = null;
	private int countResourcesCalled = 0;
	private int countResourcesIndexed = 0;

	/**
	 * 
	 * @param harvestSourceDTO
	 * @param harvestType
	 * @param crUser
	 */
	public DefaultHarvestListener(HarvestSourceDTO harvestSourceDTO, String harvestType, CRUser crUser){
		
		if (harvestSourceDTO==null || harvestType==null)
			throw new NullPointerException();
		
		this.harvestSourceDTO = harvestSourceDTO;
		this.harvestType = harvestType;
		this.crUser = crUser;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.HarvestListener#resourceHarvested(eionet.cr.harvest.util.RDFResource)
	 */
	public void resourceHarvested(RDFResource resource){
		
		if (resource==null)
			return;
		
		countResourcesCalled++;
		try{
			indexer.indexRDFResource(resource);			
			countResourcesIndexed++;
		}
		catch (IndexException e){
			setFatalException(new HarvestException(e.toString(), e));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.HarvestListener#harvestStarted()
	 */
	public void harvestStarted() {
		
		logger.debug("Harvest started for URL: " + harvestSourceDTO.getPullUrl());
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.HarvestListener#harvestFinished()
	 */
	public void harvestFinished(){
		
		indexer.close();
		
		logger.debug("Harvest finished for URL: " + harvestSourceDTO.getPullUrl());
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.HarvestListener#hasFatalException()
	 */
	public boolean hasFatalException() {
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
		this.setFatalException(new HarvestException("SAX fatal error encountered: " + e.toString(), e));
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	public void warning(SAXParseException e) throws SAXException {
		logger.warn("SAX warning encountered: " + e.toString(), e);
	}

	/**
	 * @return the harvestSourceDTO
	 */
	public HarvestSourceDTO getHarvestSourceDTO() {
		return harvestSourceDTO;
	}

	/**
	 * @return the harvestType
	 */
	public String getHarvestType() {
		return harvestType;
	}

	/**
	 * @return the crUser
	 */
	public CRUser getCrUser() {
		return crUser;
	}

	/**
	 * @param fatalException the fatalException to set
	 */
	protected void setFatalException(HarvestException fatalException) {
		this.fatalException = fatalException;
	}

	/**
	 * @return the countResourcesCalled
	 */
	public int getCountResourcesCalled() {
		return countResourcesCalled;
	}

	/**
	 * @return the countResourcesIndexed
	 */
	public int getCountResourcesIndexed() {
		return countResourcesIndexed;
	}
}
