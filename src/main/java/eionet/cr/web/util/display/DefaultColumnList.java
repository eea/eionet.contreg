package eionet.cr.web.util.display;

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
public class DefaultColumnList extends ArrayList{

	/**
	 * 
	 */
	public DefaultColumnList(){
		
		super();
		
		Map type = new HashMap();
		type.put("property", Identifiers.RDF_TYPE);
		type.put("propertyMd5", Util.md5digest(Identifiers.RDF_TYPE));
		type.put("title", "Type");
		type.put("sortable", Boolean.TRUE);
		
		Map label = new HashMap();
		label.put("property", SearchResultRowDisplayMap.FALLBACKED_LABEL);
		label.put("propertyMd5", SearchResultRowDisplayMap.FALLBACKED_LABEL);
		label.put("title", "Label");
		label.put("sortable", Boolean.TRUE);

		Map date = new HashMap();
		date.put("property", Identifiers.DC_DATE);
		date.put("propertyMd5", Util.md5digest(Identifiers.DC_DATE));
		date.put("title", "Date");
		date.put("sortable", Boolean.TRUE);
		
		add(type);
		add(label);
		add(date);
	}
}
