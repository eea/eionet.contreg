package eionet.cr.dto;

import eionet.cr.harvest.Harvest;

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
			if (messageType.equals(Harvest.FATAL))
				dto.setHasFatals(Boolean.TRUE);
			else if (messageType.equals(Harvest.ERROR))
				dto.setHasErrors(Boolean.TRUE);
			else if (messageType.equals(Harvest.WARNING))
				dto.setHasWarnings(Boolean.TRUE);
		}
	}
}
