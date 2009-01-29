package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.Harvest;

/**
 * 
 * @author heinljab
 *
 */
@UrlBinding("/harvest.action")
public class HarvestActionBean extends AbstractActionBean {
	
	/** */
	private HarvestDTO harvestDTO;
	private HarvestSourceDTO harvestSourceDTO;
	
	/** */
	private List<HarvestMessageDTO> fatals;
	private List<HarvestMessageDTO> errors;
	private List<HarvestMessageDTO> warnings;
	
	/**
	 * 
	 * @return
	 * @throws DAOException 
	 */
	@DontValidate
    public Resolution unspecified() throws DAOException{
		
		harvestDTO = DAOFactory.getDAOFactory().getHarvestDAO().getHarvestById(harvestDTO.getHarvestId());
		harvestSourceDTO = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(harvestDTO.getHarvestSourceId());
		loadMessages();
		return new ForwardResolution("/pages/harvest.jsp");
	}
	
	/**
	 * @throws DAOException 
	 * 
	 */
	private void loadMessages() throws DAOException{
		
		List<HarvestMessageDTO> messageDTOs = DAOFactory.getDAOFactory().getHarvestMessageDAO().findHarvestMessagesByHarvestID(harvestDTO.getHarvestId());
		if (messageDTOs!=null){
			loadMessages(Harvest.FATAL, messageDTOs, fatals = new ArrayList<HarvestMessageDTO>());
			loadMessages(Harvest.ERROR, messageDTOs, errors = new ArrayList<HarvestMessageDTO>());
			loadMessages(Harvest.WARNING, messageDTOs, warnings = new ArrayList<HarvestMessageDTO>());
		}
	}

	/**
	 * 
	 * @param messageDTOs
	 * @param type
	 */
	private void loadMessages(String type, List<HarvestMessageDTO> sourceList, List<HarvestMessageDTO> destList){
		
		for (int i=0; i<sourceList.size(); i++){
			HarvestMessageDTO messageDTO = sourceList.get(i);
			if (messageDTO.getType().equals(type)){
				destList.add(messageDTO);
			}
		}
	}

	/**
	 * @return the harvestDTO
	 */
	public HarvestDTO getHarvestDTO() {
		return harvestDTO;
	}

	/**
	 * @param harvestDTO the harvestDTO to set
	 */
	public void setHarvestDTO(HarvestDTO harvestDTO) {
		this.harvestDTO = harvestDTO;
	}

	/**
	 * @return the harvestSourceDTO
	 */
	public HarvestSourceDTO getHarvestSourceDTO() {
		return harvestSourceDTO;
	}

	/**
	 * @return the fatals
	 */
	public List<HarvestMessageDTO> getFatals() {
		return fatals;
	}

	/**
	 * @return the errors
	 */
	public List<HarvestMessageDTO> getErrors() {
		return errors;
	}

	/**
	 * @return the warnings
	 */
	public List<HarvestMessageDTO> getWarnings() {
		return warnings;
	}
}
