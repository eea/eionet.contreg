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

package eionet.cr.web.action;

import java.util.Collections;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.log4j.Logger;

import eionet.cr.dao.BrowseVoidDatasetsDAO;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.util.VoidDatasetsResultRow;
import eionet.cr.web.action.factsheet.FactsheetActionBean;

/**
 * Action bean that provides functions for browsing VoID (Vocabulary of Interlinked Datasets) datasets.
 * Browsing done by two facets: dct:creator and dct:subject, where "dct" stands for DublinCore Terms (http://purl.org/dc/terms/).
 *
 * @author jaanus
 */
@UrlBinding("/browseDatasets.action")
public class BrowseDatasetsActionBean extends AbstractActionBean{

    /** */
    private static final Logger LOGGER = Logger.getLogger(BrowseDatasetsActionBean.class);

    /** Forward path to the JSP that handles the display and faceted browsing of VoID datasets. */
    private static final String BROWSE_DATASETS_JSP = "/pages/browseDatasets.jsp";

    /** The creator to search by. Corresponds to http://purl.org/dc/terms/creator. */
    private String creator;

    /** The subject to search by. Corresponds to http://purl.org/dc/terms/subject. */
    private String subject;

    /** The found datasets. */
    private List<VoidDatasetsResultRow> datasets;

    /**
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution defaultEvent() throws DAOException{

        List<String> creators = Collections.singletonList(creator);
        List<String> subjects = Collections.singletonList(subject);

        LOGGER.debug("Searching for VoID datasets, creators = " + creators + ", subjects = " + subjects);

        BrowseVoidDatasetsDAO dao = DAOFactory.get().getDao(BrowseVoidDatasetsDAO.class);
        datasets = dao.findDatasets(creators, subjects);

        LOGGER.debug(datasets.size() + " datasets found!");
        return new ForwardResolution(BROWSE_DATASETS_JSP);
    }

    /**
     * @return the creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * @param creator the creator to set
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the datasets
     */
    public List<VoidDatasetsResultRow> getDatasets() {
        return datasets;
    }

    /**
     * Returns the Java class object of the {@link FactsheetActionBean}.
     * Used for building refactoring-safe links to that bean in JSP page(s).
     *
     * @return
     */
    public Class getFactsheetActionBeanClass(){
        return FactsheetActionBean.class;
    }
}
