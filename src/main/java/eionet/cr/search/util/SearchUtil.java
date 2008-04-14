package eionet.cr.search.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.Hits;

import eionet.cr.search.Searcher;
import eionet.cr.util.CRRuntimeException;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SearchUtil {

	/**
	 * 
	 * @param list
	 * @return
	 */
	public static List<Map<String,String>> listForDisplay(List<Map<String,String[]>> list){
		
		if (list==null)
			return null;
		
		List<Map<String,String>> resultList = new ArrayList<Map<String,String>>();
		for (int i=0; i<list.size(); i++){
			Map<String,String[]> map = list.get(i);
			if (!map.isEmpty())
				resultList.add(new SearchResultRowDisplayMap(map));
		}
		return resultList;
	}

	/**
	 * 
	 * @param analyzerClassName
	 * @return
	 */
	public static Analyzer createAnalyzer(String analyzerClassName){
		
		Object result = null;
		if (analyzerClassName!=null){
			try{
				Class classDefinition = Class.forName(analyzerClassName);
				result = classDefinition.newInstance();
			}
			catch (Throwable t){
				throw new CRRuntimeException(t.toString(), t);
			}
		}
		
		return (Analyzer)result;
	}

	/**
	 * 
	 * @param hits
	 * @return
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	public static List<Map<String,String[]>> processHits(Hits hits) throws CorruptIndexException, IOException{
		
		List<Map<String,String[]>> resultList = new ArrayList<Map<String,String[]>>();
		if (hits!=null && hits.length()>0){
			for (int i=0; hits!=null && i<hits.length() && i<Searcher.MAX_RESULT_SET_SIZE; i++){
				
				Document doc = hits.doc(i);
				Map<String,String[]> map = new Hashtable<String,String[]>();
				List allFields = doc.getFields();
				
				for (int j=0; allFields!=null && j<allFields.size(); j++){
					Fieldable field = (Fieldable)allFields.get(j);
					String fieldName = field.name();
					String[] fieldValues = doc.getValues(fieldName);
					if (fieldValues!=null && fieldValues.length>0)
						map.put(field.name(), fieldValues);
				}
				
				resultList.add(map);
			}
		}
			
		return resultList;
	}

}
