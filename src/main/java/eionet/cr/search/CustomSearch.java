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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.SearchDAO;
import eionet.cr.util.Hashes;
import eionet.cr.util.URIUtil;
import eionet.cr.web.util.columns.SubjectLastModifiedColumn;

/**
 * Use {@link SearchDAO} performCustomSearch  method instead.
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@Deprecated
public class CustomSearch extends AbstractSubjectSearch{
	
	/** */
	private Map<String,String> criteria;
	
	/** */
	private HashSet<String> literalsEnabledPredicates;
	
	/** */
	private StringBuffer fromStatement = new StringBuffer();
	private StringBuffer whereStatement = new StringBuffer();
	
	/**
	 * 
	 */
	public CustomSearch(Map<String,String> criteria){
		
		this.criteria = criteria;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#getSubjectSelectSQL(java.util.List)
	 */
	protected String getSubjectSelectSQL(List inParameters) {
		
		if (criteria==null || criteria.isEmpty())
			return null;
		
		createFromAndWhereStatements(inParameters);
		
		StringBuffer sqlBuf = new StringBuffer("select SPO1.SUBJECT as SUBJECT_HASH from ").
		append(fromStatement).append(" where ").append(whereStatement);
		
		if (sortPredicate!=null){
			if (sortPredicate.equals(SubjectLastModifiedColumn.class.getSimpleName())){
				sqlBuf.append(" order by RESOURCE.LASTMODIFIED_TIME ").append(sortOrder==null ? sortOrder.ASCENDING.toSQL() : sortOrder.toSQL());
			}
			else{
				sqlBuf.append(" order by ORDERING.OBJECT ").append(sortOrder==null ? sortOrder.ASCENDING.toSQL() : sortOrder.toSQL());
			}
		}

		return sqlBuf.toString();
	}
	
	/**
	 * 
	 * @return
	 */
	private void createFromAndWhereStatements(List inParameters){
		
		int i=1;
		for (Iterator<String> predicates=criteria.keySet().iterator(); predicates.hasNext(); i++){
			
			String spoCurr = "SPO" + String.valueOf(i);
			fromStatement.append(fromStatement.length()==0 ? "" : ", ").append("SPO as ").append(spoCurr);
			
			if (i==1 && sortPredicate!=null){
				
				if (sortPredicate.equals(SubjectLastModifiedColumn.class.getSimpleName())){
					fromStatement.append(" left join RESOURCE on (SPO1.SUBJECT=RESOURCE.URI_HASH) ");
				}
				else{
					fromStatement.append(" left join SPO as ORDERING on (SPO1.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=?)");
					inParameters.add(Long.valueOf(Hashes.spoHash(sortPredicate)));
				}
			}
			
			if (whereStatement.length()>0)
				whereStatement.append(" and ");

			String predicateUri = predicates.next();
			String predicateValue = criteria.get(predicateUri);

			whereStatement.append(spoCurr).append(".PREDICATE=? and ");
			inParameters.add(Long.valueOf(Hashes.spoHash(predicateUri)));
			
			if (requiresExactMatch(predicateUri, predicateValue)){
				whereStatement.append(spoCurr).append(".OBJECT_HASH=?");
				inParameters.add(Long.valueOf(Hashes.spoHash(normalize(predicateValue))));
			}
			else{
				whereStatement.append("match(").append(spoCurr).append(".OBJECT) against (?)");
				inParameters.add(predicateValue);
			}

			if (i<criteria.size()){
				String spoNext = "SPO" + String.valueOf(i+1);
				whereStatement.append(" and ").append(spoCurr).append(".SUBJECT=").append(spoNext).append(".SUBJECT");
			}
		}
	}

	/**
	 * 
	 * @param predicateValue
	 * @return
	 */
	private boolean requiresExactMatch(String predicateUri, String predicateValue){
		return (predicateValue.startsWith("\"") && predicateValue.endsWith("\"")) || URIUtil.isSchemedURI(predicateValue) || !isLiteralsEnabled(predicateUri);
	}

	/**
	 * 
	 * @param predicateValue
	 * @return
	 */
	private String normalize(String predicateValue){
		if  (predicateValue.startsWith("\"") && predicateValue.endsWith("\""))
			return predicateValue.substring(1, predicateValue.length()-1).trim();
		else
			return predicateValue;
	}
	
	/**
	 * 
	 * @param predicate
	 * @param value
	 * @return
	 */
	public static Map<String,String> singletonCriteria(String predicate, String value){
		
		Map<String,String> result = new HashMap<String,String>();
		if (!StringUtils.isBlank(predicate) && !StringUtils.isBlank(value)){
			result.put(predicate, value);
		}
		return result;
	}

	/**
	 * @param literalsEnabledPredicates the literalsEnabledPredicates to set
	 */
	public void setLiteralsEnabledPredicates(
			HashSet<String> literalsEnabledPredicates) {
		this.literalsEnabledPredicates = literalsEnabledPredicates;
	}
	
	/**
	 * 
	 * @param predicateUri
	 * @return
	 */
	private boolean isLiteralsEnabled(String predicateUri){
		return literalsEnabledPredicates!=null && literalsEnabledPredicates.contains(predicateUri);
	}

	protected String orderingJoinTable(){
		return "SPO1";
	}
}
