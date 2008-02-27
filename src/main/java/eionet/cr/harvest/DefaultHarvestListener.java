package eionet.cr.harvest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
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
public class DefaultHarvestListener implements HarvestListener, org.xml.sax.ErrorHandler{
	
	/** */
	private static Log logger = LogFactory.getLog(DefaultHarvestListener.class);
	
	/** */
	private Indexer indexer = new MainIndexer();
	private HarvestException fatalException = null;
	
	/** */
	private HarvestSourceDTO harvestSourceDTO = null;
	private String harvestType = null;
	private CRUser crUser = null;
	
	/** */
	private int countTotalResources = 0;
	private int countEncodingSchemes = 0;
	private int countTotalStatements = 0;
	private int countLiteralStatements = 0;
	
	/** */
	private Integer harvestId = null; 

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
	public void resourceHarvested(RDFResource resource) throws HarvestException{
		
		if (resource==null)
			return;
		
		countTotalStatements = countTotalStatements + resource.getCountTotalProperties();
		countLiteralStatements = countLiteralStatements + resource.getCountLiteralProperties();
		
		try{
			indexer.indexRDFResource(resource);
		}
		catch (IndexException e){
			fatalException = new HarvestException(e.toString(), e);
			throw fatalException;
		}
		
		countTotalResources++;
		if (resource.isEncodingScheme())
			countEncodingSchemes++;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.HarvestListener#harvestStarted()
	 */
	public void harvestStarted() throws HarvestException {
		
		logger.debug("Harvest started for URL: " + harvestSourceDTO.getUrl());
		
		try {
			harvestId = new Integer(DAOFactory.getDAOFactory().getHarvestDAO().insertStartedHarvest(
					harvestSourceDTO.getSourceId(), harvestType, crUser==null ? null : crUser.getUserName(), "started"));
		}
		catch (DAOException e) {
			throw new HarvestException("Failure when inserting new harvest: " + e.toString(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.HarvestListener#harvestFinished()
	 */
	public void harvestFinished() throws HarvestException{

		try{
			indexer.close();
		}
		catch (Exception e){
			logger.error("Failure when closing indexer: " + e.toString(), e);
		}

		try {
			DAOFactory.getDAOFactory().getHarvestDAO().updateFinishedHarvest(
					harvestId.intValue(), countTotalStatements, countLiteralStatements, countTotalResources, countEncodingSchemes, "");
		}
		catch (DAOException e) {
			throw new HarvestException("Failure when updating finished harvest: " + e.toString(), e);
		}
		finally{
			logger.debug("Harvest finished for URL: " + harvestSourceDTO.getUrl());
		}
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
	 * @return the countTotalResources
	 */
	public int getCountTotalResources() {
		return countTotalResources;
	}

	/**
	 * @return the countEncodingSchemes
	 */
	public int getCountEncodingSchemes() {
		return countEncodingSchemes;
	}

	/**
	 * @return the countLitObjStatements
	 */
	public int getCountLiteralStatements() {
		return countLiteralStatements;
	}

	/**
	 * 
	 * @return
	 */
	public int getCountTotalStatements(){
		return countTotalStatements;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
	 */
	public void error(SAXParseException e) throws SAXException {
		logger.error("SAX error encountered: " + e.toString(), e);	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	public void fatalError(SAXParseException e) throws SAXException {
		fatalException = new HarvestException("SAX fatal error encountered: " + e.toString(), e);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	public void warning(SAXParseException e) throws SAXException {
		logger.warn("SAX warning encountered: " + e.toString(), e);
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.HarvestListener#getFatalException()
	 */
	public HarvestException getFatalException() {
		return fatalException;
	}
}
