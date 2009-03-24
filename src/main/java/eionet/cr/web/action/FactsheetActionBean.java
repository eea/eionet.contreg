package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import eionet.cr.search.util.PredicateLabels;
import eionet.cr.search.util.SubProperties;
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
	private SubProperties subProperties;

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

		predicateLabels = factsheetSearch.getPredicateLabels().getByLanguagePreferences(createPreferredLanguages(), "en");
		subProperties = factsheetSearch.getSubProperties();
		
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
	private Set<String> createPreferredLanguages(){
		
		Set<String> result = new HashSet<String>();
		
		String languagePreferences = getContext().getRequest().getHeader("Accept-Language");
		if (!StringUtils.isBlank(languagePreferences)){
			String[] languages = StringUtils.split(languagePreferences, ',');
			for (int i=0; i<languages.length; i++){
				result.add(PredicateLabels.parseHTTPAcceptedLanguage(languages[i]));
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

	/**
	 * @return the subProperties
	 */
	public SubProperties getSubProperties() {
		return subProperties;
	}
}
