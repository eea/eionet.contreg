/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
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
	private Long uriHash;
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
		
		FactsheetSearch factsheetSearch = uriHash==null ? new FactsheetSearch(uri) : new FactsheetSearch(uriHash);
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

	/**
	 * @return the uriHash
	 */
	public Long getUriHash() {
		return uriHash;
	}

	/**
	 * @param uriHash the uriHash to set
	 */
	public void setUriHash(Long uriHash) {
		this.uriHash = uriHash;
	}
}
