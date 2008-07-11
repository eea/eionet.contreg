package eionet.cr.harvest;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.common.Identifiers;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.util.HarvestMessagesMask;
import eionet.cr.harvest.util.RDFResource;
import eionet.cr.util.Util;
import eionet.cr.web.security.CRUser;


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
		
		Boolean sourceAvailable = (harvest instanceof PullHarvest) ? ((PullHarvest)harvest).getSourceAvailable() : null;
		DAOFactory.getDAOFactory().getHarvestSourceDAO().updateHarvestFinished(sourceId, null, numResourcesInSource, sourceAvailable);
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
	
	/**
	 * 
	 * @param resource
	 * @param dedicatedTypeName
	 * @throws DAOException 
	 */
	protected void storeDedicatedHarvestSource(RDFResource resource, String dedicatedTypeName) throws DAOException{
		
		String name = resource.getPropertyValue(Identifiers.DC_TITLE);
		if (name==null || name.length()==0)
			name = resource.getPropertyValue(Identifiers.RDFS_LABEL);
		if (name==null)
			name = resource.getId();
		
		HarvestSourceDTO dto = new HarvestSourceDTO();
		dto.setUrl(resource.getId());
		dto.setName(name);
		dto.setType(dedicatedTypeName);
		
		// TODO - set e-mails + a proper schedule
		
		DAOFactory.getDAOFactory().getHarvestSourceDAO().addSourceIgnoreDuplicate(dto, CRUser.application.getUserName());
	}
}
