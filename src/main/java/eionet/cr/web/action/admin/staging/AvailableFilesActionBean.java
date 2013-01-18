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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        jaanus
 */

package eionet.cr.web.action.admin.staging;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import eionet.cr.web.action.AbstractActionBean;

/**
 * Action bean that lists files available for the creation of staging databases, and provides other actions on these files as well.
 *
 * @author jaanus
 */
public class AvailableFilesActionBean extends AbstractActionBean {

    /** */
    private static final String LIST_JSP = "/pages/admin/staging/availableFiles.jsp";

    /**
     *
     * @return
     */
    @DefaultHandler
    public Resolution list() {
        return new ForwardResolution("");
    }
}
