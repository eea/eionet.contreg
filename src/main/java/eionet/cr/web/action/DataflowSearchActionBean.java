package eionet.cr.web.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.common.Predicates;
import eionet.cr.common.ResourceDTO;
import eionet.cr.search.SearchException;
import eionet.cr.search.Searcher;
import eionet.cr.search.util.dataflow.RodInstrumentDTO;
import eionet.cr.util.Util;
import eionet.cr.web.util.search.SearchResultColumn;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/dataflowSearch.action")
public class DataflowSearchActionBean extends SearchResourcesActionBean{
	
	private static final String DATAFLOWS_SESSION_ATTR_NAME = DataflowSearchActionBean.class + ".dataflows";
	private static final String LOCALITIES_SESSION_ATTR_NAME = DataflowSearchActionBean.class + ".localities";
	
	/** */
	private List<RodInstrumentDTO> instrumentsObligations;
	private Set<String> localities;
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
		
		HttpSession session = this.getContext().getRequest().getSession();
		if (session.getAttribute(DATAFLOWS_SESSION_ATTR_NAME)==null)
			session.setAttribute(DATAFLOWS_SESSION_ATTR_NAME, Searcher.getDataflowsGroupedByInstruments());
		if (session.getAttribute(LOCALITIES_SESSION_ATTR_NAME)==null)
			session.setAttribute(LOCALITIES_SESSION_ATTR_NAME, Searcher.getLiteralFieldValues(Predicates.ROD_LOCALITY_PROPERTY));
	}
	
	/**
	 * 
	 * @return
	 * @throws SearchException 
	 */
	public Resolution search() throws SearchException{
		loadOptions();
		resultList = Searcher.dataflowSearch(dataflow, locality, year);
		return new ForwardResolution("/pages/dataflowSearch.jsp");
	}

	/**
	 * @return the instrumentsObligations
	 */
	public List<RodInstrumentDTO> getInstrumentsObligations() {
		return (List<RodInstrumentDTO>) getContext().getRequest().getSession().getAttribute(DATAFLOWS_SESSION_ATTR_NAME);
	}

	/**
	 * @return the countries
	 */
	public Set<String> getLocalities() {
		return (Set<String>) getContext().getRequest().getSession().getAttribute(LOCALITIES_SESSION_ATTR_NAME);
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
	 * @see eionet.cr.web.action.SearchResourcesActionBean#getColumns()
	 */
	public List<SearchResultColumn> getColumns(){
		
		ArrayList<SearchResultColumn> list = new ArrayList<SearchResultColumn>();
		
		SearchResultColumn col = new SearchResultColumn();
		col.setProperty(ResourceDTO.SpecialKeys.RESOURCE_TITLE);
		col.setTitle("Title");
		col.setSortable(true);
		list.add(col);
		
		col = new SearchResultColumn();
		col.setProperty(Predicates.ROD_OBLIGATION_PROPERTY);
		col.setTitle("Dataflow");
		col.setSortable(true);
		list.add(col);

		col = new SearchResultColumn();
		col.setProperty(Predicates.ROD_LOCALITY_PROPERTY);
		col.setTitle("Locality");
		col.setSortable(true);
		list.add(col);

		col = new SearchResultColumn();
		col.setProperty(Predicates.DC_DATE);
		col.setTitle("Date");
		col.setSortable(true);
		list.add(col);

		return list;
	}
}
