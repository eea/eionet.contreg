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

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.CompiledDatasetDAO;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.web.util.tabs.FactsheetTabMenuHelper;
import eionet.cr.web.util.tabs.TabElement;

/**
 * Compiled dataset tab controller.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/compiledDataset.action")
public class CompiledDatasetActionBean extends AbstractActionBean {

    /** URI by which the factsheet has been requested. */
    private String uri;

    /** Compiled dataset sources. */
    private List<SubjectDTO> sources;

    private List<TabElement> tabs;

    /**
     * Action event for displaying dataset sources.
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution view() throws DAOException {

        if (StringUtils.isEmpty(uri)) {
            addCautionMessage("No request criteria specified!");
        } else {
            HelperDAO helperDAO = DAOFactory.get().getDao(HelperDAO.class);
            SubjectDTO subject = helperDAO.getFactsheet(uri, null, null);
            sources = factory.getDao(CompiledDatasetDAO.class).getDetailedDatasetFiles(uri);

            FactsheetTabMenuHelper helper = new FactsheetTabMenuHelper(uri, subject, factory.getDao(HarvestSourceDAO.class));
            tabs = helper.getTabs(FactsheetTabMenuHelper.TabTitle.COMPILED_DATASET);
        }

        return new ForwardResolution("/pages/compiledDataset.jsp");
    }

    /**
     * Action event for reloading dataset.
     *
     * @return
     * @throws DAOException
     */
    public Resolution reload() throws DAOException {
        boolean success = false;
        if (getUser() != null) {
            if (isUsersDataset()) {
                // TODO: implement reload
                logger.info("test: " + uri);
                success = true;
            } else {
                addCautionMessage("User must be the owner of the compiled dataset!");
            }
        } else {
            addCautionMessage("User must be logged in!");
        }

        if (success) {
            addSystemMessage("Compiled dataset reloaded successfully");
        }
        return view();
    }

    /**
     * True, if the dataset with given uri belongs to the currently logged in user.
     *
     * @return
     */
    public boolean isUsersDataset() {
        //FIXME: demporary removed
        return false;
        /*
        if (getUser() == null) {
            return false;
        }
        return uri.contains("/home/" + getUserName());
        */
    }

    /**
     * @return the tabs
     */
    public List<TabElement> getTabs() {
        return tabs;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri
     *            the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the sources
     */
    public List<SubjectDTO> getSources() {
        return sources;
    }

}
