package eionet.cr.util;

import java.util.Date;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class FirstSeenTimestamp{
	
	/** */
	private Integer value = null;

	/**
	 * 
	 */
	public FirstSeenTimestamp(){
		this(System.currentTimeMillis());
	}
	
	/**
	 * 
	 * @param timeInMilliseconds
	 */
	public FirstSeenTimestamp(long timeInMilliseconds){
		this.value = new Integer((int)(timeInMilliseconds / (long)1000));
	}
	
	/**
	 * 
	 * @param valueString
	 */
	public FirstSeenTimestamp(String valueString){
		this.value = Integer.valueOf(valueString);
	}
	
	/**
	 * 
	 * @param pattern
	 * @return
	 */
	public String toDateString(String pattern){
		if (pattern==null)
			return value.toString();
		else
			return Util.dateToString(new Date(value.longValue()*(long)1000), pattern);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return value.toString();
	}
	
	/**
	 * 
	 * @param valueString
	 * @param pattern
	 * @return
	 */
	public static String toDateString(String valueString, String pattern){
		if (valueString==null)
			return null;
		else if (pattern==null)
			return valueString;
		else
			return Util.dateToString(new Date(Integer.valueOf(valueString).longValue()*(long)1000), pattern);
	}
}
