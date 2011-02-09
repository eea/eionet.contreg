package eionet.cr.harvest;

import java.util.Date;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.web.security.CRUser;

public class VirtuosoInstantHarvest extends VirtuosoPullHarvest {


	/**
	 * 
	 * @param sourceUrlString
	 * @param lastHarvest
	 */
	public VirtuosoInstantHarvest(String sourceUrlString, Date lastHarvest, String userName){
		
		super(sourceUrlString, lastHarvest);
		
		ObjectDTO objectDTO = new ObjectDTO(Subjects.CR_FILE, false);
		objectDTO.setSourceUri(CRUser.registrationsUri(userName));
		sourceMetadata.addObject(Predicates.RDF_TYPE, objectDTO);
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.Harvest#doHarvestStartedActions()
	 */
	protected void doHarvestStartedActions() throws HarvestException{
		
		logger.debug("Instant virtuoso harvest started");
		super.doHarvestStartedActions();
	}
	
	/**
	 * 
	 * @param sourceUrl
	 * @param userName
	 * @return
	 * @throws DAOException
	 */
	public static VirtuosoInstantHarvest createFullSetup(String sourceUrl, String userName) throws DAOException{
		
		HarvestSourceDTO dto = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(sourceUrl);
		int numOfResources = dto.getResources()==null ? 0 : dto.getResources().intValue();
		
		VirtuosoInstantHarvest instantHarvest = new VirtuosoInstantHarvest(sourceUrl, null, userName);
		instantHarvest.setPreviousHarvest(DAOFactory.get().getDao(HarvestDAO.class).getLastHarvestBySourceId(dto.getSourceId().intValue()));
		instantHarvest.setDaoWriter(new HarvestDAOWriter(dto.getSourceId().intValue(), Harvest.TYPE_PULL, numOfResources, CRUser.application.getUserName()));
		instantHarvest.setNotificationSender(new HarvestNotificationSender());
		
		return instantHarvest;
	}
}
