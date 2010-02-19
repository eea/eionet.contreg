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
package eionet.cr.dao.postgre.helpers;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.search.util.BBOX;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.search.util.SortOrder;
import eionet.cr.util.Hashes;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SpatialSearchHelper {

	/** */
	private BBOX box;
	private String sourceUri;
	private PagingRequest pagingRequest;
	private String sortPredicate;
	private String sortOrder;
	private boolean sortByObjectHash;
	
	/**
	 * 
	 * @param expression
	 * @param pagingRequest
	 * @param sortingRequest
	 */
	private SpatialSearchHelper(BBOX box, String sourceUri,
			PagingRequest pagingRequest, SortingRequest sortingRequest, boolean sortByObjectHash){
		
		this.box = box;
		this.sourceUri= sourceUri;		
		
		this.pagingRequest = pagingRequest;
		if (sortingRequest!=null){
			this.sortPredicate = sortingRequest.getSortingColumnName();
			this.sortOrder = sortingRequest.getSortOrder() == null  ? SortOrder.ASCENDING.toSQL() 
					: sortingRequest.getSortOrder().toSQL();
			this.sortByObjectHash = sortByObjectHash;
		}
	}
	
	/**
	 * 
	 * @param expression
	 * @param pagingRequest
	 * @param sortingRequest
	 * @return
	 */
	public static SpatialSearchHelper create(BBOX box, String sourceUri,
			PagingRequest pagingRequest,
			SortingRequest sortingRequest, boolean sortByObjectHash){
		return new SpatialSearchHelper(box, sourceUri, pagingRequest,
				sortingRequest, sortByObjectHash);
	}

	/**
	 * 
	 * @param inParams
	 * @return
	 */
	public String getQuery(List<Object> inParams){
		
		String query = StringUtils.isBlank(sortPredicate) ?
				getUnorderedQuery(inParams) : getOrderedQuery(inParams);
		
		if (pagingRequest!=null){
			return query;
		}
		else{
			return new StringBuffer(query).
			append(" limit ").append(pagingRequest.getItemsPerPage()).
			append(" offset ").append(pagingRequest.getOffset()).
			toString();
		}
	}
	
	/**
	 * 
	 * @param inParams
	 * @return
	 */
	public String getUnorderedQuery(List<Object> inParams){
		
		StringBuffer buf = new StringBuffer().
		append("select distinct SPO_POINT.SUBJECT as SUBJECT_HASH from SPO as SPO_POINT");
		
		if (box.hasLatitude()){
			buf.append(", SPO as SPO_LAT");
		}
		if (box.hasLongitude()){
			buf.append(", SPO as SPO_LONG");
		}
		
		buf.append(" where SPO_POINT.PREDICATE=").append(Hashes.spoHash(Predicates.RDF_TYPE)).
		append(" and SPO_POINT.OBJECT_HASH=").append(Hashes.spoHash(Subjects.WGS_POINT));
		
		if (!StringUtils.isBlank(sourceUri)){
			buf.append(" and SPO_POINT.SOURCE=").append(Hashes.spoHash(sourceUri));
		}
		
		if (box.hasLatitude()){
			
			buf.append(" and SPO_POINT.SUBJECT=SPO_LAT.SUBJECT and SPO_LAT.PREDICATE=").
			append(Hashes.spoHash(Predicates.WGS_LAT));
			if (box.getLatitudeSouth()!=null){
				buf.append(" and SPO_LAT.OBJECT_DOUBLE>=").append(box.getLatitudeSouth());
			}
			if (box.getLatitudeNorth()!=null){
				buf.append(" and SPO_LAT.OBJECT_DOUBLE<=").append(box.getLatitudeNorth());
			}
		}
		
		if (box.hasLongitude()){
			
			if (box.hasLatitude()){
				buf.append(" and SPO_LAT.SUBJECT=SPO_LONG.SUBJECT");
			}
			else{
				buf.append(" and SPO_POINT.SUBJECT=SPO_LONG.SUBJECT");
			}
			buf.append(" and SPO_LONG.PREDICATE=").append(Hashes.spoHash(Predicates.WGS_LONG));
			
			if (box.getLongitudeWest()!=null){
				buf.append(" and SPO_LONG.OBJECT_DOUBLE>=").append(box.getLongitudeWest());
			}
			if (box.getLongitudeEast()!=null){
				buf.append(" and SPO_LONG.OBJECT_DOUBLE<=").append(box.getLongitudeEast());
			}
		}
		
		return buf.toString();
	}
	
	/**
	 * 
	 * @param inParams
	 * @return
	 */
	public String getOrderedQuery(List<Object> inParams){
		
		StringBuffer subSelect = new StringBuffer().
		append("select distinct on (SUBJECT_HASH) SPO_POINT.SUBJECT as SUBJECT_HASH").
		append(sortByObjectHash ? ", ORDERING.OBJECT_HASH" : ", ORDERING.OBJECT").
		append(" as OBJECT_ORDERED_BY from SPO as SPO_POINT");
		
		subSelect.append(" left join SPO as ORDERING on").
		append(" (SPO_POINT.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=").
		append(Hashes.spoHash(sortPredicate)).
		append(")");

		if (box.hasLatitude()){
			subSelect.append(", SPO as SPO_LAT");
		}
		if (box.hasLongitude()){
			subSelect.append(", SPO as SPO_LONG");
		}
		
		subSelect.append(" where SPO_POINT.PREDICATE=").append(Hashes.spoHash(Predicates.RDF_TYPE)).
		append(" and SPO_POINT.OBJECT_HASH=").append(Hashes.spoHash(Subjects.WGS_POINT));
		
		if (!StringUtils.isBlank(sourceUri)){
			subSelect.append(" and SPO_POINT.SOURCE=").append(Hashes.spoHash(sourceUri));
		}
		
		if (box.hasLatitude()){
			
			subSelect.append(" and SPO_POINT.SUBJECT=SPO_LAT.SUBJECT and SPO_LAT.PREDICATE=").
			append(Hashes.spoHash(Predicates.WGS_LAT));
			if (box.getLatitudeSouth()!=null){
				subSelect.append(" and SPO_LAT.OBJECT_DOUBLE>=").append(box.getLatitudeSouth());
			}
			if (box.getLatitudeNorth()!=null){
				subSelect.append(" and SPO_LAT.OBJECT_DOUBLE<=").append(box.getLatitudeNorth());
			}
		}
		
		if (box.hasLongitude()){
			
			if (box.hasLatitude()){
				subSelect.append(" and SPO_LAT.SUBJECT=SPO_LONG.SUBJECT");
			}
			else{
				subSelect.append(" and SPO_POINT.SUBJECT=SPO_LONG.SUBJECT");
			}
			subSelect.append(" and SPO_LONG.PREDICATE=").append(Hashes.spoHash(Predicates.WGS_LONG));
			
			if (box.getLongitudeWest()!=null){
				subSelect.append(" and SPO_LONG.OBJECT_DOUBLE>=").append(box.getLongitudeWest());
			}
			if (box.getLongitudeEast()!=null){
				subSelect.append(" and SPO_LONG.OBJECT_DOUBLE<=").append(box.getLongitudeEast());
			}
		}
		
		StringBuffer buf = new StringBuffer().
		append("select * from (").append(subSelect).append(") as FOO order by OBJECT_ORDERED_BY");

		return buf.toString();
	}
	
	/**
	 * 
	 * @param inParams
	 * @return
	 */
	public String getCountQuery(List<Object> inParams){
		
		String query = getUnorderedQuery(inParams);
		return new StringBuffer(
				"select count(*) from (").append(query).append(") as FOO").toString();
	}
}
