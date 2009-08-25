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
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.util.pagination;

import java.util.LinkedList;
import java.util.List;

/**
 * Encapsulates page request data.
 * 
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class PaginationRequest {
	//total matches
	private int matchCount;
	//page number requested
	private int pageNumber;
	//max items on page
	private int itemsPerPage;
	
	/**
	 * @param pageNumber
	 * @param itemsPerPage
	 */
	public PaginationRequest(int pageNumber, int itemsPerPage) {
		this.pageNumber = pageNumber;
		this.itemsPerPage = itemsPerPage;
	}
	
	/**
	 * @return List of parameters to be injected into sql statement or null, if params should not be injected.
	 */
	@SuppressWarnings("unchecked")
	public List<Object> getLimitParams() {
		List result = new LinkedList();
		if (pageNumber <= 0) {
			//user wants all records.
			return null;
			
		} 
		
		//adding starting element
		result.add(
				Math.max(
						0,
						(pageNumber - 1) * itemsPerPage));
		//adding end element
		result.add(pageNumber * itemsPerPage);
		return result;
	}
	
	/**
	 * @return the matchCount
	 */
	public int getMatchCount() {
		return matchCount;
	}
	/**
	 * @param matchCount the matchCount to set
	 */
	public void setMatchCount(int matchCount) {
		this.matchCount = matchCount;
	}
	/**
	 * @return the pageNumber
	 */
	public int getPageNumber() {
		return pageNumber;
	}
	/**
	 * @param pageNumber the pageNumber to set
	 */
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	/**
	 * @return the itemsPerPage
	 */
	public int getItemsPerPage() {
		return itemsPerPage;
	}
	/**
	 * @param itemsPerPage the itemsPerPage to set
	 */
	public void setItemsPerPage(int itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}

}
