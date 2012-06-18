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
 *        Juhan Voolaid
 */

package eionet.cr.web.action;

import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;

/**
 * Shared SPARQL bookmars action bean.
 * 
 * @author Juhan Voolaid
 */
@UrlBinding("/sharedSparqlBookmarks.action")
public class SharedSparqlBookmarksActionBean extends AbstractActionBean {

    /**
     * Shared SPARQL bookmars.
     */
    private List<Map<String, String>> sharedSparqlBookmars;

    @DefaultHandler
    public Resolution view() throws DAOException {
        sharedSparqlBookmars = DAOFactory.get().getDao(HelperDAO.class).getSharedSparqlBookmarks();
        return new ForwardResolution("/pages/sharedSparqlBookmarks.jsp");
    }

    /**
     * @return the sharedSparqlBookmars
     */
    public List<Map<String, String>> getSharedSparqlBookmars() {
        return sharedSparqlBookmars;
    }

}
