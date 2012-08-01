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

package eionet.cr.web.action.admin;

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dto.HarvestStatDTO;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.action.HarvestActionBean;

/**
 * Action bean for displaying harvest statistics.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/admin/harveststats")
public class HarvestStatsActionBean extends AbstractActionBean {

    private static final int HARVEST_STATS_LIMIT = 100;

    private boolean adminLoggedIn;

    private List<HarvestStatDTO> resultList;

    /**
     * Handles the page view.
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution view() throws DAOException {
        if (getUser() != null) {
            if (getUser().isAdministrator()) {
                adminLoggedIn = true;
                resultList = DAOFactory.get().getDao(HarvestDAO.class).getLastHarvestStats(HARVEST_STATS_LIMIT);
            } else {
                adminLoggedIn = false;
            }
        } else {
            adminLoggedIn = false;
        }
        return new ForwardResolution("/pages/admin/harvestStats.jsp");
    }

    /**
     * @return the adminLoggedIn
     */
    public boolean isAdminLoggedIn() {
        return adminLoggedIn;
    }

    /**
     * @param adminLoggedIn the adminLoggedIn to set
     */
    public void setAdminLoggedIn(boolean adminLoggedIn) {
        this.adminLoggedIn = adminLoggedIn;
    }

    /**
     * @return the resultList
     */
    public List<HarvestStatDTO> getResultList() {
        return resultList;
    }

    /**
     * @param resultList the resultList to set
     */
    public void setResultList(List<HarvestStatDTO> resultList) {
        this.resultList = resultList;
    }

    /**
     *
     * @return
     */
    public Class getHarvestDetailsBeanClass(){
        return HarvestActionBean.class;
    }
}
