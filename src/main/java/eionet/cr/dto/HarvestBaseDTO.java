package eionet.cr.dto;

/**
 * 
 * @author heinljab
 *
 */
public class HarvestBaseDTO {

	/** */
	private boolean hasFatals;
	private boolean hasErrors;
	private boolean hasWarnings;
	/**
	 * @return the hasFatals
	 */
	public boolean isHasFatals() {
		return hasFatals;
	}
	/**
	 * @param hasFatals the hasFatals to set
	 */
	public void setHasFatals(boolean hasFatals) {
		this.hasFatals = hasFatals;
	}
	/**
	 * @return the hasErrors
	 */
	public boolean isHasErrors() {
		return hasErrors;
	}
	/**
	 * @param hasErrors the hasErrors to set
	 */
	public void setHasErrors(boolean hasErrors) {
		this.hasErrors = hasErrors;
	}
	/**
	 * @return the hasWarnings
	 */
	public boolean isHasWarnings() {
		return hasWarnings;
	}
	/**
	 * @param hasWarnings the hasWarnings to set
	 */
	public void setHasWarnings(boolean hasWarnings) {
		this.hasWarnings = hasWarnings;
	}
}
