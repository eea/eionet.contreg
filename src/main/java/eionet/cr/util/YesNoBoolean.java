package eionet.cr.util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class YesNoBoolean {
	
	/** */
	public static final String YES = "Y";
	public static final String NO = "N";

	/** */
	private String value;

	/**
	 * 
	 * @param booleanValue
	 */
	public YesNoBoolean(boolean booleanValue){
		value = booleanValue ? YesNoBoolean.YES : YesNoBoolean.NO;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getValue(){
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return getValue();
	}
	
	/**
	 * 
	 * @param b
	 * @return
	 */
	public static String parse(boolean b){
		YesNoBoolean yn = new YesNoBoolean(b);
		return yn.getValue();
	}
}
