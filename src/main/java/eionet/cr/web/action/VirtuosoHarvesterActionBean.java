package eionet.cr.web.action;

import virtuoso.jena.driver.VirtGraph;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
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
		VirtGraph graph = new VirtGraph ("Example2", "jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2", "dba", "dba");
		graph.clear ();

		System.out.println("Begin read from 'http://rod.eionet.europa.eu/spatial'  ");
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
