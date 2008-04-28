package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.Resolution;

import eionet.cr.common.Identifiers;
import eionet.cr.common.ResourceDTO;
import eionet.cr.search.SearchException;
import eionet.cr.search.util.HitsCollector;
import eionet.cr.util.Util;
import eionet.cr.web.util.search.SearchResultColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class SearchResourcesActionBean extends AbstractCRActionBean{

	/** */
	protected List<ResourceDTO> resultList;
	
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
	 * @return the resultList
	 */
	public List<ResourceDTO> getResultList() {
		return resultList;
	}

	/**
	 * @param resultList the resultList to set
	 */
	public void setResultList(List<ResourceDTO> resultList) {
		this.resultList = resultList;
	}

	/**
	 * 
	 * @return
	 */
	public int getMaxResultSetSize(){
		return HitsCollector.MAX_HITS;
	}

	/**
	 * 
	 * @return
	 */
	protected List<SearchResultColumn> getDefaultColumns(){
		
		ArrayList<SearchResultColumn> list = new ArrayList<SearchResultColumn>();
		
		SearchResultColumn col = new SearchResultColumn();
		col.setProperty(Identifiers.RDF_TYPE);
		col.setTitle("Type");
		col.setSortable(true);
		list.add(col);
		
		col = new SearchResultColumn();
		col.setProperty(ResourceDTO.SpecialKeys.RESOURCE_LABEL);
		col.setTitle("Label");
		col.setSortable(true);
		list.add(col);

		col = new SearchResultColumn();
		col.setProperty(Identifiers.DC_DATE);
		col.setTitle("Date");
		col.setSortable(true);
		list.add(col);
		
		return list;
	}
}
