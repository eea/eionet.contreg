package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.CustomSearch;
import eionet.cr.search.DataflowPicklistSearch;
import eionet.cr.search.PicklistSearch;
import eionet.cr.search.SearchException;
import eionet.cr.search.util.UriLabelPair;
import eionet.cr.web.util.search.PredicateBasedColumn;
import eionet.cr.web.util.search.SearchResultColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/dataflowSearch.action")
public class DataflowSearchActionBean extends AbstractSearchActionBean{
	
	/** */
	private static Log logger = LogFactory.getLog(DataflowSearchActionBean.class);
	
	/** */
	private static final String DATAFLOWS_SESSION_ATTR_NAME = DataflowSearchActionBean.class + ".dataflows";
	private static final String LOCALITIES_SESSION_ATTR_NAME = DataflowSearchActionBean.class + ".localities";

	/** */
	private List<String> years;
	
	/** */
	private String dataflow;
	private String locality;
	private String year;
	
	/**
	 * 
	 * @return
	 * @throws SearchException 
	 * @throws SearchException
	 */
	@DefaultHandler
	public Resolution init() throws SearchException{
		loadOptions();
		return new ForwardResolution("/pages/dataflowSearch.jsp");
	}

	/**
	 * @throws SearchException 
	 * 
	 */
	private void loadOptions() throws SearchException{
		
		HttpSession session = getContext().getRequest().getSession();
		loadDataflows(session);
		loadLocalities(session);
	}
	
	/**
	 * 
	 * @throws SearchException
	 */
	private void loadDataflows(HttpSession session) throws SearchException{
		
		if (session.getAttribute(DATAFLOWS_SESSION_ATTR_NAME)!=null)
			return;

		long start = System.currentTimeMillis();
		
		DataflowPicklistSearch search = new DataflowPicklistSearch();
		search.execute();
		
		logger.debug("Dataflow search executed with " + (System.currentTimeMillis()-start) + " ms");
		start = System.currentTimeMillis();
		
		session.setAttribute(DATAFLOWS_SESSION_ATTR_NAME, search.getResultMap());
		
		logger.debug("Dataflow search results sorted with " + (System.currentTimeMillis() - start) + " ms");
	}

	/**
	 * 
	 * @param session
	 * @throws SearchException
	 */
	private void loadLocalities(HttpSession session) throws SearchException{
		
		if (session.getAttribute(LOCALITIES_SESSION_ATTR_NAME)!=null)
			return;
			
		PicklistSearch picklistSearch = new PicklistSearch(Predicates.ROD_LOCALITY_PROPERTY);
		picklistSearch.execute();
		
		session.setAttribute(LOCALITIES_SESSION_ATTR_NAME, picklistSearch.getResultCollection());
	}

	/**
	 * 
	 * @return
	 * @throws SearchException 
	 */
	public Resolution search() throws SearchException{
		
		loadOptions();
		
		CustomSearch customSearch = new CustomSearch(buildSearchCriteria());
		customSearch.setPageNumber(getPageN());
		customSearch.setSorting(getSortP(), getSortO());
		customSearch.execute();
		
		resultList = customSearch.getResultList();
		matchCount = customSearch.getTotalMatchCount();
		
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
	public HashMap<String,ArrayList<UriLabelPair>> getInstrumentsObligations() {
		return (HashMap<String,ArrayList<UriLabelPair>>)getContext().getRequest().getSession().getAttribute(DATAFLOWS_SESSION_ATTR_NAME);
	}

	/**
	 * @return the countries
	 */
	public Collection<String> getLocalities() {
		return (Collection<String>) getContext().getRequest().getSession().getAttribute(LOCALITIES_SESSION_ATTR_NAME);
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
		
		PredicateBasedColumn col = new PredicateBasedColumn();
		col.setPredicateUri(Predicates.RDFS_LABEL);
		col.setTitle("Title");
		col.setSortable(true);
		list.add(col);
		
		col = new PredicateBasedColumn();
		col.setPredicateUri(Predicates.ROD_OBLIGATION_PROPERTY);
		col.setTitle("Dataflow");
		col.setSortable(true);
		list.add(col);

		col = new PredicateBasedColumn();
		col.setPredicateUri(Predicates.ROD_LOCALITY_PROPERTY);
		col.setTitle("Locality");
		col.setSortable(true);
		list.add(col);

		col = new PredicateBasedColumn();
		col.setPredicateUri(Predicates.DC_DATE);
		col.setTitle("Date");
		col.setSortable(true);
		list.add(col);

		return list;
	}
}
