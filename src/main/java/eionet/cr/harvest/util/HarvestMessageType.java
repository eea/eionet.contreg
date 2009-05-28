package eionet.cr.harvest.util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public enum HarvestMessageType {

	FATAL("ftl"), ERROR("err"), WARNING("wrn"), INFO("inf");
	
	/** */
	private String value;
	
	/**
	 * 
	 * @param value1
	 */
	HarvestMessageType(String value){
		this.value = value;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	public String toString(){
		return value;
	}
}
