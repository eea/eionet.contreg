package eionet.cr.harvest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.harvest.util.HarvestMessagesMask;

/**
 * 
 * @author heinljab
 *
 */
public class HarvestDAOWriter {
	
	private static Log logger = LogFactory.getLog(HarvestDAOWriter.class);
	
	/** */
	private int sourceId;
	private String harvestType;
	private String userName;
	
	/** */
	private int harvestId;

	/**
	 * 
	 */
	public HarvestDAOWriter(int sourceId, String harvestType, String userName){
		
		this.sourceId = sourceId;
		this.harvestType = harvestType;
		this.userName = userName;
	}

	/**
	 * 
	 * @throws DAOException
	 */
	protected void writeStarted(Harvest harvest) throws DAOException{
		
		logger.debug(getClass().getSimpleName() + ", recording harvest started, source URL = " + harvest.getSourceUrlString());
		harvestId = DAOFactory.getDAOFactory().getHarvestDAO().insertStartedHarvest(sourceId, harvestType, userName, Harvest.STATUS_STARTED);
	}

	/**
	 * 
	 * @throws DAOException
	 */
	protected void writeFinished(Harvest harvest) throws DAOException{
		
		logger.debug(getClass().getSimpleName() + ", recording harvest finished, source URL = " + harvest.getSourceUrlString());
		DAOFactory.getDAOFactory().getHarvestDAO().updateFinishedHarvest(harvestId,
				harvest.getCountTotalStatements(),
				harvest.getCountLiteralStatements(),
				harvest.getCountTotalResources(),
				harvest.getCountEncodingSchemes(),
				HarvestMessagesMask.toString(harvest.getFatalError()!=null, harvest.getErrors().size()>0, harvest.getWarnings().size()>0));
}

	/**
	 * 
	 * @param harvest
	 * @throws DAOException
	 */
	protected void writeMessages(Harvest harvest) throws DAOException{
		
		// TODO - implement this method
		logger.debug(getClass().getSimpleName() + ", recording harvest messages, source URL = " + harvest.getSourceUrlString());
	}
}
