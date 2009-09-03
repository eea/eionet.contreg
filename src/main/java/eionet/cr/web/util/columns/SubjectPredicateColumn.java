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

import org.apache.commons.lang.StringUtils;

import net.sourceforge.stripes.action.UrlBinding;

import eionet.cr.common.Predicates;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.URIUtil;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;
import eionet.cr.web.action.FactsheetActionBean;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubjectPredicateColumn extends SearchResultColumn{

	/** */
	private String predicateUri;
	
	public SubjectPredicateColumn() {
		//blank
	}
	
	/**
	 * @param title
	 * @param isSortable
	 * @param predicateUri
	 */
	public SubjectPredicateColumn(String title, boolean isSortable, String predicateUri) {
		super(title, isSortable);
		this.predicateUri = predicateUri;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param title
	 * @param isSortable
	 * @param predicateUri
	 * @param actionRequestParameter
	 */
	public SubjectPredicateColumn(String title, boolean isSortable, String predicateUri, String actionRequestParameter) {
		super(title, isSortable);
		this.predicateUri = predicateUri;
		setActionRequestParameter(actionRequestParameter);
	}
	
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
				if (objects!=null && !objects.isEmpty()){
				
					LinkedHashSet<ObjectDTO> distinctObjects = new LinkedHashSet<ObjectDTO>(objects);		
					StringBuffer bufLiterals = new StringBuffer();
					StringBuffer bufNonLiterals = new StringBuffer();

					String resultFromHitSource = null;
					for (Iterator<ObjectDTO> iter = distinctObjects.iterator(); iter.hasNext();){

						ObjectDTO objectDTO = iter.next();
						String objectString = objectDTO.getValue().trim();
						
						// if the source of the object matches the search-hit source of the subject then
						// remember the object value and break
						if (subjectDTO.getHitSource()>0 && objectDTO.getSourceHash()==subjectDTO.getHitSource()
								&& !StringUtils.isBlank(objectString)
								&& objectDTO.isLiteral()){

							resultFromHitSource = objectString;
							break;
						}
						
						if (objectString.length()>0){

							if (objectDTO.isLiteral())
								bufLiterals.append(bufLiterals.length()>0 ? ", " : "").append(objectString);
							else
								bufNonLiterals.append(bufNonLiterals.length()>0 ? ", " : "").append(objectString);
						}
					}

					// if there was a value found that came from search-hit source then prefer that one as the result
					if (!StringUtils.isBlank(resultFromHitSource)){
						result = resultFromHitSource;
					}
					else{
						result = bufLiterals.length()>0 ? bufLiterals.toString() : bufNonLiterals.toString();
					}
				}
			}

			// rdfs:label gets special treatment
			if (predicateUri.equals(Predicates.RDFS_LABEL)){
				
				// if the result is blank, then guess the value from subject
				if (result.trim().length()==0){

					if (subjectDTO.isAnonymous()){
						result = "Anonymous object";
					}
					else{
						String subjectUri = subjectDTO.getUri();
						if (URIUtil.isSchemedURI(subjectUri) && !URLUtil.isURL(subjectUri)){
							result = subjectUri;
						}
						else{
							int i = Math.max(Math.max(subjectUri.lastIndexOf('#'), subjectUri.lastIndexOf('/')), subjectUri.lastIndexOf(':'));
							if (i>=0){
								result = subjectUri.substring(i+1);
							}
							else{
								result = subjectUri;
							}
						}
					}
				}
				
				// no we are sure we have a label to display, so let's generate the factsheet link based on that
				String factsheetUrlBinding = FactsheetActionBean.class.getAnnotation(UrlBinding.class).value();
				int i = factsheetUrlBinding.lastIndexOf("/");
				StringBuffer href = new StringBuffer(i>=0 ? factsheetUrlBinding.substring(i+1) : factsheetUrlBinding).append("?");
				if (subjectDTO.isAnonymous()){
					href.append("uriHash=").append(subjectDTO.getUriHash());
				}
				else{
					href.append("uri=").append(Util.urlEncode(subjectDTO.getUri()));
				}				
				result = new StringBuffer("<a href=\"").append(href).append("\">").append(result).append("</a>").toString();
			}
		}
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.util.columns.SearchResultColumn#isEscapeXml()
	 */
	public boolean isEscapeXml(){
		
		return predicateUri.equals(Predicates.RDFS_LABEL) ? false : super.isEscapeXml();
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.util.search.SearchResultColumn#getSortParamValue()
	 */
	public String getSortParamValue() {		
		return predicateUri;
	}
}
