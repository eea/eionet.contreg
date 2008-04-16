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
import eionet.cr.web.util.display.ResourcePropertyDTO;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class Resource extends HashMap<String,List<String>>{
	
	/** */
	private static HashSet skipFromFactsheet;

	/**
	 * 
	 */
	public Resource(){
		super();
	}

	/**
	 * 
	 * @param luceneDocument
	 */
	public Resource(Document luceneDocument){
		
		if (luceneDocument!=null){
			List fields = luceneDocument.getFields();
			if (fields!=null && !fields.isEmpty()){
				for (int i=0; i<fields.size(); i++){
					Field field = (Field)fields.get(i);
					if (field!=null){
						String fieldName = new String(field.name());
						List<String> values = get(fieldName);
						if (values==null)
							values = new ArrayList<String>();
						values.add(field.stringValue());
						put(fieldName, values);
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
	
	/**
	 * 
	 * @return
	 */
	public List<ResourcePropertyDTO> getPropertiesForFactsheet(){

		List<ResourcePropertyDTO> result = new ArrayList<ResourcePropertyDTO>();
		if (isEmpty())
			return result;
		
		List<ResourcePropertyDTO> propertiesWithLabels = new ArrayList<ResourcePropertyDTO>();
		List<ResourcePropertyDTO> propertiesWithoutLabels = new ArrayList<ResourcePropertyDTO>();
		
		Iterator<String> keysIterator = keySet().iterator();
		while (keysIterator.hasNext()){
			String propertyUri = keysIterator.next();
			if (!skipFromFactsheet(propertyUri)){
				String propertyLabel = EncodingSchemes.getLabel(propertyUri);
				List<String> values = getDistinct(get(propertyUri));
				if (values!=null && values.size()>0){
					for (int i=0; i<values.size(); i++){
						
						String value = values.get(i);
						ResourcePropertyDTO propDTO = new ResourcePropertyDTO();
						propDTO.setPropertyLabel(propertyLabel != null ? propertyLabel : propertyUri);
	
						if (Util.isURL(value)){
							propDTO.setValueUrl(value);
							propDTO.setValueLabel(EncodingSchemes.getLabel(value, true));
						}
						else
							propDTO.setValueLabel(value);
						
						if (propertyLabel!=null)
							propertiesWithLabels.add(propDTO);
						else
							propertiesWithoutLabels.add(propDTO);
					}
				}
			}
		}

		Collections.sort(propertiesWithLabels);
		Collections.sort(propertiesWithoutLabels);
		result.addAll(propertiesWithLabels);
		result.addAll(propertiesWithoutLabels);
		
		String previous = null;
		for (int i=0; i<result.size(); i++){
			boolean b = false;
			if (previous!=null && previous.equals(result.get(i).getPropertyLabel()))
				b = true;
			previous = result.get(i).getPropertyLabel();
			if (b)
				result.get(i).setPropertyLabel("");
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param propertyValues
	 * @return
	 */
	private List<String> getDistinct(List<String> propertyValues){
		
		if (propertyValues==null || propertyValues.size()==0)
			return propertyValues;
		
		HashSet<String> hashSet = new HashSet(propertyValues);
		String[] distinct = hashSet.toArray(new String[hashSet.size()]);
		for (int i=0; i<distinct.length; i++){
			if (Util.isURL(distinct[i])){
				String label = EncodingSchemes.getLabel(distinct[i]);
				if (label!=null && hashSet.contains(label))
					hashSet.remove(label);
			}
		}
		
		return new ArrayList<String>(hashSet);
	}
	
	/**
	 * 
	 * @param propertyUri
	 * @return
	 */
	public static boolean skipFromFactsheet(String propertyUri){
		
		if (skipFromFactsheet==null){			
			skipFromFactsheet = new HashSet();
			skipFromFactsheet.add(Identifiers.DOC_ID);
			skipFromFactsheet.add(Identifiers.FIRST_SEEN_TIMESTAMP);
			skipFromFactsheet.add(Identifiers.SOURCE_ID);
			skipFromFactsheet.add(Identifiers.IS_ENCODING_SCHEME);
		}
		
		return skipFromFactsheet.contains(propertyUri);
	}
}
