/*
 * The contents of this file are subject uri the Mozilla Public
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

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.SearchException;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.search.util.SortOrder;
import eionet.cr.search.util.UriLabelPair;
import eionet.cr.util.Hashes;
import eionet.cr.util.PagingRequest;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.URIUtil;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;
import eionet.cr.web.util.columns.ReferringPredicatesColumn;
import eionet.cr.web.util.columns.SearchResultColumn;
import eionet.cr.web.util.columns.SubjectPredicateColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/references.action")
public class ReferencesActionBean extends AbstractSearchActionBean<SubjectDTO> {
	
	/** */
	private static final String REFERRING_PREDICATES = ReferencesActionBean.class.getName() + ".referringPredicates";
	private static final String PREV_OBJECT = ReferencesActionBean.class.getName() + ".previousObject";

	/** */
	private String uri;
	private long anonHash;
	private SubjectDTO subject;
	private boolean noCriteria;

	/** */
	private HashMap<String,List<String>> referringPredicates;
	
	/** */
	private Map<String,String> predicateLabels;
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 */
	public Resolution search() throws SearchException {
		
		if (StringUtils.isBlank(uri) && anonHash==0){
			
			noCriteria = true;
			addCautionMessage("Resource identifier not specified!");
		}
		else{
			try{
				Pair<Integer, List<SubjectDTO>> searchResult =
					DAOFactory.get().getDao(SearchDAO.class).searchReferences(
						anonHash==0 ? Hashes.spoHash(uri) : anonHash,
								new PagingRequest(getPageN()),
								new SortingRequest(getSortP(), SortOrder.parse(getSortO())));

				resultList = searchResult.getRight();
				matchCount = searchResult.getLeft();

				HashSet<Long> subjectHashes = new HashSet<Long>();
				for (SubjectDTO subject : resultList){
					subjectHashes.add(subject.getUriHash());
				}
				predicateLabels = DAOFactory.get().getDao(HelperDAO.class).getPredicateLabels(
						subjectHashes).getByLanguages(getAcceptedLanguages());
			}
			catch (DAOException e){
				throw new SearchException(e.toString(), e);
			}
		}
		
		return new ForwardResolution("/pages/references.jsp");
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
	 */
	public List<SearchResultColumn> getColumns() throws SearchException {
		
		ArrayList<SearchResultColumn> list = new ArrayList<SearchResultColumn>();

		/* let's always include rdf:type and rdfs:label in the columns */

		SubjectPredicateColumn predCol = new SubjectPredicateColumn();
		predCol.setPredicateUri(Predicates.RDF_TYPE);
		predCol.setTitle("Type");
		predCol.setSortable(true);
		list.add(predCol);

		predCol = new SubjectPredicateColumn();
		predCol.setPredicateUri(Predicates.RDFS_LABEL);
		predCol.setTitle("Title");
		predCol.setSortable(true);
		list.add(predCol);
		
		SearchResultColumn col = new ReferringPredicatesColumn(this);
		col.setTitle("Relationship");
		col.setSortable(true);
		list.add(col);		
		
		return list;
	}

	/**
	 * @return the predicateLabels
	 */
	public Map<String, String> getPredicateLabels() {
		return predicateLabels;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * @return the anonHash
	 */
	public long getAnonHash() {
		return anonHash;
	}

	/**
	 * @param anonHash the anonHash to set
	 */
	public void setAnonHash(long hash) {
		this.anonHash = hash;
	}

	/**
	 * @return the subject
	 */
	public SubjectDTO getSubject() {
		return subject;
	}

	/**
	 * @return the noCriteria
	 */
	public boolean isNoCriteria() {
		return noCriteria;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isUriResolvable(){
		
		return uri==null ? false : URLUtil.isURL(uri);
	}
}
