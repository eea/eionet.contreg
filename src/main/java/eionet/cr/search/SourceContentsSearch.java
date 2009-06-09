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

import java.util.List;

import eionet.cr.util.Hashes;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SourceContentsSearch extends AbstractSubjectSearch {
	
	/** */
	private String sourceUrl;
	private long sourceUrlHash;

	/**
	 * 
	 * @param sourceUrl
	 */
	public SourceContentsSearch(String sourceUrl){
		
		this.sourceUrl = sourceUrl;
		if (this.sourceUrl!=null)
			this.sourceUrlHash = Hashes.spoHash(this.sourceUrl);
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#getSubjectSelectSQL(java.util.List)
	 */
	protected String getSubjectSelectSQL(List inParameters){
		
		if (sourceUrl==null)
			return null;
		
		StringBuffer sqlBuf = new StringBuffer("select SPO.SUBJECT as SUBJECT_HASH from SPO ");
		if (sortPredicate!=null){
			sqlBuf.append("left join SPO as ORDERING on (SPO.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=?) ");
			inParameters.add(Long.valueOf(Hashes.spoHash(sortPredicate)));
		}
		sqlBuf.append(" where SPO.SOURCE=?");
		inParameters.add(Long.valueOf(sourceUrlHash));
		
		if (sortPredicate!=null)
			sqlBuf.append(" order by ORDERING.OBJECT ").append(sortOrder==null ? sortOrder.ASCENDING.toSQL() : sortOrder.toSQL());

		return sqlBuf.toString();
	}
}
