package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.FactsheetSearch;
import eionet.cr.search.SearchException;
import eionet.cr.search.UriSearch;
import eionet.cr.search.util.SearchExpression;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/factsheet.action")
public class FactsheetActionBean extends AbstractActionBean{

	/** */
	private String uri;
	private SubjectDTO subject;
	
	/** */
	private Map<String,String> predicateLabels;

	/**
	 * 
	 * @return
	 * @throws SearchException
	 */
	@DefaultHandler
	public Resolution view() throws SearchException{
		
		FactsheetSearch factsheetSearch = new FactsheetSearch(uri);
		factsheetSearch.execute();
		Collection<SubjectDTO> coll = factsheetSearch.getResultList();
		if (coll!=null && !coll.isEmpty())
			subject = coll.iterator().next();

		predicateLabels = factsheetSearch.getPredicateLabels().getByLanguagePreferences(createLanguagePreferences(), "en");
		
		return new ForwardResolution("/pages/factsheet.jsp");
	}

	/**
	 * @return the resourceUri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @param resourceUri the resourceUri to set
	 */
	public void setUri(String resourceUri) {
		this.uri = resourceUri;
	}

	/**
	 * @return the resource
	 */
	public SubjectDTO getSubject() {
		return subject;
	}
	
	/**
	 * 
	 * @return
	 */
	private List<String> createLanguagePreferences(){
		
		List<String> result = new ArrayList<String>();
		
		String languagePreferences = getContext().getRequest().getHeader("Accept-Language");
		if (!StringUtils.isBlank(languagePreferences)){
			String[] languages = StringUtils.split(languagePreferences, ',');
			for (int m=0; m<languages.length; m++){
				
				String lang = languages[m];
	            
	            int i = lang.indexOf(";");
	            if (i != -1)
	            	lang = lang.substring(0, i);
	            
	            int j = lang.indexOf("-");
	            if (j == -1)
	            	j = lang.indexOf("_");
	            if (j != -1)
	            	lang = lang.substring(0, j);
	            
	            result.add(lang.toLowerCase());
			}
		}
			
		return result;
	}

	/**
	 * @return the predicateLabels
	 */
	public Map<String, String> getPredicateLabels() {
		return predicateLabels;
	}
}
