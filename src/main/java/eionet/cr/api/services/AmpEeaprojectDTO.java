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
package eionet.cr.api.services;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@Root(name = "amp:eeaproject")
public class AmpEeaprojectDTO {

	/** */
	private static final long serialVersionUID = 1L;

	/** */
	@Attribute(name = "rdf:resource")
	private String eeaproject;
	
	/**
	 * @param type
	 */
	public AmpEeaprojectDTO(String eeaproject) {
		this.eeaproject = eeaproject;
	}

	/**
	 * @return the eeaproject
	 */
	public String getEeaproject() {
		return eeaproject;
	}

	/**
	 * @param eeaproject the eeaproject to set
	 */
	public void setEeaproject(String eeaproject) {
		this.eeaproject = eeaproject;
	}
}
