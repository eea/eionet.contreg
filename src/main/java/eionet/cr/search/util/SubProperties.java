package eionet.cr.search.util;

import java.util.HashMap;
import java.util.HashSet;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubProperties extends HashMap<String,HashSet<String>>{

	/**
	 * 
	 * @param predicateUri
	 * @param parentUri
	 */
	public void add(String predicate, String subProperty){
		
		HashSet<String> subProperties = get(predicate);
		if (subProperties==null){
			subProperties = new HashSet<String>();
			put(predicate, subProperties);
		}
		
		subProperties.add(subProperty);
	}
}
