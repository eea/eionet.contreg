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

package eionet.cr.web.action.source;

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.TripleDTO;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.tabs.SourceTabMenuHelper;
import eionet.cr.web.util.tabs.TabElement;

/**
 * Source triples tab.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/sourceTriples.action")
public class SourceTriplesActionBean extends AbstractActionBean {

    /** URI of the source. */
    private String uri;

    /** Tabs. */
    private List<TabElement> tabs;

    /** Sample triples. */
    private List<TripleDTO> sampleTriples;

    /**
     * Action event for displaying the source triples.
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution view() throws DAOException {

        if (StringUtils.isEmpty(uri)) {
            addCautionMessage("No request criteria specified!");
        } else {
            sampleTriples = DAOFactory.get().getDao(HelperDAO.class).getSampleTriplesInSource(uri, PagingRequest.create(1, 10));
            HarvestSourceDTO harvestSource = factory.getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(uri);

            SourceTabMenuHelper helper = new SourceTabMenuHelper(uri, isUserOwner(harvestSource));
            tabs = helper.getTabs(SourceTabMenuHelper.TabTitle.SAMPLE_TRIPLES);
        }

        return new ForwardResolution("/pages/source/sourceTriples.jsp");
    }

    /**
     * Chekcs if user is owner of the harvest source.
     *
     * @param harvestSourceDTO
     * @return
     */
    private boolean isUserOwner(HarvestSourceDTO harvestSourceDTO) {

        boolean result = false;

        if (harvestSourceDTO != null) {

            String sourceOwner = harvestSourceDTO.getOwner();
            CRUser user = getUser();

            if (user != null && (user.isAdministrator() || user.getUserName().equals(sourceOwner))) {
                result = true;
            }
        }

        return result;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the tabs
     */
    public List<TabElement> getTabs() {
        return tabs;
    }

    /**
     * @return the sampleTriples
     */
    public List<TripleDTO> getSampleTriples() {
        return sampleTriples;
    }

}
