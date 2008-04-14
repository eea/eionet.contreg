package eionet.cr.web.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;

import eionet.cr.search.SearchException;
import eionet.cr.search.Searcher;
import eionet.cr.search.util.RodInstrumentDTO;
import eionet.cr.search.util.SearchUtil;
import eionet.cr.util.CRRuntimeException;
import eionet.cr.util.Identifiers;
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
public class DataflowSearchActionBean extends AbstractSearchActionBean{
	
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
		
		instrumentsObligations = Searcher.getDataflowsGroupedByInstruments();
		localities = Searcher.getLiteralFieldValues(Identifiers.ROD_LOCALITY_PROPERTY);
		return new ForwardResolution("/pages/dataflowSearch.jsp");
	}
	
	/**
	 * 
	 * @return
	 * @throws SearchException 
	 */
	public Resolution search() throws SearchException{
		
		resultList = SearchUtil.listForDisplay(Searcher.dataflowSearch(dataflow, locality, year));
		return new ForwardResolution("/pages/dataflowSearchResults.jsp");
	}

	/**
	 * @return the instrumentsObligations
	 */
	public List<RodInstrumentDTO> getInstrumentsObligations() {
		return instrumentsObligations;
	}

	/**
	 * @return the countries
	 */
	public Set<String> getLocalities() {
		return localities;
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
}
