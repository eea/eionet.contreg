package eionet.cr.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SearchResultRowDisplayMap extends HashMap<String,String>{

	/**
	 * 
	 */
	public SearchResultRowDisplayMap(Map<String,String[]> underlyingMap){
		
		super();
		
		if (underlyingMap==null || underlyingMap.isEmpty())
			return;
		
		for (Iterator<String> iter = underlyingMap.keySet().iterator(); iter.hasNext();){
			
			String key = iter.next();
			String[] values = underlyingMap.get(key);
			String resultValue = Util.arrayToString(Util.pruneUrls(values), ", ");
			this.put(Util.md5digest(key), resultValue);
		}
		
		if (!this.isEmpty()){
			String displayLabel = this.get(Util.md5digest(Identifiers.DC_TITLE));
			if (displayLabel==null || displayLabel.length()==0)
				displayLabel = this.get(Util.md5digest(Identifiers.RDFS_LABEL));
			this.put(Util.md5digest(Identifiers.DISPLAY_LABEL), displayLabel==null ? "" : displayLabel);
		}
	}
}
