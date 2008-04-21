package eionet.cr.web.action;

import java.util.List;
import java.util.Map;

import eionet.cr.web.util.DefaultSearchResultColumnList;
import eionet.cr.web.util.SearchResultColumn;
import eionet.cr.web.util.SearchResultRowDisplayMap;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class AbstractSearchActionBean extends AbstractCRActionBean{

	/** */
	protected List<SearchResultColumn> columns = null;
	
	/** */
	protected List<SearchResultRowDisplayMap> resultList;

	/**
	 * @return the columns
	 */
	public List<SearchResultColumn> getColumns() {
		if (columns==null)
			columns = getDefaultColumns();
		return columns;
	}
	
	/**
	 * 
	 */
	protected List<SearchResultColumn> getDefaultColumns(){
		return new DefaultSearchResultColumnList();
	}

	/**
	 * @return the resultList
	 */
	public List<SearchResultRowDisplayMap> getResultList() {
		return resultList;
	}
}
