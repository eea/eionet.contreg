package eionet.cr.harvest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.util.RDFResource;
import eionet.cr.index.EncodingSchemes;
import eionet.cr.index.IndexException;
import eionet.cr.index.Indexer;
import eionet.cr.index.MainIndexer;
import eionet.cr.web.security.CRUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author heinljab
 *
 */
public class DefaultHarvestListener implements HarvestListener{
	
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
	
	private HarvestDAO harvestDAO = null;
	
	/**
	 * 
	 * @param harvestSourceDTO
	 * @param harvestType
	 * @param crUser
	 */
	public DefaultHarvestListener(HarvestSourceDTO harvestSourceDTO, String harvestType, CRUser crUser, HarvestDAO harvestDAO){
		
		if (harvestSourceDTO==null || harvestType==null)
			throw new NullPointerException();
		
		this.harvestSourceDTO = harvestSourceDTO;
		this.harvestType = harvestType;
		this.crUser = crUser;
		this.harvestDAO= harvestDAO;
	}

	/**
	 * @throws HarvestException 
	 * 
	 */
	public void indexResources(Map<String,RDFResource> resources) throws HarvestException{
		
		if (resources==null || resources.size()==0)
			return;
		
		Iterator<RDFResource> iter = resources.values().iterator();
		while (iter.hasNext()){
			
			RDFResource resource = iter.next();
			
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
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.HarvestListener#harvestStarted()
	 */
	public void harvestStarted() throws HarvestException {
		
		logger.debug("Harvest started for URL: " + harvestSourceDTO.getUrl());
		
		try {
			if (harvestDAO!=null){
				harvestId = new Integer(harvestDAO.insertStartedHarvest(
						harvestSourceDTO.getSourceId(), harvestType, crUser==null ? null : crUser.getUserName(), "started"));
			}
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
		
		daoUpdateFinishedHarvest();
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.HarvestListener#harvestCancelled()
	 */
	public void harvestCancelled() throws HarvestException {
		try{
			indexer.abort();
		}
		catch (Exception e){
			logger.error("Failure when aborting indexer: " + e.toString(), e);
		}
		
		//daoUpdateFinishedHarvest();
	}

	
	/**
	 * @throws HarvestException 
	 */
	private void daoUpdateFinishedHarvest() throws HarvestException{
		try {
			if (harvestDAO!=null){
				harvestDAO.updateFinishedHarvest(
						harvestId.intValue(), countTotalStatements, countLiteralStatements, countTotalResources, countEncodingSchemes, "");
			}
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
}
