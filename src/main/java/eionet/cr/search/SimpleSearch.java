package eionet.cr.search;

import java.util.List;

import eionet.cr.search.util.SearchString;
import eionet.cr.util.sql.SQLUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SimpleSearch extends RDFSubjectSearch{

	/** */
	private static final String SQL_WITH_LIKE =
		"select distinct SPO.SUBJECT from SPO "
		+ "left join SPO as ORDERING on (SPO.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=?) "
		+ "where SPO.OBJECT like ?";

	/** */
	private static final String SQL_WITH_MATCH =
		"select distinct SPO.SUBJECT from SPO "
		+ "left join SPO as ORDERING on (SPO.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=?) "
		+ "where match(SPO.OBJECT) against (?)";

	/** */
	private SearchString searchString;

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.RDFSubjectSearch#getSQL(java.util.List)
	 */
	protected String getSubjectSelectSQL(List<Object> paramValues){
		
		if (searchString==null || searchString.isEmpty())
			return null;
		
		StringBuffer sqlBuf = new StringBuffer("select sql_calc_found_rows distinct SPO.SUBJECT from SPO ");
		if (sortPredicate!=null){
			sqlBuf.append("left join SPO as ORDERING on (SPO.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=?) ");
			paramValues.add(Integer.valueOf(sortPredicate));
		}
		
		if (searchString.isExactPhrase())
			sqlBuf.append(" where SPO.OBJECT like ?");
		else
			sqlBuf.append(" where match(SPO.OBJECT) against (?)");
		paramValues.add(searchString.toString());
		
		if (sortPredicate!=null)
			sqlBuf.append(" order by ORDERING.OBJECT ").append(sortOrder==null ? "" : sortOrder.toSQL());
			
		if (getPageLength()>0){
			sqlBuf.append(" limit ");
			if (pageNumber>0){
				sqlBuf.append("?,");
				paramValues.add(new Integer((pageNumber-1)*getPageLength()));
			}
			sqlBuf.append(getPageLength());
		}
		
		return sqlBuf.toString();
	}

	/**
	 * @param searchString the searchString to set
	 */
	public void setSearchString(SearchString searchString) {
		this.searchString = searchString;
	}
}
