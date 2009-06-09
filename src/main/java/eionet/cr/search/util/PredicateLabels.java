/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.search.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		
		String parsedLanguage = parseHTTPAcceptedLanguage(language);
		HashSet<String> labelsOfLang = labels.get(parsedLanguage);
		if (labelsOfLang==null){
			labelsOfLang = new HashSet<String>();
			labels.put(parsedLanguage, labelsOfLang);
		}
		
		labelsOfLang.add(label);
	}
	
	/**
	 * 
	 * @param preferredLanguages
	 * @return
	 */
	public Map<String,String> getByLanguagePreferences(Set<String> preferredLanguages, String defaultLanguage){
		
		Map<String,String> result = new HashMap<String,String>();
		if (preferredLanguages!=null && !preferredLanguages.isEmpty()){
			if (!isEmpty()){
				
				for (Iterator<String> predicates=keySet().iterator(); predicates.hasNext();){
					
					String predicate = predicates.next();
					HashMap<String,HashSet<String>> labels = get(predicate);
					
					HashSet<String> labelsOfLang = null;
					for (Iterator<String> prefLanguages=preferredLanguages.iterator(); labelsOfLang==null && prefLanguages.hasNext();){
						labelsOfLang = labels.get(prefLanguages.next());
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
	
	/**
	 * 
	 * @param language
	 */
	public static String parseHTTPAcceptedLanguage(String httpAcceptedLanguage){
		
		String result = new String(httpAcceptedLanguage);
		
		/* ignore quality value which is separated by ';' */
		
        int j = result.indexOf(";");
        if (j != -1)
        	result = result.substring(0, j);

        /* ignore language refinement (e.g. en-US, en_UK) which is separated either by '-' or '_' */
        
        j = result.indexOf("-");
        if (j<0){
        	j = result.indexOf("_");
        }
        if (j>=0){
        	result = result.substring(0, j);
        }
        
		return result.toLowerCase();
	}
}
