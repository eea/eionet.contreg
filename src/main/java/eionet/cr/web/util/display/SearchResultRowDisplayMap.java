package eionet.cr.web.util.display;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import eionet.cr.common.Identifiers;
import eionet.cr.util.Util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SearchResultRowDisplayMap extends HashMap<String,String>{
	
	/** */
	public static final String RESOURCE_URI = "resourceUri";
	public static final String FALLBACKED_LABEL = "fallbackedLabel";

	/** */
	private HashMap<String,String> md5Map = new HashMap<String,String>();
	
	/**
	 * 
	 */
	public SearchResultRowDisplayMap(Map<String,String[]> underlyingMap){
		
		super();
		
		if (underlyingMap==null || underlyingMap.isEmpty())
			return;
		
		for (Iterator<String> iter = underlyingMap.keySet().iterator(); iter.hasNext();){
			
			String key = iter.next();
			String[] values = underlyingMap.get(key);
			String resultValue = Util.arrayToString(Util.pruneUrls(values), ", ");
			put(key, resultValue);
			md5Map.put(Util.md5digest(key), key);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.HashMap#get(java.lang.Object)
	 */
	public String get(Object key){
		
		if (key==null)
			return null;

		String keyString = key.toString();
		if (keyString.equals(RESOURCE_URI))
			return super.get(Identifiers.DOC_ID);
		else if (keyString.equals(FALLBACKED_LABEL)){
			String s = super.get(Identifiers.DC_TITLE);
			s = s==null ? super.get(Identifiers.RDFS_LABEL) : s;
			return s==null ? "" : s;
		}
		else
			return getByMd5(keyString);
	}
	
	/**
	 * 
	 * @param md5Key
	 * @return
	 */
	private String getByMd5(String md5Key){
		if (md5Key==null)
			return null;
		
		String key = md5Map.get(md5Key);
		return key==null ? null : super.get(key);
	}
}
