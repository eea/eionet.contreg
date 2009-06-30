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
package eionet.cr.web.util.columns;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubjectPredicateColumn extends SearchResultColumn{

	/** */
	private String predicateUri;
	
	/**
	 * @return the predicateUri
	 */
	public String getPredicateUri() {
		return predicateUri;
	}
	/**
	 * @param predicateUri the predicateUri to set
	 */
	public void setPredicateUri(String predicateUri) {
		this.predicateUri = predicateUri;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.util.search.SearchResultColumn#format(java.lang.Object)
	 * 
	 * Gets the collection of objects matching to the given predicate in the given subject.
	 * Formats the given collection to comma-separated string and returns it.
	 * Only distinct objects and only literal ones are selected (unless there is not a single literal
	 * in which case the non-literals are returned.
	 */
	public String format(Object object){
		
		String result = "";
		if (object!=null && object instanceof SubjectDTO && predicateUri!=null){
			
			SubjectDTO subjectDTO = (SubjectDTO)object;
			if (subjectDTO.getPredicateCount()>0){
				
				Collection<ObjectDTO> objects = subjectDTO.getObjects(predicateUri);
				if (objects==null || objects.isEmpty())
					return "";
				
				LinkedHashSet<ObjectDTO> distinctObjects = new LinkedHashSet<ObjectDTO>(objects);		
				StringBuffer bufLiterals = new StringBuffer();
				StringBuffer bufNonLiterals = new StringBuffer();
				
				for (Iterator<ObjectDTO> iter = distinctObjects.iterator(); iter.hasNext();){
					
					ObjectDTO objectDTO = iter.next();
					String objectString = objectDTO.toString().trim();
					if (objectString.length()>0){
						
						if (objectDTO.isLiteral())
							bufLiterals.append(bufLiterals.length()>0 ? ", " : "").append(objectString);
						else
							bufNonLiterals.append(bufNonLiterals.length()>0 ? ", " : "").append(objectString);
					}
				}
				
				result = bufLiterals.length()>0 ? bufLiterals.toString() : bufNonLiterals.toString();
			}
		}
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.util.search.SearchResultColumn#getSortParamValue()
	 */
	public String getSortParamValue() {		
		return predicateUri;
	}
}
