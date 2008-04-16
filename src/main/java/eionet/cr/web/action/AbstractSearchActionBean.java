package eionet.cr.web.action;

import java.util.List;
import java.util.Map;

import eionet.cr.web.util.display.DefaultColumnList;
import eionet.cr.web.util.display.SearchResultRowDisplayMap;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class AbstractSearchActionBean extends AbstractCRActionBean{

	/** */
	protected List columns = null;
	
	/** */
	protected List<SearchResultRowDisplayMap> resultList;

	/**
	 * @return the columns
	 */
	public List getColumns() {
		if (columns==null)
			columns = getDefaultColumns();
		return columns;
	}
	
	/**
	 * 
	 */
	protected List getDefaultColumns(){
		return new DefaultColumnList();
	}

	/**
	 * @return the resultList
	 */
	public List<SearchResultRowDisplayMap> getResultList() {
		return resultList;
	}
}
