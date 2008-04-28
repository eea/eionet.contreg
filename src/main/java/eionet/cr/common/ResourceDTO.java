package eionet.cr.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

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
		public static final String FALLBACKED_LABEL = "fallbackedLabel";
	}
	
	/**
	 * 
	 */
	public ResourceDTO(){
		super();
	}

	/**
	 * 
	 * @param luceneDocument
	 */
	public ResourceDTO(Document luceneDocument){
		
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
			
			if (key.equals(SpecialKeys.RESOURCE_URI)){
				result = super.get(Identifiers.DOC_ID);
			}
			else if (key.equals(SpecialKeys.FALLBACKED_LABEL)){
				result = super.get(Identifiers.DC_TITLE);
				if (deepEmpty(result))
					result = super.get(Identifiers.RDFS_LABEL);
			}
			else if (key.equals(SpecialKeys.RESOURCE_URL)){
				result = super.get(Identifiers.DOC_ID);
				if (result==null || result.isEmpty() || !URLUtil.isURL(result.get(0))){
					result = super.get(Identifiers.DC_IDENTIFIER);
					if (result!=null && !result.isEmpty() && !URLUtil.isURL(result.get(0)))
						result = null;
				}
			}
			else if (Md5Map.hasKey(key))
				result = super.get(Md5Map.getValue(key));
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
		return getValue(Identifiers.DOC_ID);
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
	public String getFallbackedLabel(){
		return getValue(SpecialKeys.FALLBACKED_LABEL);
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
