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
 * Enriko Käsper, Tieto Estonia
 */
package eionet.cr.dao.postgre.helpers;

import java.util.Map;
import java.util.Set;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.common.Predicates;
import eionet.cr.util.Hashes;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;

/**
 * @author <a href="mailto:enriko.kasper@tieto.com">Enriko Käsper</a>, Tieto Estonia
 */

public class FilteredTypeSearchHelper extends FilteredSearchHelper {
	
	private String type = null;
	private long typeHash = 0;

	public FilteredTypeSearchHelper(Map<String, String> filters, Set<String> literalPredicates,
			PagingRequest pagingRequest, SortingRequest sortingRequest) {
		
		super(filters, literalPredicates, pagingRequest, sortingRequest);
		
		String type = null;
		
		if(filters!=null && !filters.isEmpty() && filters.containsKey(Predicates.RDF_TYPE)){
			type= filters.get(Predicates.RDF_TYPE);
		}		
		if (type==null){
			throw new CRRuntimeException("Type URI can not be empty!");
		}	
		setUseCache(true);
		setType(type);
		removeFilter(Predicates.RDF_TYPE);
	}

	public void setType(String type){
		this.type = type;
		this.typeHash = Hashes.spoHash(type);
	}
	protected String getSpoTableName(){
		return isUseCache() ? 
				"CACHE_SPO_TYPE_SUBJECT":
					super.getSpoTableName();
	}

	protected String getSpoTableCriteria(){
		return isUseCache() ? 
			"SPO1.OBJECT_HASH=" + typeHash :
				super.getSpoTableCriteria();
	}
	public void setUserCache(boolean useCache){
		super.setUseCache(useCache);
		if(!useCache){
			addFilter(Predicates.RDF_TYPE, this.type);
		}
	}
	protected int getSpoTableIndex(){
		return 2;
	}
}
