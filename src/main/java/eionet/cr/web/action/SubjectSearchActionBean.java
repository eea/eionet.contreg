package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import net.sourceforge.stripes.action.Resolution;
import eionet.cr.common.Predicates;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.SearchException;
import eionet.cr.search.util.HitsCollector;
import eionet.cr.search.util.SortOrder;
import eionet.cr.util.Util;
import eionet.cr.web.util.search.SearchResultColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class SubjectSearchActionBean extends AbstractCRActionBean{

	/** */
	protected Collection<SubjectDTO> resultList;
	
	/** */
	protected int pageN = 0;
	protected String sortO = SortOrder.ASCENDING.toString();
	protected String sortP = null;	
	protected int totalMatchCount = 0;
	
	/**
	 * 
	 * @return
	 * @throws SearchException
	 */
	public abstract Resolution search() throws SearchException;

	/**
	 * @return the columns
	 */
	public abstract List<SearchResultColumn> getColumns();

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
		return HitsCollector.DEFAULT_MAX_HITS;
	}

	/**
	 * 
	 * @return
	 */
	protected List<SearchResultColumn> getDefaultColumns(){
		
		ArrayList<SearchResultColumn> list = new ArrayList<SearchResultColumn>();
		
		SearchResultColumn col = new SearchResultColumn();
		col.setProperty(Predicates.RDF_TYPE);
		col.setTitle("Type");
		col.setSortable(true);
		list.add(col);
		
		col = new SearchResultColumn();
		col.setProperty(Predicates.RDFS_LABEL);
		col.setTitle("Title");
		col.setSortable(true);
		list.add(col);

		col = new SearchResultColumn();
		col.setProperty(Predicates.DC_DATE);
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
		this.pageN = pageNumber;
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
	 * @return the totalMatchCount
	 */
	public int getTotalMatchCount() {
		return totalMatchCount;
	}
}
