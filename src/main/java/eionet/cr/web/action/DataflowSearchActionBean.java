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

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.RodInstrumentDTO;
import eionet.cr.dto.RodObligationDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.CustomSearch;
import eionet.cr.search.PicklistSearch;
import eionet.cr.search.SearchException;
import eionet.cr.web.util.search.SearchResultColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/dataflowSearch.action")
public class DataflowSearchActionBean extends AbstractSearchActionBean{
	
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
		
		CustomSearch customSearch = new CustomSearch(CustomSearch.singletonCriteria(Predicates.RDF_TYPE, Subjects.ROD_OBLIGATION_CLASS));
		customSearch.setNoLimit(true);
		customSearch.execute();
		
		session.setAttribute(DATAFLOWS_SESSION_ATTR_NAME, createDataflowsGroupByInstruments(customSearch.getResultList()));
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
	 * @param dataflowSubjects
	 * @return
	 */
	private List<RodInstrumentDTO> createDataflowsGroupByInstruments(Collection<SubjectDTO> dataflowSubjects) {
		
		List<RodInstrumentDTO> result = new ArrayList<RodInstrumentDTO>();
		if (dataflowSubjects==null || dataflowSubjects.isEmpty())
			return result;
		
		Map<String,RodInstrumentDTO> instrumentsMap = new HashMap<String,RodInstrumentDTO>();
		for (Iterator<SubjectDTO> iter=dataflowSubjects.iterator(); iter.hasNext();){
			
			SubjectDTO subjectDTO = iter.next();
			String instrumentLabel = subjectDTO.getObjectValue(Predicates.ROD_INSTRUMENT_PROPERTY, ObjectDTO.Type.LITERAL);
			if (!StringUtils.isBlank(instrumentLabel)){
				
				String obligationLabel = subjectDTO.getObjectValue(Predicates.RDFS_LABEL, ObjectDTO.Type.LITERAL);
				if (StringUtils.isBlank(obligationLabel))
					obligationLabel = subjectDTO.getObjectValue(Predicates.DC_TITLE, ObjectDTO.Type.LITERAL);
				
				if (!StringUtils.isBlank(obligationLabel) && !StringUtils.isBlank(subjectDTO.getUri())){
					
					RodInstrumentDTO instrumentDTO = instrumentsMap.get(instrumentLabel);
					if (instrumentDTO==null){
						instrumentDTO = new RodInstrumentDTO("", instrumentLabel);
						instrumentsMap.put(instrumentLabel, instrumentDTO);
					}
					instrumentDTO.addObligation(new RodObligationDTO(subjectDTO.getUri(), obligationLabel));
				}
			}
		}
		
		if (!instrumentsMap.isEmpty()){
			result.addAll(instrumentsMap.values());
			Collections.sort(result);
		}
		
		return result;
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
	public List<RodInstrumentDTO> getInstrumentsObligations() {
		return (List<RodInstrumentDTO>) getContext().getRequest().getSession().getAttribute(DATAFLOWS_SESSION_ATTR_NAME);
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
		
		SearchResultColumn col = new SearchResultColumn();
		col.setPredicateUri(Predicates.RDFS_LABEL);
		col.setTitle("Title");
		col.setSortable(true);
		list.add(col);
		
		col = new SearchResultColumn();
		col.setPredicateUri(Predicates.ROD_OBLIGATION_PROPERTY);
		col.setTitle("Dataflow");
		col.setSortable(true);
		list.add(col);

		col = new SearchResultColumn();
		col.setPredicateUri(Predicates.ROD_LOCALITY_PROPERTY);
		col.setTitle("Locality");
		col.setSortable(true);
		list.add(col);

		col = new SearchResultColumn();
		col.setPredicateUri(Predicates.DC_DATE);
		col.setTitle("Date");
		col.setSortable(true);
		list.add(col);

		return list;
	}
}
