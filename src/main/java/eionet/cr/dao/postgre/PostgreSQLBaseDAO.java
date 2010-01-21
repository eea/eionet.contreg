/*
* The contents of this file are subject to the Mozilla Public
* 
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
* Agency. Portions created by Tieto Eesti are Copyright
* (C) European Environment Agency. All Rights Reserved.
* 
* Contributor(s):
* Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.dao.postgre;

import java.util.Collection;

import eionet.cr.util.Util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class PostgreSQLBaseDAO {

	/**
	 * 
	 * @param subjectHashes
	 * @return
	 */
	protected String getSubjectsDataQuery(Collection<Long> subjectHashes) {

		StringBuffer buf = new StringBuffer().
		append("select distinct ").
		append("SUBJECT as SUBJECT_HASH, SUBJ_RESOURCE.URI as SUBJECT_URI, SUBJ_RESOURCE.LASTMODIFIED_TIME as SUBJECT_MODIFIED, ").
		append("PREDICATE as PREDICATE_HASH, PRED_RESOURCE.URI as PREDICATE_URI, ").
		append("OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, OBJ_SOURCE_OBJECT, OBJ_DERIV_SOURCE, SOURCE, ").
		append("SRC_RESOURCE.URI as SOURCE_URI, DSRC_RESOURCE.URI as DERIV_SOURCE_URI ").
		append("from SPO ").
		append("left join RESOURCE as SUBJ_RESOURCE on (SUBJECT=SUBJ_RESOURCE.URI_HASH) ").
		append("left join RESOURCE as PRED_RESOURCE on (PREDICATE=PRED_RESOURCE.URI_HASH) ").
		append("left join RESOURCE as SRC_RESOURCE on (SOURCE=SRC_RESOURCE.URI_HASH) ").
		append("left join RESOURCE as DSRC_RESOURCE on (OBJ_DERIV_SOURCE=DSRC_RESOURCE.URI_HASH) ").
		append("where ").
		append("SUBJECT in (").append(Util.toCSV(subjectHashes)).append(") ").  
		append("order by ").
		append("SUBJECT, PREDICATE, OBJECT");
		return buf.toString();
	}
}
