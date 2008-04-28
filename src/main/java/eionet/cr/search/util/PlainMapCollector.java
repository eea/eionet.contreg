package eionet.cr.search.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;

import eionet.cr.util.DocumentListener;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PlainMapCollector implements DocumentListener {
	
	/** */
	private List<Map<String,String[]>> resultList;

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.DocumentListener#handleDocument(org.apache.lucene.document.Document)
	 */
	public void handleDocument(Document document) {
		
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

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.DocumentListener#done()
	 */
	public void done() {
	}

	/**
	 * @return the resultListAAA
	 */
	public List<Map<String, String[]>> getResultList() {
		return resultList;
	}
}
