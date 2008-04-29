package eionet.cr.web.action;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.common.Identifiers;
import eionet.cr.common.ResourceDTO;
import eionet.cr.search.SearchException;
import eionet.cr.search.Searcher;
import eionet.cr.util.Util;
import eionet.cr.web.util.search.SearchResultColumn;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * 
 * @author altnyris
 *
 */
@UrlBinding("/recentUploads.action")
public class RecentUploadsActionBean extends SearchResourcesActionBean {
	
	/** */
	public static final int MAX_RESULTS = 20;
	
	/** */
	private static Log logger = LogFactory.getLog(RecentUploadsActionBean.class);

	/** */
	public static List<Map<String,String>> types;
	private static Map<String,List<SearchResultColumn>> typesColumns;

	/** */
	private String type;
	
	/**
	 * 
	 */
	public RecentUploadsActionBean(){
		this.type = Util.urlEncode(Identifiers.ROD_DELIVERY_CLASS); // default type
	}
	
	/**
	 * 
	 * @return
	 * @throws SearchException 
	 */
	@DefaultHandler
	public Resolution search() throws SearchException{
		
		if (type!=null && type.length()>0){
			String decodedType = Util.urlDecode(type);
			this.resultList = Searcher.getRecentByRdfType(decodedType, MAX_RESULTS);
		}
			
		return new ForwardResolution("/pages/recent.jsp");
	}

	/**
	 * 
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * 
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * 
	 * @return
	 */
	public List<Map<String, String>> getTypes(){
		
		if (RecentUploadsActionBean.types==null){
			
			RecentUploadsActionBean.types = new ArrayList<Map<String,String>>();
			
			Map<String,String> typeMap = new HashMap<String,String>();
			typeMap.put("title", "Deliveries");
			typeMap.put("uri", Util.urlEncode(Identifiers.ROD_DELIVERY_CLASS));
			RecentUploadsActionBean.types.add(typeMap);
			
			typeMap = new HashMap<String,String>();
			typeMap.put("title", "Obligations");
			typeMap.put("uri", Util.urlEncode(Identifiers.ROD_OBLIGATION_CLASS));
			RecentUploadsActionBean.types.add(typeMap);
			
			typeMap = new HashMap<String,String>();
			typeMap.put("title", "Full reports");
			typeMap.put("uri", Util.urlEncode(Identifiers.FULL_REPORT_CLASS));
			RecentUploadsActionBean.types.add(typeMap);

			typeMap = new HashMap<String,String>();
			typeMap.put("title", "News releases");
			typeMap.put("uri", Util.urlEncode(Identifiers.RSS_ITEM_CLASS.toLowerCase()));
				// we do toLowerCase(), because http://reports.eea.europa.eu/whatsnew.rdf wrongfully uses
				// http://purl.org/rss/1.0/item, instead of http://purl.org/rss/1.0/Item
			RecentUploadsActionBean.types.add(typeMap);
		}
		
		return RecentUploadsActionBean.types;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getMaxResults(){
		return RecentUploadsActionBean.MAX_RESULTS;
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	private List<SearchResultColumn> getColumnsForType(String type){
		
		if (type==null || type.length()==0)
			return null;
		
		if (RecentUploadsActionBean.typesColumns==null){
			
			RecentUploadsActionBean.typesColumns = new HashMap<String,List<SearchResultColumn>>();
			
			// columns for deliveries
			List<SearchResultColumn> list = new ArrayList<SearchResultColumn>();
			SearchResultColumn col = new SearchResultColumn();
			col.setProperty(ResourceDTO.SpecialKeys.RESOURCE_TITLE);
			col.setTitle("Title");
			col.setSortable(false);
			list.add(col);

			col = new SearchResultColumn();
			col.setProperty(Identifiers.ROD_OBLIGATION_PROPERTY);
			col.setTitle("Obligation");
			col.setSortable(false);
			list.add(col);

			col = new SearchResultColumn();
			col.setProperty(Identifiers.ROD_LOCALITY_PROPERTY);
			col.setTitle("Locality");
			col.setSortable(false);
			list.add(col);

			col = new SearchResultColumn();
			col.setProperty(Identifiers.DC_DATE);
			col.setTitle("Date");
			col.setSortable(false);
			list.add(col);
			
			RecentUploadsActionBean.typesColumns.put(Identifiers.ROD_DELIVERY_CLASS, list);

			// columns for obligations
			list = new ArrayList<SearchResultColumn>();
			col = new SearchResultColumn();
			col.setProperty(ResourceDTO.SpecialKeys.RESOURCE_TITLE);
			col.setTitle("Title");
			col.setSortable(false);
			list.add(col);

			col = new SearchResultColumn();
			col.setProperty(Identifiers.ROD_ISSUE_PROPERTY);
			col.setTitle("Issue");
			col.setSortable(false);
			list.add(col);

			col = new SearchResultColumn();
			col.setProperty(Identifiers.ROD_INSTRUMENT_PROPERTY);
			col.setTitle("Instrument");
			col.setSortable(false);
			list.add(col);
			
			RecentUploadsActionBean.typesColumns.put(Identifiers.ROD_OBLIGATION_CLASS, list);

			// columns for full reports
			list = new ArrayList<SearchResultColumn>();
			col = new SearchResultColumn();
			col.setProperty(ResourceDTO.SpecialKeys.RESOURCE_TITLE);
			col.setTitle("Title");
			col.setSortable(false);
			list.add(col);

			col = new SearchResultColumn();
			col.setProperty(Identifiers.DC_SUBJECT);
			col.setTitle("Subject");
			col.setSortable(false);
			list.add(col);

			col = new SearchResultColumn();
			col.setProperty(Identifiers.DC_COVERAGE);
			col.setTitle("Coverage");
			col.setSortable(false);
			list.add(col);
			
			col = new SearchResultColumn();
			col.setProperty(Identifiers.DC_DATE);
			col.setTitle("Date");
			col.setSortable(false);
			list.add(col);

			RecentUploadsActionBean.typesColumns.put(Identifiers.FULL_REPORT_CLASS, list);

			// columns for news releases
			list = new ArrayList<SearchResultColumn>();
			col = new SearchResultColumn();
			col.setProperty(ResourceDTO.SpecialKeys.RESOURCE_TITLE);
			col.setTitle("Title");
			col.setSortable(false);
			list.add(col);

			col = new SearchResultColumn();
			col.setProperty(Identifiers.DC_SUBJECT);
			col.setTitle("Subject");
			col.setSortable(false);
			list.add(col);

			col = new SearchResultColumn();
			col.setProperty(Identifiers.DC_COVERAGE);
			col.setTitle("Coverage");
			col.setSortable(false);
			list.add(col);
			
			col = new SearchResultColumn();
			col.setProperty(Identifiers.DC_DATE);
			col.setTitle("Date");
			col.setSortable(false);
			list.add(col);

			RecentUploadsActionBean.typesColumns.put(Identifiers.RSS_ITEM_CLASS.toLowerCase(), list);
			// we do toLowerCase(), because http://reports.eea.europa.eu/whatsnew.rdf wrongfully uses
			// http://purl.org/rss/1.0/item, instead of http://purl.org/rss/1.0/Item
		}
		
		return RecentUploadsActionBean.typesColumns.get(type);
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.SearchResourcesActionBean#getColumns()
	 */
	public List<SearchResultColumn> getColumns(){
		
		if (type!=null && type.length()>0){
			String decodedType = Util.urlDecode(type);
			return getColumnsForType(decodedType);
		}
		else
			return getDefaultColumns();
	}
}
