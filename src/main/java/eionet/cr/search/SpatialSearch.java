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
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.search.util.BBOX;
import eionet.cr.search.util.SubjectHashesReader;
import eionet.cr.util.Hashes;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SpatialSearch extends AbstractSubjectSearch{
	
	/** */
	private BBOX box;
	
	/** */
	private String source;
	
	/** */
	private boolean googleEarthMode = false;
	
	/**
	 * 
	 * @param box
	 */
	public SpatialSearch(BBOX box, String source){
		this.box = box;
		this.source = source;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#getSubjectSelectSQL(java.util.List)
	 */
	protected String getSubjectSelectSQL(List inParameters) {

		if (box==null || box.isUndefined())
			return null;
		
		StringBuffer sqlBuf = new StringBuffer(googleEarthMode ? "select distinct" : " select").
		append(" SPO_POINT.SUBJECT as SUBJECT_HASH from SPO as SPO_POINT");
		
		if (!googleEarthMode){
			if (sortPredicate!=null){
				sqlBuf.append(" left join SPO as ORDERING on (SPO_POINT.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=?) ");
				inParameters.add(Long.valueOf(Hashes.spoHash(sortPredicate)));
			}
		}

		if (box.hasLatitude()){
			sqlBuf.append(", SPO as SPO_LAT");
		}
		if (box.hasLongitude()){
			sqlBuf.append(", SPO as SPO_LONG");
		}
		
		sqlBuf.append(" where SPO_POINT.PREDICATE=").append(Hashes.spoHash(Predicates.RDF_TYPE)).
		append(" and SPO_POINT.OBJECT_HASH=").append(Hashes.spoHash(Subjects.WGS_POINT));
		
		if (!StringUtils.isBlank(source)){
			sqlBuf.append(" and SPO_POINT.SOURCE=?");
			inParameters.add(Long.valueOf(Hashes.spoHash(source)));
		}
		
		if (box.hasLatitude()){
			sqlBuf.append(" and SPO_POINT.SUBJECT=SPO_LAT.SUBJECT and SPO_LAT.PREDICATE=").append(Hashes.spoHash(Predicates.WGS_LAT));
			if (box.getLatitudeSouth()!=null){
				sqlBuf.append(" and SPO_LAT.OBJECT_DOUBLE>=?");
				inParameters.add(box.getLatitudeSouth());
			}
			if (box.getLatitudeNorth()!=null){
				sqlBuf.append(" and SPO_LAT.OBJECT_DOUBLE<=?");
				inParameters.add(box.getLatitudeNorth());
			}
		}
		
		if (box.hasLongitude()){
			
			if (box.hasLatitude())
				sqlBuf.append(" and SPO_LAT.SUBJECT=SPO_LONG.SUBJECT");
			else
				sqlBuf.append(" and SPO_POINT.SUBJECT=SPO_LONG.SUBJECT"); 
						
			sqlBuf.append(" and SPO_LONG.PREDICATE=").append(Hashes.spoHash(Predicates.WGS_LONG));
			
			if (box.getLongitudeWest()!=null){
				sqlBuf.append(" and SPO_LONG.OBJECT_DOUBLE>=?");
				inParameters.add(box.getLongitudeWest());
			}
			if (box.getLongitudeEast()!=null){
				sqlBuf.append(" and SPO_LONG.OBJECT_DOUBLE<=?");
				inParameters.add(box.getLongitudeEast());
			}
		}
		
		if (!googleEarthMode){
			if (sortPredicate!=null){
				sqlBuf.append(" order by ORDERING.OBJECT ").append(sortOrder==null ? sortOrder.ASCENDING.toSQL() : sortOrder.toSQL());
			}
		}
		else{
			sqlBuf.append(" order by SPO_POINT.SUBJECT");
		}
		
		if (googleEarthMode){
			if (pageLength>0){
				sqlBuf.append(" limit ");
				if (pageNumber>0){
					sqlBuf.append("?,");
					inParameters.add(new Integer((pageNumber-1)*pageLength));
				}
				sqlBuf.append(pageLength);
			}
		}
		
		return sqlBuf.toString();
	}

	/**
	 * @param googleEarthMode the googleEarthMode to set
	 */
	public void setGoogleEarthMode(boolean googleEarthMode) {
		this.googleEarthMode = googleEarthMode;
	}
}
