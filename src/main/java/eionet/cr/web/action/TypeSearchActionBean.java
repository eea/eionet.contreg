package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.CustomSearch;
import eionet.cr.search.PicklistSearch;
import eionet.cr.search.SearchException;
import eionet.cr.search.util.SortOrder;
import eionet.cr.search.util.UriLabelPair;
import eionet.cr.web.util.search.PredicateBasedColumn;
import eionet.cr.web.util.search.SearchResultColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/typeSearch.action")
public class TypeSearchActionBean extends AbstractSearchActionBean{
	
	/** */
	private static final String FORM_PAGE = "/pages/typeSearch.jsp";
	
	/** */
	private static final String TYPE_SEARCH_PICKLIST = TypeSearchActionBean.class.getName() + ".picklist";
	
	/** */
	private String type;

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 */
	public Resolution search() throws SearchException {
		
		if (!StringUtils.isBlank(type)){
			
			Map<String,String> criteria = new HashMap<String,String>();
			criteria.put(Predicates.RDF_TYPE, type);
			
			CustomSearch customSearch = new CustomSearch(criteria);
			customSearch.execute();
			
			resultList = customSearch.getResultList();
    		matchCount = customSearch.getTotalMatchCount();
		}
		
		return new ForwardResolution(FORM_PAGE);
	}
	
	/**
	 * 
	 * @return
	 */
	@DefaultHandler
	public Resolution unspecified(){
		
		clearSession();
		return new ForwardResolution(FORM_PAGE);
	}
	
	/**
	 * 
	 */
	private void clearSession(){
		
		getContext().getRequest().getSession().removeAttribute(TYPE_SEARCH_PICKLIST);
	}
	
	/**
	 * @throws SearchException 
	 * 
	 */
	public List<UriLabelPair> getPicklist() throws SearchException{
		
		List<UriLabelPair> picklist = (List<UriLabelPair>)getContext().getRequest().getSession().getAttribute(TYPE_SEARCH_PICKLIST);
		if (picklist==null || picklist.isEmpty()){
			
			Map<String,String> criteria = new HashMap<String,String>();
			criteria.put(Predicates.RDF_TYPE, Subjects.RDFS_CLASS);
			
			CustomSearch customSearch = new CustomSearch(criteria);
			customSearch.setSorting(Predicates.RDFS_LABEL, SortOrder.ASCENDING);
			customSearch.setPageLength(0); // we want no limits on result set size
			customSearch.execute();
			Collection<SubjectDTO> subjects = customSearch.getResultList();
			if (subjects!=null){
				
				picklist = new ArrayList<UriLabelPair>();
				for (Iterator<SubjectDTO> it=subjects.iterator(); it.hasNext();){
					SubjectDTO subject = it.next();
					if (!subject.isAnonymous()){
						String label = subject.getObjectValue(Predicates.RDFS_LABEL);
						if (!StringUtils.isBlank(label)){
							picklist.add(UriLabelPair.create(subject.getUri(), label));
						}
					}
				}
			}
			
			if (picklist!=null && !picklist.isEmpty()){
				getContext().getRequest().getSession().setAttribute(TYPE_SEARCH_PICKLIST, picklist);
			}
		}
		
		return picklist;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
	 */
	public List<SearchResultColumn> getColumns() {
		
		ArrayList<SearchResultColumn> list = new ArrayList<SearchResultColumn>();
		
		PredicateBasedColumn col = new PredicateBasedColumn();
		col.setPredicateUri(Predicates.RDFS_LABEL);
		col.setTitle("Title");
		col.setSortable(true);
		list.add(col);

		return list;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
}
