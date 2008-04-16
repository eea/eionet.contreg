package eionet.cr.harvest.util;

import eionet.cr.common.CRRuntimeException;

/**
 * 
 * @author heinljab
 *
 */
public class HarvestMessagesMask {
	
	/** */
	private static final int POS_FATALS = 0;
	private static final int POS_ERRORS = 1;
	private static final int POS_WARNGS = 2;
	private static final int STR_LENGTH = 3;

	/** */
	private boolean fatals = false;
	private boolean errors = false;
	private boolean warngs = false;
	
	/**
	 * 
	 * @param countFatals
	 * @param countErrors
	 * @param countWarngs
	 */
	private HarvestMessagesMask(boolean fatals, boolean errors, boolean warngs){
		this.fatals = fatals;
		this.errors = errors;
		this.warngs = warngs;
	}

	/**
	 * @return the fatals
	 */
	public boolean hasFatals() {
		return fatals;
	}

	/**
	 * @return the errors
	 */
	public boolean hasErrors() {
		return errors;
	}

	/**
	 * @return the warngs
	 */
	public boolean hasWarngs() {
		return warngs;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		
		char[] chars = new char[STR_LENGTH];
		chars[POS_FATALS] = fatals ? '1' : '0';
		chars[POS_ERRORS] = errors ? '1' : '0';
		chars[POS_WARNGS] = warngs ? '1' : '0';
		
		return String.copyValueOf(chars);
	}

	/**
	 * 
	 * @param mask
	 * @return
	 */
	public static String toString(boolean fatals, boolean errors, boolean warngs){
		return (new HarvestMessagesMask(fatals, errors, warngs)).toString();
	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static HarvestMessagesMask fromString(String str){
		
		if (!isValid(str))
			throw new CRRuntimeException("Invalid input string for " + HarvestMessagesMask.class.getSimpleName() + ": " + str==null ? null : str);
		
		return new HarvestMessagesMask(str.charAt(POS_FATALS)!='0', str.charAt(POS_ERRORS)!='0', str.charAt(POS_WARNGS)!='0');
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isValid(String str){
		
		boolean result = true;
		if (str==null || str.length()!=STR_LENGTH)
			result = false;
		else if (!Character.isDigit(str.charAt(0)) || !Character.isDigit(str.charAt(1)) || !Character.isDigit(str.charAt(2)))
			result = false;
		
		return result;
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static boolean hasFatals(String str){
		return fromString(str).hasFatals();
	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static boolean hasErrors(String str){
		return fromString(str).hasErrors();
	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static boolean hasWarngs(String str){
		return fromString(str).hasWarngs();
	}
}
