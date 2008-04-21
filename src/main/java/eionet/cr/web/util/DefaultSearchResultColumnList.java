package eionet.cr.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import eionet.cr.common.Identifiers;
import eionet.cr.util.Util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class DefaultSearchResultColumnList extends ArrayList<SearchResultColumn>{

	/**
	 * 
	 */
	public DefaultSearchResultColumnList(){
		
		super();
		
		SearchResultColumn col = new SearchResultColumn();
		col.setPropertyUri(Identifiers.RDF_TYPE);
		col.setPropertyKey(Util.md5digest(Identifiers.RDF_TYPE));
		col.setTitle("Type");
		col.setSortable(true);
		add(col);
		
		col = new SearchResultColumn();
		col.setPropertyUri(SearchResultRowDisplayMap.FALLBACKED_LABEL);
		col.setPropertyKey(SearchResultRowDisplayMap.FALLBACKED_LABEL);
		col.setTitle("Label");
		col.setSortable(true);
		add(col);

		col = new SearchResultColumn();
		col.setPropertyUri(Identifiers.DC_DATE);
		col.setPropertyKey(Util.md5digest(Identifiers.DC_DATE));
		col.setTitle("Date");
		col.setSortable(true);
		add(col);
	}
}
