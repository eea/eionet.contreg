package eionet.cr.dao.mysql;

import java.util.ArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.SearchHelperDao;
import eionet.cr.search.SearchException;
import eionet.cr.util.Hashes;
import eionet.cr.util.sql.SQLValue;
import eionet.cr.util.sql.SQLValueReader;


/**
 *	Mysql implementation of {@link MySQLSearchHelperDao}.
 * 
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class MySQLSearchHelperDao extends MySQLBaseDAO implements SearchHelperDao {
	

	private static final String sqlQuery = "select distinct OBJECT from SPO where PREDICATE=? and LIT_OBJ='Y' and ANON_OBJ='N' order by OBJECT asc";
	
	MySQLSearchHelperDao() {
		//reducing visibility
	}

	/** 
	 * @see eionet.cr.dao.SearchHelperDao#getPicklistForPredicate(java.lang.String)
	 * {@inheritDoc}
	 */
	public Collection<String> getPicklistForPredicate(String predicateUri) throws SearchException {
		if (StringUtils.isBlank(predicateUri)) {
			return Collections.emptyList();
		}
		try{
			Collection<String> result = new LinkedList<String>();
			List<Map<String,SQLValue>> resultList = executeQuery(
					sqlQuery,
					Collections.singletonList((Object)Hashes.spoHash(predicateUri)),
					new SQLValueReader()); 
			if (resultList!=null && !resultList.isEmpty()){
				for (int i=0; i<resultList.size(); i++){
					SQLValue sqlValue = resultList.get(i).get("OBJECT");
					if (sqlValue!=null){
						result.add(sqlValue.getString());
					}
				}
			}
			return result;
		}
		catch (DAOException e){
			throw new SearchException(e.toString(), e);
		}
	}

	/** */
	private static final String allowLiteralSearchQuery = "select distinct OBJECT from SPO where SUBJECT=? and PREDICATE=? and LIT_OBJ='N' and ANON_OBJ='N'";
	
	/** 
	 * @see eionet.cr.dao.SearchHelperDao#isAllowLiteralSearch(java.lang.String)
	 * {@inheritDoc}
	 */
	public boolean isAllowLiteralSearch(String predicateUri) throws SearchException{
		//sanity checks
		if (StringUtils.isBlank(predicateUri)) {
			return false;
		}
		
		try {
			ArrayList<Object> values = new ArrayList<Object>();
			values.add(Long.valueOf(Hashes.spoHash(predicateUri)));
			values.add(Long.valueOf((Hashes.spoHash(Predicates.RDFS_RANGE))));
			List<Map<String,SQLValue>> resultList = executeQuery(allowLiteralSearchQuery, values, new SQLValueReader());
			if (resultList == null || resultList.isEmpty()) {
				return true; // if not rdfs:domain specified at all, then lets allow literal search
			}
			for (Map<String, SQLValue> result : resultList){
				SQLValue sqlValue = result.get("OBJECT");
				if (sqlValue!=null){
					String strValue = sqlValue.getString();
					if (strValue!=null && strValue.equals(Subjects.RDFS_LITERAL)){
						return true; // rdfs:Literal is present in the specified rdfs:domain
					}
				}
			}
			
			return false;
		} catch(DAOException exception) {
			throw new SearchException();
		}
	}
	
}
