/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.search.RecentUploadsSearch;
import eionet.cr.search.SearchException;
import eionet.cr.util.Util;
import eionet.cr.web.util.UploadDateFormatter;
import eionet.cr.web.util.search.PredicateBasedColumn;
import eionet.cr.web.util.search.PropertyBasedColumn;
import eionet.cr.web.util.search.SearchResultColumn;

/**
 * 
 * @author altnyris
 *
 */
@UrlBinding("/recentUploads.action")
public class RecentUploadsActionBean extends AbstractSearchActionBean {
	
	/** */
	public static final int MAX_RESULTS = 20;
	
	/** */
	private static Log logger = LogFactory.getLog(RecentUploadsActionBean.class);

	/** */
	public static List<Map<String,String>> types;
	private static Map<String,List<SearchResultColumn>> typesColumns;

	/** */
	private String type;
	
	private static PropertyBasedColumn uploadDateColumn = createUploadDateColumn();
	
	/**
	 * 
	 */
	public RecentUploadsActionBean(){
		this.type = Subjects.ROD_DELIVERY_CLASS; // default type
	}
	
	/**
	 * 
	 * @return
	 * @throws SearchException 
	 */
	@DefaultHandler
	public Resolution search() throws SearchException{
		
		if (!StringUtils.isBlank(type)){
			//String decodedType = Util.urlDecode(type);
			RecentUploadsSearch recentUploadsSearch = new RecentUploadsSearch(type);
			recentUploadsSearch.setPageLength(MAX_RESULTS);
			recentUploadsSearch.execute();
			this.resultList = recentUploadsSearch.getResultList();
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
			
//			Map<String,String> typeMap = new HashMap<String,String>();
//			typeMap.put("title", "Deliveries");
//			typeMap.put("uri", Util.urlEncode(Subjects.ROD_DELIVERY_CLASS));
//			RecentUploadsActionBean.types.add(typeMap);
//			
//			typeMap = new HashMap<String,String>();
//			typeMap.put("title", "Obligations");
//			typeMap.put("uri", Util.urlEncode(Subjects.ROD_OBLIGATION_CLASS));
//			RecentUploadsActionBean.types.add(typeMap);
//			
//			typeMap = new HashMap<String,String>();
//			typeMap.put("title", "Full reports");
//			typeMap.put("uri", Util.urlEncode(Subjects.FULL_REPORT_CLASS));
//			RecentUploadsActionBean.types.add(typeMap);
//
//			typeMap = new HashMap<String,String>();
//			typeMap.put("title", "News releases");
//			typeMap.put("uri", Util.urlEncode(Subjects.RSS_ITEM_CLASS.toLowerCase()));
//				// we do toLowerCase(), because http://reports.eea.europa.eu/whatsnew.rdf wrongfully uses
//				// http://purl.org/rss/1.0/item, instead of http://purl.org/rss/1.0/Item
//			RecentUploadsActionBean.types.add(typeMap);

			Map<String,String> typeMap = new HashMap<String,String>();
			typeMap.put("title", "Deliveries");
			typeMap.put("uri", Subjects.ROD_DELIVERY_CLASS);
			RecentUploadsActionBean.types.add(typeMap);
			
			typeMap = new HashMap<String,String>();
			typeMap.put("title", "Obligations");
			typeMap.put("uri", Subjects.ROD_OBLIGATION_CLASS);
			RecentUploadsActionBean.types.add(typeMap);
			
			typeMap = new HashMap<String,String>();
			typeMap.put("title", "Full reports");
			typeMap.put("uri", Subjects.FULL_REPORT_CLASS);
			RecentUploadsActionBean.types.add(typeMap);

			typeMap = new HashMap<String,String>();
			typeMap.put("title", "News releases");
			typeMap.put("uri", Subjects.RSS_ITEM_CLASS.toLowerCase());
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
			
			/* columns for deliveries */
			
			List<SearchResultColumn> list = new ArrayList<SearchResultColumn>();
			PredicateBasedColumn col = new PredicateBasedColumn();
			col.setPredicateUri(Predicates.RDFS_LABEL);
			col.setTitle("Title");
			col.setSortable(false);
			list.add(col);

			col = new PredicateBasedColumn();
			col.setPredicateUri(Predicates.ROD_OBLIGATION_PROPERTY);
			col.setTitle("Obligation");
			col.setSortable(false);
			list.add(col);

			col = new PredicateBasedColumn();
			col.setPredicateUri(Predicates.ROD_LOCALITY_PROPERTY);
			col.setTitle("Locality");
			col.setSortable(false);
			list.add(col);
			
			addColumnsForType(Subjects.ROD_DELIVERY_CLASS, list);

			/* columns for obligations */
			
			list = new ArrayList<SearchResultColumn>();
			col = new PredicateBasedColumn();
			col.setPredicateUri(Predicates.RDFS_LABEL);
			col.setTitle("Title");
			col.setSortable(false);
			list.add(col);

			col = new PredicateBasedColumn();
			col.setPredicateUri(Predicates.ROD_ISSUE_PROPERTY);
			col.setTitle("Issue");
			col.setSortable(false);
			list.add(col);

			col = new PredicateBasedColumn();
			col.setPredicateUri(Predicates.ROD_INSTRUMENT_PROPERTY);
			col.setTitle("Instrument");
			col.setSortable(false);
			list.add(col);
			
			addColumnsForType(Subjects.ROD_OBLIGATION_CLASS, list);

			/* columns for full reports */
			
			list = new ArrayList<SearchResultColumn>();
			col = new PredicateBasedColumn();
			col.setPredicateUri(Predicates.RDFS_LABEL);
			col.setTitle("Title");
			col.setSortable(false);
			list.add(col);

			col = new PredicateBasedColumn();
			col.setPredicateUri(Predicates.DC_SUBJECT);
			col.setTitle("SubjectDTOTemp");
			col.setSortable(false);
			list.add(col);

			col = new PredicateBasedColumn();
			col.setPredicateUri(Predicates.DC_COVERAGE);
			col.setTitle("Coverage");
			col.setSortable(false);
			list.add(col);
			
			addColumnsForType(Subjects.FULL_REPORT_CLASS, list);

			/* columns for news releases */
			
			list = new ArrayList<SearchResultColumn>();
			col = new PredicateBasedColumn();
			col.setPredicateUri(Predicates.RDFS_LABEL);
			col.setTitle("Title");
			col.setSortable(false);
			list.add(col);

			col = new PredicateBasedColumn();
			col.setPredicateUri(Predicates.DC_SUBJECT);
			col.setTitle("SubjectDTOTemp");
			col.setSortable(false);
			list.add(col);

			col = new PredicateBasedColumn();
			col.setPredicateUri(Predicates.DC_COVERAGE);
			col.setTitle("Coverage");
			col.setSortable(false);
			list.add(col);

			// we do toLowerCase(), because http://reports.eea.europa.eu/whatsnew.rdf wrongfully uses
			// http://purl.org/rss/1.0/item, instead of http://purl.org/rss/1.0/Item
			addColumnsForType(Subjects.RSS_ITEM_CLASS.toLowerCase(), list);
		}
		
		return RecentUploadsActionBean.typesColumns.get(type);
	}
	
	/**
	 * 
	 * @param type
	 * @param columns
	 */
	private static void addColumnsForType(String type, List<SearchResultColumn> columns){
		columns.add(uploadDateColumn);
		RecentUploadsActionBean.typesColumns.put(type, columns);
	}

	/**
	 * 
	 * @return
	 */
	private static PropertyBasedColumn createUploadDateColumn(){
		
		PropertyBasedColumn result = new PropertyBasedColumn();
		result.setProperty("firstSeenTime");
		result.setTitle("Uploaded");
		result.setSortable(false);
		result.setFormatter(new UploadDateFormatter());
		
		return result;
	}
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
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
