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
import java.util.List;

import net.sourceforge.stripes.action.Resolution;
import eionet.cr.common.Predicates;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.SearchException;
import eionet.cr.search.util.SortOrder;
import eionet.cr.util.QueryString;
import eionet.cr.util.pagination.Pagination;
import eionet.cr.web.util.columns.SearchResultColumn;
import eionet.cr.web.util.columns.SubjectPredicateColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class AbstractSearchActionBean extends AbstractActionBean{

	/** */
	protected Collection<SubjectDTO> resultList;
	
	/** */
	protected int pageN = 1;
	protected String sortO = SortOrder.ASCENDING.toString();
	protected String sortP = null;	
	protected int matchCount = 0;
	
	/** */
	private Pagination pagination;
	
	/**
	 * 
	 * @return
	 * @throws SearchException
	 */
	public abstract Resolution search() throws SearchException;

	/**
	 * @return the columns
	 * @throws SearchException 
	 */
	public abstract List<SearchResultColumn> getColumns() throws SearchException;

	/**
	 * 
	 * @return
	 */
	public Collection<SubjectDTO> getResultList() {
		return resultList;
	}

	/**
	 * @param resultList the resultList to set
	 */
	public void setResultList(List<SubjectDTO> resultList) {
		this.resultList = resultList;
	}

	/**
	 * 
	 * @return
	 */
	public int getMaxResultSetSize(){
		return Pagination.pageLength();
	}

	/**
	 * 
	 * @return
	 */
	protected List<SearchResultColumn> getDefaultColumns(){
		
		ArrayList<SearchResultColumn> list = new ArrayList<SearchResultColumn>();
		
		SubjectPredicateColumn col = new SubjectPredicateColumn();
		col.setPredicateUri(Predicates.RDF_TYPE);
		col.setTitle("Type");
		col.setSortable(true);
		list.add(col);
		
		col = new SubjectPredicateColumn();
		col.setPredicateUri(Predicates.RDFS_LABEL);
		col.setTitle("Title");
		col.setSortable(true);
		list.add(col);

		col = new SubjectPredicateColumn();
		col.setPredicateUri(Predicates.DC_DATE);
		col.setTitle("Date");
		col.setSortable(true);
		list.add(col);
		
		return list;
	}

	/**
	 * @return the pageN
	 */
	public int getPageN() {
		return pageN;
	}

	/**
	 * @param pageN the pageN to set
	 */
	public void setPageN(int pageNumber) {
		this.pageN = pageNumber<1 ? 1 : pageNumber;
	}

	/**
	 * @return the sortO
	 */
	public String getSortO() {
		return sortO;
	}

	/**
	 * @param sortO the sortO to set
	 */
	public void setSortO(String sortOrder) {
		this.sortO = sortOrder;
	}

	/**
	 * @return the sortP
	 */
	public String getSortP() {
		return sortP;
	}

	/**
	 * @param sortP the sortP to set
	 */
	public void setSortP(String sortPredicate) {
		this.sortP = sortPredicate;
	}

	/**
	 * @return the matchCount
	 */
	public int getMatchCount() {
		return matchCount;
	}
	
	/**
	 * 
	 * @return
	 */
	public Pagination getPagination(){
		
		if (pagination==null){
			String urlBinding = getUrlBinding();
			if (urlBinding.startsWith("/")){
				urlBinding = urlBinding.substring(1);
			}
			pagination = Pagination.getPagination(getMatchCount(), getPageN(), urlBinding, QueryString.createQueryString(getContext().getRequest()));
		}
		
		return pagination;
	}
}
