package eionet.cr.web.util.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import eionet.cr.common.Identifiers;
import eionet.cr.common.Md5Map;
import eionet.cr.util.Util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SearchResultRow extends HashMap<String,String>{
	
	/** */
	public static final String RESOURCE_URI = "resourceUri";
	public static final String RESOURCE_LABEL = "label";

	/**
	 * 
	 * @param underlyingMap
	 */
	public SearchResultRow(Map<String,String[]> underlyingMap){
		
		super();
		
		if (underlyingMap==null || underlyingMap.isEmpty())
			return;
		
		for (Iterator<String> iter = underlyingMap.keySet().iterator(); iter.hasNext();){
			String key = iter.next();
			String value = toValuesString(underlyingMap.get(key));
			put(key, value);
			Md5Map.addValue(key);
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
		if (Md5Map.hasKey(keyString))
			return super.get(Md5Map.getValue(keyString));
		else if (keyString.equals(RESOURCE_URI))
			return super.get(Identifiers.DOC_ID);
		else if (keyString.equals(RESOURCE_LABEL)){
			String s = super.get(Identifiers.DC_TITLE);
			s = s==null ? super.get(Identifiers.RDFS_LABEL) : s;
			return s==null ? "" : s;
		}
		else
			return super.get(keyString);
	}
	
	/**
	 * 
	 * @param rawValues
	 * @return
	 */
	private String toValuesString(String[] rawValues){
		
		if (rawValues==null || rawValues.length==0)
			return null;
		
		StringBuffer buf = new StringBuffer();
		HashSet<String> set = new HashSet<String>(Arrays.asList(Util.pruneUrls(rawValues)));
		for (Iterator<String> i=set.iterator(); i.hasNext();){
			if (buf.length()>0)
				buf.append(", ");
			buf.append(i.next());
		}
		
		return buf.toString();
	}

	/**
	 * 
	 * @param list
	 * @return
	 */
	public static List<SearchResultRow> convert(List<Map<String,String[]>> list){
		
		if (list==null)
			return null;
		
		List<SearchResultRow> resultList = new ArrayList<SearchResultRow>();
		for (int i=0; i<list.size(); i++){
			Map<String,String[]> map = list.get(i);
			if (!map.isEmpty())
				resultList.add(new SearchResultRow(map));
		}
		return resultList;
	}
	
}
