package eionet.cr.harvest;

import java.util.Iterator;
import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.harvest.util.HarvestMessageType;
import eionet.cr.util.Util;


/**
 * 
 * @author heinljab
 *
 */
public class HarvestDAOWriter {
	
	/** */
	private int sourceId;
	private String harvestType;
	private int numOfResources;
	private String userName;	
	
	/** */
	private int harvestId;

	/**
	 * @param numOfResources TODO
	 * 
	 */
	public HarvestDAOWriter(int sourceId, String harvestType, int numOfResources, String userName){
		
		this.sourceId = sourceId;
		this.harvestType = harvestType;
		this.numOfResources = numOfResources;
		this.userName = userName;		
	}

	/**
	 * 
	 * @throws DAOException
	 */
	protected void writeStarted(Harvest harvest) throws DAOException{
		
		harvestId = DAOFactory.getDAOFactory().getHarvestDAO().insertStartedHarvest(sourceId, harvestType, userName, Harvest.STATUS_STARTED);
		DAOFactory.getDAOFactory().getHarvestSourceDAO().updateHarvestStarted(sourceId);
	}

	/**
	 * 
	 * @param harvest
	 * @param numResourcesInSource
	 * @throws DAOException
	 */
	protected void writeFinished(Harvest harvest) throws DAOException{
		
		DAOFactory.getDAOFactory().getHarvestDAO().updateFinishedHarvest(harvestId, Harvest.STATUS_FINISHED,
				harvest.getStoredTriplesCount(),
				harvest.getDistinctSubjectsCount(),
				0,
				0);
		
		if (harvest instanceof PullHarvest){
			DAOFactory.getDAOFactory().getHarvestSourceDAO().updateHarvestFinished(
					sourceId, null, harvest.getDistinctSubjectsCount(), ((PullHarvest)harvest).getSourceAvailable());
		}
		else{
			DAOFactory.getDAOFactory().getHarvestSourceDAO().updateHarvestFinished(
					sourceId, null, numOfResources + harvest.getDistinctSubjectsCount(), null);
		}
	}

	/**
	 * 
	 * @param harvest
	 * @throws DAOException
	 */
	protected void writeMessages(Harvest harvest) throws DAOException{
		
		// save the fatal exception
		writeThrowable(harvest.getFatalError(), HarvestMessageType.FATAL.toString());
		
		// save errors and warnings
		writeThrowables(harvest.getErrors(), HarvestMessageType.ERROR.toString());
		writeThrowables(harvest.getWarnings(), HarvestMessageType.WARNING.toString());
		
		// save infos
		List<String> infos = harvest.getInfos();
		if (infos!=null && !infos.isEmpty()){
			for (Iterator<String> i=infos.iterator(); i.hasNext();){
				
				HarvestMessageDTO harvestMessageDTO = new HarvestMessageDTO();
				harvestMessageDTO.setHarvestId(new Integer(harvestId));
				harvestMessageDTO.setType(HarvestMessageType.INFO.toString());
				harvestMessageDTO.setMessage(i.next());
				harvestMessageDTO.setStackTrace("");
				DAOFactory.getDAOFactory().getHarvestMessageDAO().insertHarvestMessage(harvestMessageDTO);
			}
		}
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
