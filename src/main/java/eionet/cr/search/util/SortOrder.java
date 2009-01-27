package eionet.cr.search.util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public enum SortOrder {

	ASCENDING("asc"), DESCENDING("desc");
	
	/** */
	private String s;
	
	/**
	 * 
	 * @param s
	 */
	SortOrder(String s){
		this.s = s;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	public String toString(){
		return s;
	}

	/**
	 * 
	 * @return
	 */
	public String toSQL(){
		return s;
	}
	
	/**
	 * 
	 * @return
	 */
	public SortOrder toOpposite(){
		if (this.equals(ASCENDING))
			return DESCENDING;
		else
			return ASCENDING;
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static SortOrder parse(String s){
		
		if (s==null)
			return null;
		else if (s.equals(ASCENDING.toString()))
			return ASCENDING;
		else if (s.equals(DESCENDING.toString()))
			return DESCENDING;
		else
			throw new IllegalArgumentException("Unknown sort order: " + s);
	}
}
