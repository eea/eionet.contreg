package eionet.cr.dto;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PredicateDTO {

	/** */
	private String value;
	
	/**
	 * 
	 * @param value
	 */
	public PredicateDTO(String value){
		this.value = value;
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return getValue();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other){
		
		if (this==other)
			return true;
		
		if (!(other instanceof PredicateDTO))
			return false;
		
		
		String otherValue = ((PredicateDTO)other).getValue();
		return getValue()==null ? otherValue==null : getValue().equals(otherValue);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode(){

		return getValue()==null ? 0 : getValue().hashCode();
	}
}
