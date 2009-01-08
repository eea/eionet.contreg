package eionet.cr.search.util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SearchString {

	/** */
	private String str;
	private boolean exactPhrase = false;
	
	/**
	 * 
	 * @param s
	 */
	public SearchString(String str){
		
		this.str = str==null ? null : str.trim();
		exactPhrase = this.str.startsWith("\"") && this.str.endsWith("\"");
		if (exactPhrase){
			str = str.substring(1, str.length()-1);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isEmpty(){
		return str==null || str.trim().length()==0;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isExactPhrase(){
		return exactPhrase; 
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		return str;
	}
}
