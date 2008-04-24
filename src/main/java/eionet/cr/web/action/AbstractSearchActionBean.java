package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eionet.cr.common.Identifiers;
import eionet.cr.util.Util;
import eionet.cr.web.util.search.SearchResultColumn;
import eionet.cr.web.util.search.SearchResultRow;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class AbstractSearchActionBean extends AbstractCRActionBean{

	/** */
	protected List<SearchResultColumn> columns = null;
	
	/** */
	protected List<SearchResultRow> resultList;

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
		
		ArrayList<SearchResultColumn> list = new ArrayList<SearchResultColumn>();
		
		SearchResultColumn col = new SearchResultColumn();
		col.setPropertyUri(Identifiers.RDF_TYPE);
		col.setPropertyKey(Util.md5digest(Identifiers.RDF_TYPE));
		col.setTitle("Type");
		col.setSortable(true);
		list.add(col);
		
		col = new SearchResultColumn();
		col.setPropertyUri(SearchResultRow.FALLBACKED_LABEL);
		col.setPropertyKey(SearchResultRow.FALLBACKED_LABEL);
		col.setTitle("Label");
		col.setSortable(true);
		list.add(col);

		col = new SearchResultColumn();
		col.setPropertyUri(Identifiers.DC_DATE);
		col.setPropertyKey(Util.md5digest(Identifiers.DC_DATE));
		col.setTitle("Date");
		col.setSortable(true);
		list.add(col);
		
		return list;
	}

	/**
	 * @return the resultList
	 */
	public List<SearchResultRow> getResultList() {
		return resultList;
	}

	/**
	 * @param resultList the resultList to set
	 */
	public void setResultList(List<SearchResultRow> resultList) {
		this.resultList = resultList;
	}
}
