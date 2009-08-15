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
package eionet.cr.web.action;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/registerUrl.action")
public class RegisterURLActionBean extends AbstractActionBean{
	
	@Validate(required=true, on={"save"})
	private String url;
	
	private boolean bookmark = false;

	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
	@DefaultHandler
	public Resolution unspecified() throws DAOException{
		
		return new ForwardResolution("/pages/registerUrl.jsp");
	}
	
	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
	public Resolution save() throws DAOException{
		
		SubjectDTO subjectDTO = new SubjectDTO(url, false);
		subjectDTO.addObject(Predicates.RDF_TYPE, new ObjectDTO(Subjects.CR_FILE, false));
		
		return new ForwardResolution("/pages/registerUrl.jsp");
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @param bookmark the bookmark to set
	 */
	public void setBookmark(boolean bookmark) {
		this.bookmark = bookmark;
	}
}
