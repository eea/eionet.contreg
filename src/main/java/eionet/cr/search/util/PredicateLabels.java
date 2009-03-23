package eionet.cr.search.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PredicateLabels extends HashMap<String,HashMap<String,HashSet<String>>>{

	/**
	 * 
	 * @param predicate
	 * @param label
	 * @param language
	 */
	public void add(String predicate, String label, String language){
		
		HashMap<String,HashSet<String>> labels = get(predicate);
		if (labels==null){
			labels = new HashMap<String,HashSet<String>>();
			put(predicate, labels);
		}
		
		HashSet<String> labelsOfLang = labels.get(language);
		if (labelsOfLang==null){
			labelsOfLang = new HashSet<String>();
			labels.put(language, labelsOfLang);
		}
		
		labelsOfLang.add(label);
	}
	
	/**
	 * 
	 * @param languagePreferences
	 * @return
	 */
	public Map<String,String> getByLanguagePreferences(List<String> languagePreferences, String defaultLanguage){
		
		Map<String,String> result = new HashMap<String,String>();
		if (languagePreferences!=null && !languagePreferences.isEmpty()){
			if (!isEmpty()){
				
				for (Iterator<String> predicates=keySet().iterator(); predicates.hasNext();){
					
					String predicate = predicates.next();
					HashMap<String,HashSet<String>> labels = get(predicate);
					
					HashSet<String> labelsOfLang = null;
					for (int i=0; labelsOfLang==null && i<languagePreferences.size(); i++){
						labelsOfLang = labels.get(languagePreferences.get(i));
					}
					if (labelsOfLang==null)
						labelsOfLang = labels.get(defaultLanguage);
					if (labelsOfLang==null)
						labelsOfLang = labels.get("");
					
					if (labelsOfLang!=null && !labelsOfLang.isEmpty())
						result.put(predicate, labelsOfLang.iterator().next());
				}
			}
		}
		
		return result;
	}
}
