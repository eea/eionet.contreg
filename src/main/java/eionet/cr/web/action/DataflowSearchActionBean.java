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
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dao.mysql.MySQLDAOFactory;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.util.SortOrder;
import eionet.cr.search.util.UriLabelPair;
import eionet.cr.util.PagingRequest;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.web.util.ApplicationCache;
import eionet.cr.web.util.columns.SearchResultColumn;
import eionet.cr.web.util.columns.SubjectPredicateColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/dataflowSearch.action")
public class DataflowSearchActionBean extends AbstractSearchActionBean<SubjectDTO>{
	
	/** */
	private List<String> years;
	
	/** */
	private String dataflow;
	private String locality;
	private String year;

	/**
	 * 
	 * @return
	 */
	@DefaultHandler
	public Resolution init(){
		return new ForwardResolution("/pages/dataflowSearch.jsp");
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 */
	public Resolution search() throws DAOException{
		Pair<Integer, List<SubjectDTO>> customSearch = MySQLDAOFactory
					.get()
					.getDao(SearchDAO.class)
					.searchByFilters(
							buildSearchCriteria(),
							null,
							new PagingRequest(getPageN()),
							new SortingRequest(getSortP(), SortOrder.parse(getSortO())));
		resultList = customSearch.getRight();
		matchCount = customSearch.getLeft();
		
		return new ForwardResolution("/pages/dataflowSearch.jsp");
	}

	/**
	 * 
	 * @return
	 */
	private Map<String, String> buildSearchCriteria(){
		
		Map<String, String> result = new HashMap<String, String>();
		
		result.put(Predicates.RDF_TYPE, Subjects.ROD_DELIVERY_CLASS);
		if (!StringUtils.isBlank(dataflow))
			result.put(Predicates.ROD_OBLIGATION_PROPERTY, dataflow);
		
		if (!StringUtils.isBlank(locality))
			result.put(Predicates.ROD_LOCALITY_PROPERTY, locality);
		
		if (!StringUtils.isBlank(year))
			result.put(Predicates.DC_COVERAGE, year);
		
		return result;
	}

	/**
	 * @return the instrumentsObligations
	 */
	public Map<String,List<UriLabelPair>> getInstrumentsObligations() {
		return ApplicationCache.getDataflowPicklist();
	}

	/**
	 * @return the countries
	 */
	public Collection<String> getLocalities() {
		return ApplicationCache.getLocalities();
	}

	/**
	 * @return the years
	 */
	public List<String> getYears() {
		
		if (years==null){
			years = new ArrayList<String>();
			int curYear = Calendar.getInstance().get(Calendar.YEAR);
			int earliestYear = 1990;
			for (int i=curYear; i>=earliestYear; i--)
				years.add(String.valueOf(i));
		}
		
		return years;
	}

	/**
	 * @return the dataflow
	 */
	public String getDataflow() {
		return dataflow;
	}

	/**
	 * @return the locality
	 */
	public String getLocality() {
		return locality;
	}

	/**
	 * @return the year
	 */
	public String getYear() {
		return year;
	}

	/**
	 * @param dataflow the dataflow to set
	 */
	public void setDataflow(String dataflow) {
		this.dataflow = dataflow;
	}

	/**
	 * @param locality the locality to set
	 */
	public void setLocality(String locality) {
		this.locality = locality;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(String year) {
		this.year = year;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
	 */
	public List<SearchResultColumn> getColumns(){
		
		ArrayList<SearchResultColumn> list = new ArrayList<SearchResultColumn>();
		
		SubjectPredicateColumn col = new SubjectPredicateColumn();
		col.setPredicateUri(Predicates.RDFS_LABEL);
		col.setTitle("Title");
		col.setSortable(true);
		list.add(col);
		
//		col = new SubjectPredicateColumn();
//		col.setPredicateUri(Predicates.ROD_OBLIGATION_PROPERTY);
//		col.setTitle("Dataflow");
//		col.setSortable(true);
//		list.add(col);

		col = new SubjectPredicateColumn();
		col.setPredicateUri(Predicates.ROD_HAS_FILE);
		col.setTitle("File");
		col.setSortable(true);
		list.add(col);

		col = new SubjectPredicateColumn();
		col.setPredicateUri(Predicates.CR_HAS_FEEDBACK);
		col.setTitle("Feedback");
		col.setSortable(true);
		list.add(col);

		col = new SubjectPredicateColumn();
		col.setPredicateUri(Predicates.ROD_LOCALITY_PROPERTY);
		col.setTitle("Locality");
		col.setSortable(true);
		list.add(col);

		col = new SubjectPredicateColumn();
		col.setPredicateUri(Predicates.DC_DATE);
		col.setTitle("Date");
		col.setSortable(true);
		list.add(col);

		return list;
	}
}
