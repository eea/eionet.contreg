package eionet.cr.search.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PlainMapCollector extends HitsCollector {
	
	/** */
	private List<Map<String,String[]>> resultList;

	/**
	 * @return the resultList
	 */
	public List<Map<String, String[]>> getResultList() {
		return resultList;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.util.HitsCollector#collectDocument(org.apache.lucene.document.Document)
	 */
	public void collectDocument(Document document) {
		
		if (document==null)
			return;
		
		Map<String,String[]> map = new Hashtable<String,String[]>();
		
		List allFields = document.getFields();
		for (int j=0; allFields!=null && j<allFields.size(); j++){
			Fieldable field = (Fieldable)allFields.get(j);
			String fieldName = field.name();
			String[] fieldValues = document.getValues(fieldName);
			if (fieldValues!=null && fieldValues.length>0)
				map.put(field.name(), fieldValues);
		}
		
		if (resultList==null)
			resultList = new ArrayList<Map<String,String[]>>();
		
		resultList.add(map);
	}
}
