package eionet.cr.web.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class DisplayUtil {

	/**
	 * 
	 * @param list
	 * @return
	 */
	public static List<SearchResultRowDisplayMap> listForDisplay(List<Map<String,String[]>> list){
		
		if (list==null)
			return null;
		
		List<SearchResultRowDisplayMap> resultList = new ArrayList<SearchResultRowDisplayMap>();
		for (int i=0; i<list.size(); i++){
			Map<String,String[]> map = list.get(i);
			if (!map.isEmpty())
				resultList.add(new SearchResultRowDisplayMap(map));
		}
		return resultList;
	}
}
