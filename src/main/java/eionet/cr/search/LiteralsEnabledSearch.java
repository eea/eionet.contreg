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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

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
public class LiteralsEnabledSearch {

	/** */
	private String predicateUri;
	
	/** */
	private boolean literalsEnabled = false;
	
	/**
	 * 
	 * @param predicateUri
	 */
	private LiteralsEnabledSearch(String predicateUri){
		this.predicateUri = predicateUri;
	}
	
	/** */
	private static final String allowLiteralSearchQuery = "select distinct OBJECT from SPO where SUBJECT=? and PREDICATE=? and LIT_OBJ='N' and ANON_OBJ='N'";
	
	/**
	 * 
	 * @param predicateUri
	 * @return
	 * @throws SearchException 
	 */
	public static boolean search(String predicateUri) throws SearchException{
		LiteralsEnabledSearch search = new LiteralsEnabledSearch(predicateUri);
		search.execute();
		return search.isLiteralsEnabled();
	}
	
	/**
	 * @throws SearchException 
	 * 
	 */
	private void execute() throws SearchException{
		
		if (StringUtils.isBlank(predicateUri))
			return;
		
		Long predicateHash = Long.valueOf(Hashes.spoHash(predicateUri));
		
		Connection conn = null;
		try{
			conn = ConnectionUtil.getConnection();
			
			ArrayList values = new ArrayList();
			values.add(Long.valueOf(Hashes.spoHash(predicateUri)));
			values.add(Long.valueOf((Hashes.spoHash(Predicates.RDFS_RANGE))));
			
			List<Map<String,SQLValue>> resultList = SQLUtil.executeQuery(allowLiteralSearchQuery, values, conn);
			if (resultList!=null && !resultList.isEmpty()){
				for (int i=0; i<resultList.size(); i++){
					SQLValue sqlValue = resultList.get(i).get("OBJECT");
					if (sqlValue!=null){
						String strValue = sqlValue.getString();
						if (strValue!=null && strValue.equals(Subjects.RDFS_LITERAL)){
							this.literalsEnabled = true; // rdfs:Literal is present in the specified rdfs:domain
						}
					}
				}
			}
			else
				this.literalsEnabled = true; // if not rdfs:domain specified at all, then lets allow literal search
		}
		catch (SQLException e){
			throw new SearchException(e.toString(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
	}

	/**
	 * @return the literalsEnabled
	 */
	private boolean isLiteralsEnabled() {
		return literalsEnabled;
	} 
}
