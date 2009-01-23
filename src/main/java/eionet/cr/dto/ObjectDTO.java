package eionet.cr.dto;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class ObjectDTO {

	/** */
	private String value;
	private String language;
	private boolean literal;
	private boolean anonymous;
	private String derivSource;
	
	/**
	 * 
	 * @param value
	 * @param language
	 * @param literal
	 * @param anonymous
	 */
	public ObjectDTO(String value, String language, boolean literal, boolean anonymous){
		
		this.value = value;
		this.language = language;
		this.literal = literal;
		this.anonymous = anonymous;
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}
	/**
	 * @return the literal
	 */
	public boolean isLiteral() {
		return literal;
	}
	/**
	 * @return the anonymous
	 */
	public boolean isAnonymous() {
		return anonymous;
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
		
		if (!(other instanceof ObjectDTO))
			return false;
		
		
		String otherValue = ((ObjectDTO)other).getValue();
		return getValue()==null ? otherValue==null : getValue().equals(otherValue);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode(){

		return getValue()==null ? 0 : getValue().hashCode();
	}

	/**
	 * @return the derivSource
	 */
	public String getDerivSource() {
		return derivSource;
	}

	/**
	 * @param derivSource the derivSource to set
	 */
	public void setDerivSource(String derivSource) {
		this.derivSource = derivSource;
	}
}
