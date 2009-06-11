package eionet.cr.search;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.util.Hashes;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SQLValue;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SpatialSourcesSearch {

	/** */
	private static final String sql =
		new StringBuffer("select distinct URI from SPO,RESOURCE where PREDICATE=").
		append(Hashes.spoHash(Predicates.RDF_TYPE)).append(" and OBJECT_HASH=").append(Hashes.spoHash(Subjects.WGS_SPATIAL_THING)).
		append(" and SOURCE=RESOURCE.URI_HASH").toString();
	
	/**
	 * 
	 * @return
	 * @throws SearchException 
	 */
	public static List<String> execute() throws SearchException{
		
		List<String> result = new ArrayList<String>();
		
		Connection conn = null;
		try{
			conn = ConnectionUtil.getConnection();
			List<Map<String,SQLValue>> resultList = SQLUtil.executeQuery(sql, conn);
			if (resultList!=null && !resultList.isEmpty()){
				for (int i=0; i<resultList.size(); i++){
					SQLValue sqlValue = resultList.get(i).get("URI");
					if (sqlValue!=null){
						result.add(sqlValue.getString());
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
		
		return result;
	}
}
