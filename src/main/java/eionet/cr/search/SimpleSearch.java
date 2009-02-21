package eionet.cr.search;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.util.Hashes;
import eionet.cr.util.pagination.Pagination;
import eionet.cr.util.sql.ConnectionUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SimpleSearch extends AbstractSubjectSearch{

	/** */
	private SearchExpression searchExpression;
	
	/**
	 * 
	 * @param searchExpression
	 */
	public SimpleSearch(SearchExpression searchExpression){
		this.searchExpression = searchExpression;
	}

	/**
	 * 
	 * @param str
	 */
	public SimpleSearch(String searchExpression){
		this(new SearchExpression(searchExpression));
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#getSQL(java.util.List)
	 */
	protected String getSubjectSelectSQL(List inParameters){
		
		if (searchExpression==null || searchExpression.isEmpty())
			return null;
		
		StringBuffer sqlBuf = new StringBuffer("select sql_calc_found_rows distinct SPO.SUBJECT from SPO ");
		if (sortPredicate!=null){
			sqlBuf.append("left join SPO as ORDERING on (SPO.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=?) ");
			inParameters.add(Long.valueOf(Hashes.spoHash(sortPredicate)));
		}
		
		if (searchExpression.isExactPhrase())
			sqlBuf.append(" where SPO.ANON_SUBJ='N' and SPO.OBJECT like ?");
		else
			sqlBuf.append(" where SPO.ANON_SUBJ='N' and match(SPO.OBJECT) against (?)");
		inParameters.add(searchExpression.toString());
		
		if (sortPredicate!=null)
			sqlBuf.append(" order by ORDERING.OBJECT ").append(sortOrder==null ? sortOrder.ASCENDING.toSQL() : sortOrder.toSQL());

		int pageLength = Pagination.pageLength();
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
	 * @param searchExpression the searchExpression to set
	 */
	public void setSearchExpression(SearchExpression searchExpression) {
		this.searchExpression = searchExpression;
	}

	/**
	 * 
	 * @param string
	 */
	public void setSearchExpression(String string) {
		this.searchExpression = new SearchExpression(string);
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		
		ConnectionUtil.setReturnSimpleConnection(true);
		SimpleSearch simpleSearch = new SimpleSearch("soil");
		try{
			simpleSearch.execute();
			Collection<SubjectDTO> coll = simpleSearch.getResultList();
			
			if (coll!=null){
				System.out.println("coll.size() = " + coll.size());
				
				for (Iterator<SubjectDTO> i=coll.iterator(); i.hasNext(); ){
					SubjectDTO subject = i.next();
					System.out.println(subject);
				}
			}
			else
				System.out.println("coll is null");
		}
		catch (Exception e){
			e.printStackTrace();
		}		
	}
}
