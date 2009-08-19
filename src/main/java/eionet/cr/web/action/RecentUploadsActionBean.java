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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
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
import eionet.cr.web.util.columns.SearchResultColumn;
import eionet.cr.web.util.columns.SubjectPredicateColumn;
import eionet.cr.web.util.columns.SubjectUploadedColumn;

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
	private String type;

	/** */
	private static final List<Map<String,String>> types;
	private static final Map<String,List<SearchResultColumn>> typesColumns;
	

	static {
		types = new LinkedList<Map<String,String>>();
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

		
		typesColumns = new HashMap<String,List<SearchResultColumn>>();
		SubjectUploadedColumn uploaded = new SubjectUploadedColumn("Uploaded", false);
		
		/* columns for deliveries */
		List<SearchResultColumn> list = new ArrayList<SearchResultColumn>();
		list.add(new SubjectPredicateColumn("Title", false, Predicates.RDFS_LABEL));
		list.add(new SubjectPredicateColumn("Obligation", false, Predicates.ROD_OBLIGATION_PROPERTY));
		list.add(new SubjectPredicateColumn("Locality", false, Predicates.ROD_LOCALITY_PROPERTY));
		list.add(uploaded);
		typesColumns.put(Subjects.ROD_DELIVERY_CLASS, list);

		/* columns for obligations */
		list = new ArrayList<SearchResultColumn>();
		list.add(new SubjectPredicateColumn("Title", false, Predicates.RDFS_LABEL));
		list.add(new SubjectPredicateColumn("Issue", false, Predicates.ROD_ISSUE_PROPERTY));
		list.add(new SubjectPredicateColumn("Instrument", false, Predicates.ROD_INSTRUMENT_PROPERTY));
		list.add(uploaded);
		typesColumns.put(Subjects.ROD_OBLIGATION_CLASS, list);

		/* columns for full reports */
		list = new ArrayList<SearchResultColumn>();
		list.add(new SubjectPredicateColumn("Title", false, Predicates.RDFS_LABEL));
		list.add(new SubjectPredicateColumn("SubjectDTOTemp", false, Predicates.DC_SUBJECT));
		list.add(new SubjectPredicateColumn("Coverage", false, Predicates.DC_COVERAGE));
		list.add(uploaded);
		typesColumns.put(Subjects.FULL_REPORT_CLASS, list);

		/* columns for news releases  are the same as for full reports */
		// we do toLowerCase(), because http://reports.eea.europa.eu/whatsnew.rdf wrongfully uses
		// http://purl.org/rss/1.0/item, instead of http://purl.org/rss/1.0/Item
		typesColumns.put(Subjects.RSS_ITEM_CLASS.toLowerCase(), list);
	}
	
	
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
	public Resolution search() throws SearchException {
		
		if (!StringUtils.isBlank(type)){
			//String decodedType = Util.urlDecode(type);
			RecentUploadsSearch recentUploadsSearch = new RecentUploadsSearch(type);
			recentUploadsSearch.setPageLength(MAX_RESULTS);
			recentUploadsSearch.execute();
			this.resultList = recentUploadsSearch.getResultList();
		}
			
		return new ForwardResolution("/pages/recent.jsp");
	}
	
	@HandlesEvent("recentFiles")
	public Resolution recentFiles() throws SearchException {
		RecentUploadsSearch search = new RecentUploadsSearch("file");
		search.setPageLength(MAX_RESULTS);
		search.execute();
		this.resultList = search.getResultList();
		return new ForwardResolution("/pages/recentFiles.jsp");
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
		return RecentUploadsActionBean.types;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getMaxResults(){
		return RecentUploadsActionBean.MAX_RESULTS;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
	 */
	public List<SearchResultColumn> getColumns(){
		if (StringUtils.isEmpty(type)){
			return getDefaultColumns();
		}
		String decodedType = Util.urlDecode(type);
		return StringUtils.isEmpty(decodedType) 
				? null 
				: typesColumns.get(decodedType);
	}
}
