package eionet.cr.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import eionet.cr.dto.RDFSubject;
import eionet.cr.search.util.SortOrder;
import eionet.cr.search.util.SubjectHashesReader;
import eionet.cr.search.util.SubjectsDataReader;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.MySQLUtil;
import eionet.cr.util.sql.SQLUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class RDFSubjectSearch {
	
	/** */
	protected int pageNumber = 0;
	protected SortOrder sortOrder = SortOrder.ASCENDING;
	protected String sortPredicate = null;	
	
	/** */
	protected Collection<RDFSubject> resultList = new ArrayList<RDFSubject>();
	protected int totalRowCount = 0;
	
	/**
	 * @throws SQLException 
	 * 
	 */
	public void execute() throws SQLException{
		
		List<Object> inParameters = new ArrayList<Object>();
		
		String subjectSelectSQL = getSubjectSelectSQL(inParameters);
		if (subjectSelectSQL!=null && subjectSelectSQL.length()>0){
			
			Connection conn = null;
			try{
				conn = getConnection();
				
				SubjectHashesReader subjectHashesReader = new SubjectHashesReader();
				SQLUtil.executeQuery(subjectSelectSQL, inParameters, subjectHashesReader, conn);
				
				if (subjectHashesReader.getResultCount()>0){
					
					Integer totalRowCount = MySQLUtil.getTotalRowCount(conn); // TODO - maybe do it without directly pointing to MySQL
					LinkedHashMap<String, RDFSubject> subjectsMap = subjectHashesReader.getResultMap();
					
					SubjectsDataReader subjectsDataReader = new SubjectsDataReader(subjectsMap);
					SQLUtil.executeQuery(
							getSubjectDataSelectSQL(subjectHashesReader.getSubjectHashesCommaSeparated()), subjectsDataReader, conn);
					
					this.resultList = subjectsMap.values();
				}
			}
			finally{
				SQLUtil.close(conn);
			}
		}
	}

	/**
	 * 
	 * @param inParameters
	 * @return
	 */
	protected abstract String getSubjectSelectSQL(List<Object> inParameters);
	
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
	 * @param pageNumber
	 */
	public void setPageNumber(int pageNumber) {
		
		this.pageNumber = pageNumber;
	}

	/**
	 * @return the resultList
	 */
	public Collection<RDFSubject> getResultList() {
		return resultList;
	}

	/**
	 * @return the totalRowCount
	 */
	public int getTotalRowCount() {
		return totalRowCount;
	}
	
	/**
	 * 
	 * @return
	 */
	protected int getPageLength(){
		return 20; // FIXME - should not be hard-coded
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
	protected String getSubjectDataSelectSQL(String subjectHashes){
		
		StringBuffer buf = new StringBuffer().
		append("select SUBJECT, SUBJ_RESOURCE.URI as SUBJECT_URI, PRED_RESOURCE.URI as PREDICATE_URI, ").
		append("OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_DERIV_SOURCE, OBJ_LANG ").
		append("from SPO, RESOURCE as SUBJ_RESOURCE, RESOURCE as PRED_RESOURCE ").
		append("where SUBJECT in (").append(subjectHashes).append(") and ").
		append("SUBJECT=SUBJ_RESOURCE.URI_HASH and PREDICATE=PRED_RESOURCE.URI_HASH order by SUBJECT, PREDICATE, OBJECT");
		
		return buf.toString();
	}
}
