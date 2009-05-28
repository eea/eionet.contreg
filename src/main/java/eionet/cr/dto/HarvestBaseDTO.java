package eionet.cr.dto;

import eionet.cr.harvest.Harvest;
import eionet.cr.harvest.util.HarvestMessageType;

/**
 * 
 * @author heinljab
 *
 */
public class HarvestBaseDTO {

	/** */
	private Boolean hasFatals;
	private Boolean hasErrors;
	private Boolean hasWarnings;
	/**
	 * @return the hasFatals
	 */
	public Boolean getHasFatals() {
		return hasFatals;
	}
	/**
	 * @param hasFatals the hasFatals to set
	 */
	public void setHasFatals(Boolean hasFatals) {
		this.hasFatals = hasFatals;
	}
	/**
	 * @return the hasErrors
	 */
	public Boolean getHasErrors() {
		return hasErrors;
	}
	/**
	 * @param hasErrors the hasErrors to set
	 */
	public void setHasErrors(Boolean hasErrors) {
		this.hasErrors = hasErrors;
	}
	/**
	 * @return the hasWarnings
	 */
	public Boolean getHasWarnings() {
		return hasWarnings;
	}
	/**
	 * @param hasWarnings the hasWarnings to set
	 */
	public void setHasWarnings(Boolean hasWarnings) {
		this.hasWarnings = hasWarnings;
	}
	
	/**
	 * 
	 * @param dto
	 * @param messageType
	 */
	public static final void addMessageType(HarvestBaseDTO dto, String messageType){
		
		if (dto!=null && messageType!=null){
			if (messageType.equals(HarvestMessageType.FATAL.toString()))
				dto.setHasFatals(Boolean.TRUE);
			else if (messageType.equals(HarvestMessageType.ERROR.toString()))
				dto.setHasErrors(Boolean.TRUE);
			else if (messageType.equals(HarvestMessageType.WARNING.toString()))
				dto.setHasWarnings(Boolean.TRUE);
		}
	}
}
