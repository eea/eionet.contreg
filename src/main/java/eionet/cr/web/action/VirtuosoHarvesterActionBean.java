package eionet.cr.web.action;

import virtuoso.jena.driver.VirtGraph;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;

@UrlBinding("/virtuosoHarvester.action")
public class VirtuosoHarvesterActionBean extends AbstractActionBean {
	
	private String sourceUrl;
	
	/**
	 * 
	 * @return
	 * @throws DAOException TODO
	 */
	@DefaultHandler
	public Resolution view() throws DAOException {
		return new ForwardResolution("/pages/virtuosoHarvester.jsp");
	}
	
	public Resolution harvest() throws DAOException {
		
		String url = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_URL);
		String username = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_USR);
		String password = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_PWD);
		
		VirtGraph graph = new VirtGraph ("CR3", url, username, password);
		graph.clear ();

		System.out.println("Begin read from: "+sourceUrl);
		graph.read(sourceUrl, "RDF/XML");
		System.out.println ("\t\t\t Done.");
		
		addSystemMessage("Successfully harvested!");
		
		return new ForwardResolution("/pages/virtuosoHarvester.jsp");
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
	
	

}
