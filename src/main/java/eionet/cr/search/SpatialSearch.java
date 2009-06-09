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

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.search.util.CoordinateBox;
import eionet.cr.util.Hashes;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SpatialSearch extends AbstractSubjectSearch{
	
	/** */
	private CoordinateBox box;
	
	/**
	 * 
	 * @param box
	 */
	public SpatialSearch(CoordinateBox box){
		this.box = box;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#getSubjectSelectSQL(java.util.List)
	 */
	protected String getSubjectSelectSQL(List inParameters) {

		if (box==null || box.isUndefined())
			return null;
		
		StringBuffer buf = new StringBuffer("select SPO_SPAT.SUBJECT as SUBJECT_HASH from SPO as SPO_SPAT");
		if (sortPredicate!=null){
			buf.append(" left join SPO as ORDERING on (SPO_SPAT.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=?) ");
			inParameters.add(Long.valueOf(Hashes.spoHash(sortPredicate)));
		}

		if (box.hasLatitude()){
			buf.append(", SPO as SPO_LAT");
		}
		if (box.hasLongitude()){
			buf.append(", SPO as SPO_LONG");
		}
		
		buf.append(" where SPO_SPAT.PREDICATE=").append(Hashes.spoHash(Predicates.RDF_TYPE)).
		append(" and SPO_SPAT.OBJECT_HASH=").append(Hashes.spoHash(Subjects.WGS_SPATIAL_THING));
		
		if (box.hasLatitude()){
			buf.append(" and SPO_SPAT.SUBJECT=SPO_LAT.SUBJECT and SPO_LAT.PREDICATE=").append(Hashes.spoHash(Predicates.WGS_LAT));
			if (box.getLowerLatitude()!=null){
				buf.append(" and SPO_LAT.OBJECT_DOUBLE>=?");
				inParameters.add(box.getLowerLatitude());
			}
			if (box.getUpperLatitude()!=null){
				buf.append(" and SPO_LAT.OBJECT_DOUBLE<=?");
				inParameters.add(box.getUpperLatitude());
			}
		}
		
		if (box.hasLongitude()){
			
			if (box.hasLatitude())
				buf.append(" and SPO_LAT.SUBJECT=SPO_LONG.SUBJECT");
			else
				buf.append(" and SPO_SPAT.SUBJECT=SPO_LONG.SUBJECT"); 
						
			buf.append(" and SPO_LONG.PREDICATE=").append(Hashes.spoHash(Predicates.WGS_LONG));
			
			if (box.getLowerLongitude()!=null){
				buf.append(" and SPO_LONG.OBJECT_DOUBLE>=?");
				inParameters.add(box.getLowerLongitude());
			}
			if (box.getUpperLongitude()!=null){
				buf.append(" and SPO_LONG.OBJECT_DOUBLE<=?");
				inParameters.add(box.getUpperLongitude());
			}
		}
		
		if (sortPredicate!=null)
			buf.append(" order by ORDERING.OBJECT ").append(sortOrder==null ? sortOrder.ASCENDING.toSQL() : sortOrder.toSQL());
		
		return buf.toString();
	}
}
