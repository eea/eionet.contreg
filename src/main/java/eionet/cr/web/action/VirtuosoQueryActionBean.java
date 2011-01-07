package eionet.cr.web.action;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dao.DAOException;

@UrlBinding("/virtuosoQuery.action")
public class VirtuosoQueryActionBean extends AbstractActionBean {
	
	private String defaultUri;
	private String query;
	
	/**
	 * 
	 * @return
	 * @throws DAOException TODO
	 */
	@DefaultHandler
	public Resolution view() throws DAOException {
		return new ForwardResolution("/pages/virtuosoQuery.jsp");
	}
	
	public StreamingResolution query() throws Exception {
		
		StringBuffer ret = new StringBuffer();
		
		if(defaultUri == null)
			defaultUri = "";
		
		if(query == null)
			query = "";
		
		String endpoint = "http://localhost:8890/sparql?default-graph-uri="+URLEncoder.encode(defaultUri,"UTF-8")+"&query="+URLEncoder.encode(query,"UTF-8");
		URL url = new URL(endpoint);
		InputStream is = url.openStream();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String line = null;
		int cnt = 0;
		while((line = in.readLine()) != null) {
			if(cnt == 0)
				ret.append("<br/><b>Query result: </b><br/>");
			ret.append(line);
			cnt++;
		}


		return new StreamingResolution("text/html", ret.toString());
	}
	
	public String getDefaultUri() {
		return defaultUri;
	}

	public void setDefaultUri(String defaultUri) {
		this.defaultUri = defaultUri;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
	

}
