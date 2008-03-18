package eionet.cr.harvest;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.harvest.util.HarvestMessagesMask;
import eionet.cr.util.Util;


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
	 * @param harvest
	 * @param numResourcesInSource
	 * @throws DAOException
	 */
	protected void writeFinished(Harvest harvest, Integer numResourcesInSource) throws DAOException{
		
		logger.debug(getClass().getSimpleName() + ", recording harvest finished, source URL = " + harvest.getSourceUrlString());
		DAOFactory.getDAOFactory().getHarvestDAO().updateFinishedHarvest(harvestId, Harvest.STATUS_FINISHED,
				harvest.getCountTotalStatements(),
				harvest.getCountLiteralStatements(),
				harvest.getCountTotalResources(),
				harvest.getCountEncodingSchemes());
				//HarvestMessagesMask.toString(harvest.getFatalError()!=null, harvest.getErrors().size()>0, harvest.getWarnings().size()>0));
		
		DAOFactory.getDAOFactory().getHarvestSourceDAO().updateHarvestFinished(sourceId, null, numResourcesInSource);
	}

	/**
	 * 
	 * @param harvest
	 * @throws DAOException
	 */
	protected void writeMessages(Harvest harvest) throws DAOException{
		
		logger.debug(getClass().getSimpleName() + ", recording harvest messages, source URL = " + harvest.getSourceUrlString());
		
		// save the fatal exception
		writeThrowable(harvest.getFatalError(), Harvest.FATAL);
		
		// save errors and warnings
		writeThrowables(harvest.getErrors(), Harvest.ERROR);
		writeThrowables(harvest.getWarnings(), Harvest.WARNING);
	}
	
	/**
	 * 
	 * @param throwable
	 * @param type
	 * @throws DAOException 
	 */
	protected void writeThrowable(Throwable throwable, String type) throws DAOException{
		
		if (throwable==null)
			return;
		
		HarvestMessageDTO harvestMessageDTO = new HarvestMessageDTO();
		harvestMessageDTO.setHarvestId(new Integer(this.harvestId));
		harvestMessageDTO.setType(type);
		harvestMessageDTO.setMessage(throwable.toString());
		harvestMessageDTO.setStackTrace(Util.getStackTrace(throwable));
		DAOFactory.getDAOFactory().getHarvestMessageDAO().insertHarvestMessage(harvestMessageDTO);
	}
	
	/**
	 * 
	 * @param throwables
	 * @param type
	 * @throws DAOException
	 */
	protected void writeThrowables(List<Throwable> throwables, String type) throws DAOException{
		
		if (throwables==null)
			return;
		
		for (int i=0; i<throwables.size(); i++){
			writeThrowable(throwables.get(i), type);
		}
	}
}
