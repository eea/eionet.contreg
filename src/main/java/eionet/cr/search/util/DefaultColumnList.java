package eionet.cr.search.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import eionet.cr.util.Identifiers;
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
		type.put("property", Util.md5digest(Identifiers.RDF_TYPE));
		type.put("title", "Type");
		type.put("sortable", Boolean.TRUE);
		
		Map label = new HashMap();
		label.put("property", Util.md5digest(Identifiers.DISPLAY_LABEL));
		label.put("title", "Label");
		label.put("sortable", Boolean.TRUE);

		Map date = new HashMap();
		date.put("property", Util.md5digest(Identifiers.DC_DATE));
		date.put("title", "Date");
		date.put("sortable", Boolean.TRUE);
		
		add(type);
		add(label);
		add(date);

	}
}
