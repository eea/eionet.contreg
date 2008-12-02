package eionet.cr.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import eionet.cr.harvest.util.RDFResource;
import eionet.cr.harvest.util.RDFResourceProperty;
import eionet.cr.index.EncodingSchemes;
import eionet.cr.util.URLUtil;
import eionet.cr.web.util.factsheet.ResourcePropertyDTO;
import eionet.cr.web.util.factsheet.ResourcePropertyValueDTO;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class ResourceDTO extends HashMap<String,List<String>>{
	
	/**
	 */
	public static interface SpecialKeys{
		
		public static final String RESOURCE_URI = "resourceUri";
		public static final String RESOURCE_URL = "resourceUrl";
		public static final String RESOURCE_TITLE = "resourceTitle";
	}
	
	/**
	 * 
	 * @param luceneDocument
	 */
	public ResourceDTO(Document luceneDocument){
		
		super();
		
		if (luceneDocument==null)
			return;
		
		List fields = luceneDocument.getFields();
		if (fields!=null && !fields.isEmpty()){
			for (int i=0; i<fields.size(); i++){
				Field field = (Field)fields.get(i);
				if (field!=null){
					String fieldName = new String(field.name());
					List<String> values = super.get(fieldName);
					if (values==null){
						values = new ArrayList<String>();
						values.add(field.stringValue());
						super.put(fieldName, values);
					}
					else if (!values.contains(field.stringValue()))
						values.add(field.stringValue());
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.HashMap#get(java.lang.Object)
	 */
	public List<String> get(Object keyObject){
		
		List<String> result = null;
		if (keyObject!=null){
			
			String key = keyObject.toString();
			if (Md5Map.hasKey(key))
				key = Md5Map.getValue(key);
			
			if (key.equals(SpecialKeys.RESOURCE_URI)){
				result = super.get(Predicates.DOC_ID);
			}
			else if (key.equals(SpecialKeys.RESOURCE_TITLE)){
				result = super.get(Predicates.DC_TITLE);
				if (deepEmpty(result))
					result = super.get(Predicates.RDFS_LABEL);
			}
			else if (key.equals(SpecialKeys.RESOURCE_URL)){
				result = super.get(Predicates.DOC_ID);
				if (result==null || result.isEmpty() || !URLUtil.isURL(result.get(0))){
					result = super.get(Predicates.DC_IDENTIFIER);
					if (result!=null && !result.isEmpty() && !URLUtil.isURL(result.get(0)))
						result = null;
				}
			}
			else
				result = super.get(key);
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public String getValue(String key){
		
		List<String> values = get(key);
		return values!=null && values.size()>0 ? values.get(0) : null;
	}

	/**
	 * 
	 * @return
	 */
	public String getUri(){
		return getValue(Predicates.DOC_ID);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getUrl(){
		return getValue(SpecialKeys.RESOURCE_URL);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getTitle(){
		return getValue(SpecialKeys.RESOURCE_TITLE);
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public List<String> getDistinctLiteralValues(String key){
		return asDistinctLiteralValues(get(key));
	}
	
	/**
	 * 
	 * @param list
	 * @return
	 */
	public static List<String> asDistinctLiteralValues(List list){
		
		if (list==null || list.isEmpty())
			return list;
		
		// first we pick out only literals (using HashSet ensures we get only distinct ones)
		HashSet set = new HashSet();
		for (int i=0; i<list.size(); i++){
			String s = list.get(i).toString();
			if (!URLUtil.isURL(s))
				set.add(s);
		}
		
		// if no distinct literals were found at all, return the list as it was given
		if (set.isEmpty())
			return list;
		
		return new ArrayList<String>(set);
	}

	/**
	 * 
	 * @param list
	 * @return
	 */
	private static boolean deepEmpty(List<String> list){
		
		if (list==null || list.isEmpty())
			return true;
		
		for (int i=0; i<list.size(); i++){
			String s = list.get(i);
			if (s!=null && s.trim().length()>0)
				return false;
		}
		
		return true;
	}
}
