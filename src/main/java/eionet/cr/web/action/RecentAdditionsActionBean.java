package eionet.cr.web.action;

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	public Hashtable<String, String> typeTitles;
	
	String typeDelivery = "http://rod.eionet.eu.int/schema.rdf#Delivery";
	String typeObligation = "http://rod.eionet.eu.int/schema.rdf#Obligation";
	String typeFullReport = "http://reports.eea.eu.int/reports_rdf?nr=10000#Full%20Report";
	String typeNewsReport = "http://purl.org/rss/1.0/item";
	
	/**
	 * 
	 * @return
	 */
	@DefaultHandler
	public Resolution init(){
		typeTitles = new Hashtable();
		typeTitles.put(typeDelivery, "Deliveries");
		typeTitles.put(typeObligation, "Obligations");
		typeTitles.put(typeFullReport, "Full reports");
		typeTitles.put(typeNewsReport, "News releases");
		
		return new ForwardResolution("/pages/recent.jsp");
	}

	public Hashtable<String, String> getTypeTitles() {
		return typeTitles;
	}

	public void setTypeTitles(Hashtable<String, String> typeTitles) {
		this.typeTitles = typeTitles;
	}

}
