package eionet.cr.web.action;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.common.Identifiers;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * 
 * @author altnyris
 *
 */
@UrlBinding("/recentAdditions.action")
public class RecentAdditionsActionBean extends AbstractSearchActionBean {
	
	/** */
	private static Log logger = LogFactory.getLog(RecentAdditionsActionBean.class);
	
	/** */
	public Map<String, String> typeTitles;
	public String type;
	
	/**
	 * 
	 * @return
	 */
	@DefaultHandler
	public Resolution init(){
		typeTitles = new LinkedHashMap<String, String>();
		typeTitles.put(Identifiers.ROD_DELIVERY_CLASS, "Deliveries");
		typeTitles.put(Identifiers.ROD_OBLIGATION_CLASS, "Obligations");
		typeTitles.put(Identifiers.REPORTS_FULL_REPORT, "Full reports");
		typeTitles.put(Identifiers.NEWS_REPORT, "News releases");
		
		return new ForwardResolution("/pages/recent.jsp");
	}

	public Map<String, String> getTypeTitles() {
		return typeTitles;
	}

	public void setTypeTitles(Map<String, String> typeTitles) {
		this.typeTitles = typeTitles;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
