package eionet.cr.search.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;

import eionet.cr.common.Identifiers;
import eionet.cr.index.EncodingSchemes;
import eionet.cr.util.DocumentListener;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class EntriesCollector extends HitsCollector{

	/** */
	private Vector<Hashtable<String,Hashtable<String,Vector<String>>>> resultVector;

	/**
	 * 
	 * @return
	 */
	public Vector getResultVector() {
		return resultVector;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.util.HitsCollector#collectDocument(org.apache.lucene.document.Document)
	 */
	public void collectDocument(Document document) {
		
		if (document==null)
			return;
		
		Hashtable<String,Vector<String>> fieldsHashtable = new Hashtable<String,Vector<String>>();
		
		String docId = null;
		List allFields = document.getFields();
		for (int i=0; allFields!=null && i<allFields.size(); i++){
			
			Fieldable field = (Fieldable)allFields.get(i);
			String fieldName = field.name();
			String[] fieldValues = document.getValues(fieldName);
			
			if (fieldValues!=null){
				
				HashSet<String> valueSet = new HashSet<String>();
				for (int j=0; j<fieldValues.length; j++){
					
					String s = EncodingSchemes.getLabel(fieldValues[j], true);
					valueSet.add(s.trim());
				}
				fieldsHashtable.put(fieldName, new Vector<String>(valueSet));
				
				if (fieldName.equals(Identifiers.DOC_ID))
					docId = fieldValues[0];
			}
		}
		
		if (docId!=null){
			Hashtable h = new Hashtable();
			h.put(docId, fieldsHashtable);
			if (resultVector==null)
				resultVector = new Vector<Hashtable<String,Hashtable<String,Vector<String>>>>();
			resultVector.add(h);
		}
	}
}
