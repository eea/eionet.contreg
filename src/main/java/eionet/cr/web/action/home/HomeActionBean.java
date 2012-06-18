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
package eionet.cr.web.action.home;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.web.action.AbstractActionBean;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 * 
 */

@UrlBinding("/home/{username}")
public class HomeActionBean extends AbstractActionBean {

    private String username;

    /**
     * 
     * @return Resolution
     */
    @DefaultHandler
    public Resolution noEvent() {

        try {
            if (StringUtils.isBlank(username)) {
                throw new CRRuntimeException("Could not detect username from request parameters");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CRRuntimeException(e.getMessage());
        }

        return new RedirectResolution("/home/" + username + "/uploads");
    }

    /**
     * Sets username request parameter.
     * 
     * @param username request parameter
     */
    public void setUsername(String username) {
        this.username = username;
    }
}
