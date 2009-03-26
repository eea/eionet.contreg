package eionet.cr.search;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eionet.cr.util.Hashes;
import eionet.cr.util.URIUtil;
import eionet.cr.util.pagination.Pagination;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class CustomSearch extends AbstractSubjectSearch{
	
	/** */
	private Map<String,String> criteria;
	
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
		
		StringBuffer sqlBuf = new StringBuffer("select sql_calc_found_rows distinct SPO1.SUBJECT as SUBJECT_HASH from ").
		append(fromStatement).append(" where ").append(whereStatement);
		
		if (sortPredicate!=null)
			sqlBuf.append(" order by ORDERING.OBJECT ").append(sortOrder==null ? sortOrder.ASCENDING.toSQL() : sortOrder.toSQL());

		if (pageLength>0){
			sqlBuf.append(" limit ");
			if (pageNumber>0){
				sqlBuf.append("?,");
				inParameters.add(new Integer((pageNumber-1)*pageLength));
			}
			sqlBuf.append(pageLength);
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
				fromStatement.append(" left join SPO as ORDERING on (SPO1.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=?)");
				inParameters.add(Long.valueOf(Hashes.spoHash(sortPredicate)));
			}
			
			if (whereStatement.length()>0)
				whereStatement.append(" and ");

			String predicateUri = predicates.next();
			String predicateValue = criteria.get(predicateUri);

			whereStatement.append(spoCurr).append(".PREDICATE=? and ");
			inParameters.add(Long.valueOf(Hashes.spoHash(predicateUri)));
			
			if (requiresExactMatch(predicateValue)){
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
	private boolean requiresExactMatch(String predicateValue){
		return (predicateValue.startsWith("\"") && predicateValue.endsWith("\"")) || URIUtil.isSchemedURI(predicateValue);
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
}
