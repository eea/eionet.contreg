package eionet.cr.search;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eionet.cr.util.Hashes;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SQLValue;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PicklistSearch {

	/** */
	private Collection<String> resultCollection = new ArrayList<String>();
	
	/** */
	private String predicateUri;
	
	/**
	 * 
	 * @param predicateUri
	 */
	public PicklistSearch(String predicateUri){
		this.predicateUri = predicateUri;
	}
	
	/** */
	private static final String sqlQuery = "select distinct OBJECT from SPO where PREDICATE=? and LIT_OBJ='Y' and ANON_OBJ='N' order by OBJECT asc";
	/**
	 * @throws SearchException 
	 * 
	 */
	public void execute() throws SearchException{
		
		if (StringUtils.isBlank(predicateUri))
			return;

		Connection conn = null;
		try{
			conn = ConnectionUtil.getConnection();
			List<Map<String,SQLValue>> resultList = SQLUtil.executeQuery(sqlQuery, Collections.singletonList(Hashes.spoHash(predicateUri)), conn);
			if (resultList!=null && !resultList.isEmpty()){
				for (int i=0; i<resultList.size(); i++){
					SQLValue sqlValue = resultList.get(i).get("OBJECT");
					if (sqlValue!=null){
						resultCollection.add(sqlValue.getString());
					}
				}
			}
		}
		catch (SQLException e){
			throw new SearchException(e.toString(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
	}

	/**
	 * @return the resultCollection
	 */
	public Collection<String> getResultCollection() {
		return resultCollection;
	} 
}
