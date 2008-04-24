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
import eionet.cr.util.Util;
import eionet.cr.web.util.factsheet.ResourcePropertyDTO;
import eionet.cr.web.util.factsheet.ResourcePropertyValueDTO;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class ResourceMap extends HashMap<String,List<String>>{
	
	/**
	 * 
	 */
	public ResourceMap(){
		super();
	}

	/**
	 * 
	 * @param luceneDocument
	 */
	public ResourceMap(Document luceneDocument){
		
		if (luceneDocument!=null){
			List fields = luceneDocument.getFields();
			if (fields!=null && !fields.isEmpty()){
				for (int i=0; i<fields.size(); i++){
					Field field = (Field)fields.get(i);
					if (field!=null){
						String fieldName = new String(field.name());
						List<String> values = get(fieldName);
						if (values==null){
							values = new ArrayList<String>();
							values.add(field.stringValue());
							put(fieldName, values);
						}
						else if (!values.contains(field.stringValue()))
							values.add(field.stringValue());
					}
				}
			}
		}
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
		
		String result = getValue(Identifiers.DOC_ID);
		if (result==null || !Util.isURL(result))
			result = getValue(Identifiers.DC_IDENTIFIER);
		if (result==null || !Util.isURL(result))
			result = getValue(Identifiers.DC_SOURCE);
		
		if (result!=null && !Util.isURL(result))
			result = null;
		
		return result;
	}
	
	/**
	 * 
	 * @param propertyName
	 * @return
	 */
	public String getValue(String propertyName){
		
		if (propertyName==null)
			return null;
		else{
			List<String> values = get(propertyName);
			return values!=null && values.size()>0 ? values.get(0) : null;
		}
	}
}
