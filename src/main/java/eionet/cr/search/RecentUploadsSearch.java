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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.util.RecentUploadsDataReader;
import eionet.cr.search.util.RecentUploadsHashesReader;
import eionet.cr.search.util.SubjectDataReader;
import eionet.cr.search.util.SubjectHashesReader;
import eionet.cr.util.Hashes;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class RecentUploadsSearch extends AbstractSubjectSearch {
	
	/** */
	private String subjectType;
	private Map<String,Date> firstSeenTimes = new HashMap<String,Date>();
	
	/**
	 * 
	 * @param subjectType
	 */
	public RecentUploadsSearch(String subjectType){
		this.subjectType = subjectType;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#getSubjectSelectSQL(java.util.List)
	 */
	protected String getSubjectSelectSQL(List inParameters) {
		
		if (StringUtils.isBlank(subjectType))
			return null;
		
		StringBuffer sqlBuf = new StringBuffer().
		append("select SPO.SUBJECT as SUBJECT_HASH, RESOURCE.FIRSTSEEN_TIME as FIRSTSEEN_TIME from SPO, RESOURCE").
		append(" where SPO.PREDICATE=").append(Hashes.spoHash(Predicates.RDF_TYPE)).
		append(" and SPO.OBJECT_HASH=?").
		append(" and SPO.SUBJECT=RESOURCE.URI_HASH").
		append(" order by RESOURCE.FIRSTSEEN_TIME desc");
		
		inParameters.add(Long.valueOf(Hashes.spoHash(subjectType)));
		
		return sqlBuf.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#createSubjectHashesReader()
	 */
	protected SubjectHashesReader createSubjectHashesReader(){
		return new RecentUploadsHashesReader(firstSeenTimes);
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#createSubjectDataReader(java.util.Map)
	 */
	protected SubjectDataReader createSubjectDataReader(Map<String, SubjectDTO> subjectsMap){
		return new RecentUploadsDataReader(subjectsMap, firstSeenTimes);
	}
}
