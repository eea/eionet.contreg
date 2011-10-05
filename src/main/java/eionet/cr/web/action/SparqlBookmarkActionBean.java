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

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;

/**
 * Sparql bookmark tab controller. The tabmenu is part of factsheet page.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/sparqlBookmark.action")
public class SparqlBookmarkActionBean extends FactsheetActionBean {

    @DefaultHandler
    public Resolution view() throws DAOException {
        if (isNoCriteria()) {
            addCautionMessage("No request criteria specified!");
        } else {
            HelperDAO helperDAO = DAOFactory.get().getDao(HelperDAO.class);

            setAdminLoggedIn(getUser() != null && getUser().isAdministrator());

            subject = helperDAO.getFactsheet(uri, null, getPredicatePageNumbers());
        }
        return new ForwardResolution("/pages/sparqlBookmark.jsp");
    }

    public String getSpqrqlQuery() {
        return subject.getObject(Predicates.CR_SPARQL_QUERY).getValue();
    }
}
