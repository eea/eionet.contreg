package eionet.cr.web.util.factsheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import eionet.cr.common.EncodingSchemes;
import eionet.cr.common.Predicates;
import eionet.cr.dto.ResourceDTO;
import eionet.cr.util.URLUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class FactsheetUtil {
	
	/** */
	private static HashSet skipFromFactsheet;

	/**
	 * 
	 * @return
	 */
	public static List<ResourcePropertyDTO> getPropertiesForFactsheet(ResourceDTO resource){
	
		if (resource==null)
			return null;
		
		List<ResourcePropertyDTO> result = new ArrayList<ResourcePropertyDTO>();
		if (resource.isEmpty())
			return result;
		
		List<ResourcePropertyDTO> propertiesWithLabels = new ArrayList<ResourcePropertyDTO>();
		List<ResourcePropertyDTO> propertiesWithoutLabels = new ArrayList<ResourcePropertyDTO>();
		
		Iterator<String> keysIterator = resource.keySet().iterator();
		while (keysIterator.hasNext()){
			String propertyUri = keysIterator.next();
			if (!skipFromFactsheet(propertyUri)){
				
				String propertyLabel = EncodingSchemes.getLabel(propertyUri);
				
				ResourcePropertyDTO propDTO = new ResourcePropertyDTO();
				propDTO.setUri(propertyUri);
				propDTO.setLabel(propertyLabel != null ? propertyLabel : propertyUri);

				List<String> values = getDistinct(resource.get(propertyUri));
				for (int i=0; values!=null && i<values.size(); i++){
					
					String value = values.get(i);
					ResourcePropertyValueDTO valueDTO = new ResourcePropertyValueDTO();
					if (URLUtil.isURL(value)){
						valueDTO.setUrl(value);
						valueDTO.setLabel(EncodingSchemes.getLabel(value, true));
					}
					else
						valueDTO.setLabel(value);
					
					propDTO.addValue(valueDTO);
				}
				
				if (propertyLabel!=null)
					propertiesWithLabels.add(propDTO);
				else
					propertiesWithoutLabels.add(propDTO);
			}
		}
	
		Collections.sort(propertiesWithLabels);
		Collections.sort(propertiesWithoutLabels);
		result.addAll(propertiesWithLabels);
		result.addAll(propertiesWithoutLabels);
		
		String previous = null;
		for (int i=0; i<result.size(); i++){
			boolean b = false;
			if (previous!=null && previous.equals(result.get(i).getLabel()))
				b = true;
			previous = result.get(i).getLabel();
			if (b)
				result.get(i).setLabel("");
		}
		
		return result;
	}

	/**
	 * 
	 * @param propertyValues
	 * @return
	 */
	private static List<String> getDistinct(List<String> propertyValues){
		
		if (propertyValues==null || propertyValues.size()==0)
			return propertyValues;
		
		HashSet<String> hashSet = new HashSet<String>(propertyValues);
		for (int i=0; i<propertyValues.size(); i++){
			if (URLUtil.isURL(propertyValues.get(i))){
				String label = EncodingSchemes.getLabel(propertyValues.get(i));
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
			skipFromFactsheet.add(Predicates.DOC_ID);
			skipFromFactsheet.add(Predicates.FIRST_SEEN_TIMESTAMP);
			skipFromFactsheet.add(Predicates.SOURCE_ID);
			skipFromFactsheet.add(Predicates.IS_ENCODING_SCHEME);
		}
		
		return skipFromFactsheet.contains(propertyUri);
	}
}
