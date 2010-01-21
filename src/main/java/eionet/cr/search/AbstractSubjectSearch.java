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
package eionet.cr.search;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.util.SortOrder;
import eionet.cr.search.util.SubjectDataReader;
import eionet.cr.search.util.SubjectHashesReader;
import eionet.cr.search.util.SubjectSelectMode;
import eionet.cr.util.Util;
import eionet.cr.util.pagination.Pagination;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.SQLUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class AbstractSubjectSearch {
	
	/** */
	private static Log logger = LogFactory.getLog(AbstractSubjectSearch.class);
	
	/** */
	protected int pageNumber = 0;
	protected int pageLength = Pagination.pageLength();
	
	/** */
	protected SortOrder sortOrder = SortOrder.ASCENDING;
	protected String sortPredicate = null;	
	
	/** */
	protected Collection<SubjectDTO> resultList = new ArrayList<SubjectDTO>();
	protected int totalMatchCount = 0;
	
	/**
	 * @throws SQLException 
	 * 
	 */
	public void execute() throws SearchException{
		
		List<Object> inParameters = new ArrayList<Object>();
		
		/* create the SQL query that selects the hashes of matching subjects */
		
		String subjectSelectQuery = getSubjectSelectSQL(inParameters);
		if (subjectSelectQuery!=null && subjectSelectQuery.length()>0){
			
			Connection conn = null;
			try{
				/* create connection and SubjectHashesReader */
				
				conn = getConnection();
				SubjectHashesReader subjectHashesReader = createSubjectHashesReader();
				
				/* execute the above-created query */
				
				logger.debug("Executing subject select query: " + subjectSelectQuery);
				long time = System.currentTimeMillis();
				SQLUtil.executeQuery(subjectSelectQuery, inParameters, subjectHashesReader, conn);
				logger.debug("subject select query took " + (System.currentTimeMillis()-time) + " ms");
				
				/* get the result-map */
				
				LinkedHashMap<Long,SubjectDTO> subjectsMap =
//					!getSubjectSelectMode().equals(SubjectSelectMode.SERVER) ? subjectHashesReader.getResultMap() :
							subjectHashesReader.getResultMap(pageNumber, pageLength);

				// if any matching subjects found, proceed to getting their metadata */
				if (subjectsMap.size()>0){

					/* set the count of total matches, depending on the mode */
					
					totalMatchCount = subjectHashesReader.getResultSetSize();
					
					/* execute the SQL query that gets the metadata of the found subjects, collect the query results */

					SubjectDataReader subjectDataReader = createSubjectDataReader(subjectsMap);
					logger.debug("Executing subject data select query");
					time = System.currentTimeMillis();
					SQLUtil.executeQuery(createSubjectMetadataSelectQuery(Util.toCSV(subjectsMap.keySet())), subjectDataReader, conn);
					logger.debug("subject data select query took " + (System.currentTimeMillis()-time) + " ms");
					
					/* collect labels and sub-properties */
					
					collectPredicateLabels(conn, subjectDataReader);
					collectSubProperties(conn, subjectDataReader);
					
					/* assign result list */
					
					resultList = subjectsMap.values();
				}
			}
			catch (SQLException e){
				throw new SearchException(e.toString(), e);
			}
			finally{
				SQLUtil.close(conn);
			}
		}
	}
	
	/**
	 * 
	 * @param conn
	 * @param subjectDataReader
	 * @throws SQLException 
	 */
	protected void collectPredicateLabels(Connection conn, SubjectDataReader subjectDataReader) throws SQLException {
	}

	/**
	 * 
	 * @param conn
	 * @param subjectDataReader
	 * @throws SQLException
	 */
	protected void collectSubProperties(Connection conn, SubjectDataReader subjectDataReader) throws SQLException {
	}

	/**
	 * 
	 * @param inParameters
	 * @return
	 */
	protected abstract String getSubjectSelectSQL(List inParameters);
	
	/**
	 * 
	 * @param sortP
	 * @param sortO
	 */
	public void setSorting(String sortPredicate, String sortOrder) {
		
		setSorting(sortPredicate, SortOrder.parse(sortOrder));
	}

	/**
	 * 
	 * @param sortPredicate
	 * @param sortOrder
	 */
	public void setSorting(String sortPredicate, SortOrder sortOrder) {
		
		this.sortPredicate= sortPredicate;
		this.sortOrder = sortOrder;
	}

	/**
	 * 
	 * @param pageN
	 */
	public void setPageNumber(int pageNumber) {
		
		this.pageNumber = pageNumber;
	}

	/**
	 * @return the resultList
	 */
	public Collection<SubjectDTO> getResultList() {
		return resultList;
	}

	/**
	 * @return the matchCount
	 */
	public int getTotalMatchCount() {
		return totalMatchCount;
	}
	
	/**
	 * 
	 * @return
	 * @throws SQLException 
	 */
	protected Connection getConnection() throws SQLException{
		return ConnectionUtil.getConnection();
	}
	
	/**
	 * 
	 * @return
	 */
	protected String createSubjectMetadataSelectQuery(String subjectHashes){
		
		StringBuffer buf = new StringBuffer().
		append("select distinct ").
			append("SUBJECT as SUBJECT_HASH, SUBJ_RESOURCE.URI as SUBJECT_URI, SUBJ_RESOURCE.LASTMODIFIED_TIME as SUBJECT_MODIFIED, ").
			append("PREDICATE as PREDICATE_HASH, PRED_RESOURCE.URI as PREDICATE_URI, ").
			append("OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, OBJ_SOURCE_OBJECT, OBJ_DERIV_SOURCE, SOURCE, ").
			append("SRC_RESOURCE.URI as SOURCE_URI, DSRC_RESOURCE.URI as DERIV_SOURCE_URI ").
		append("from SPO ").
			append("left join RESOURCE as SUBJ_RESOURCE on (SUBJECT=SUBJ_RESOURCE.URI_HASH) ").
			append("left join RESOURCE as PRED_RESOURCE on (PREDICATE=PRED_RESOURCE.URI_HASH) ").
			append("left join RESOURCE as SRC_RESOURCE on (SOURCE=SRC_RESOURCE.URI_HASH) ").
			append("left join RESOURCE as DSRC_RESOURCE on (OBJ_DERIV_SOURCE=DSRC_RESOURCE.URI_HASH) ").
		append("where ").
			append("SUBJECT in (").append(subjectHashes).append(") ").  
		append("order by ").
			append("SUBJECT, PREDICATE, OBJECT");
		
		return buf.toString();
	}
	
	/**
	 * 
	 * @return
	 */
	public int resultSize(){
		return resultList==null ? 0 : resultList.size();
	}

	/**
	 * @param noLimit the noLimit to set
	 */
	public final void setNoLimit() {
		pageLength = 0;
	}
	
	/**
	 * 
	 * @return
	 */
	protected SubjectHashesReader createSubjectHashesReader(){
		return new SubjectHashesReader();
	}

	/**
	 * 
	 * @param subjectsMap
	 * @return
	 */
	protected SubjectDataReader createSubjectDataReader(Map<Long,SubjectDTO> subjectsMap){
		return new SubjectDataReader(subjectsMap);
	}

	/**
	 * @return the pageLength
	 */
	public int getPageLength() {
		return pageLength;
	}

	/**
	 * @param pageLength the pageLength to set
	 */
	public void setPageLength(int pageLength) {
		this.pageLength = pageLength;
	}

	/**
	 * 
	 * @return
	 */
	protected static SubjectSelectMode getSubjectSelectMode(){
		
		return SubjectSelectMode.valueOf(
				GeneralConfig.getProperty(GeneralConfig.SUBJECT_SELECT_MODE, SubjectSelectMode.DB_1STEP.toString()).toUpperCase());
	}

	/**
	 * 
	 * @return
	 */
	protected String orderingJoinTable(){
		return "SPO";
	}
}
