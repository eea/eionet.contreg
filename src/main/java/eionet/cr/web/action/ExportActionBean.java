package eionet.cr.web.action;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.postgre.helpers.FreeTextSearchHelper;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

@UrlBinding("/export.action")
public class ExportActionBean extends AbstractActionBean {
	
	String url;
	private String simpleFilter;
	
	@DefaultHandler
	public Resolution view() throws DAOException{
		return new ForwardResolution("/pages/export.jsp");
	}

	public Resolution export() throws DAOException{
		url = url + "exported";
		
		if (simpleFilter.equals("toFile")){
    		
    	} else if (simpleFilter.equals("toHomespace")){
    		
    	}
		
		return new ForwardResolution("/pages/export.jsp");
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSimpleFilter() {
		return simpleFilter;
	}

	public void setSimpleFilter(String simpleFilter) {
		this.simpleFilter = simpleFilter;
	}
}
