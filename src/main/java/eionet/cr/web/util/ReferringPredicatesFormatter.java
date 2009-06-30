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
package eionet.cr.web.util;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.util.Hashes;
import eionet.cr.util.Util;
import eionet.cr.web.action.ReferencesActionBean;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class ReferringPredicatesFormatter implements Formatter{
	
	/** */
	private ReferencesActionBean actionBean;
	private String referringToHash;
	
	/**
	 * 
	 * @param referringToHash
	 */
	public ReferringPredicatesFormatter(ReferencesActionBean actionBean){
		
		this.actionBean = actionBean;
		
		SearchExpression searchExpression = actionBean.getSearchExpression();
		if (searchExpression!=null){
			referringToHash = searchExpression.isHash() ?
					searchExpression.toString() : String.valueOf(Hashes.spoHash(searchExpression.toString()));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.util.Formatter#format(java.lang.Object)
	 */
	public String format(Object object){
		
		if (object instanceof SubjectDTO){
			
			SubjectDTO subjectDTO = (SubjectDTO)object;
			
			/* collect labels of all predicates pointing to referringToHash (ignore derived object values) */
			
			LinkedHashSet<String> labels = new LinkedHashSet<String>();
			Map<String,Collection<ObjectDTO>> predicatesObjects = subjectDTO.getPredicates();
			if (predicatesObjects!=null && !predicatesObjects.isEmpty()){
				
				for (String predicate:predicatesObjects.keySet()){
					
					Collection<ObjectDTO> objects = predicatesObjects.get(predicate);
					if (objects!=null && !objects.isEmpty()){
						
						for (ObjectDTO objectDTO:objects){
							
							if (objectDTO.getSourceObjectLong()==0 && objectDTO.getValueHash().equals(referringToHash)){
								
								String predicateLabel = JstlFunctions.getPredicateLabel(actionBean.getPredicateLabels(), predicate);
								labels.add(predicateLabel);
							}
						}
					}
				}
			}
			
			/* return the above-found labels as a comma-separated list */
			
			return labels.isEmpty() ? "" : Util.toCSV(labels);
		}
		else
			return object.toString();
	}

}