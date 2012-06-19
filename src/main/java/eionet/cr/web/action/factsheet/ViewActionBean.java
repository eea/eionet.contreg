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
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */

package eionet.cr.web.action.factsheet;

import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.util.tabs.FactsheetTabMenuHelper;
import eionet.cr.web.util.tabs.TabElement;

/**
 * A bean that checks if the subject by the given URI has a type-specific factsheet tab, and if it has indeed, then redirects to
 * that tab. Otherwise redirects simply to {@link FactsheetActionBean}.
 *
 * @author Jaanus Heinlaid
 */
@UrlBinding("/view.action")
public class ViewActionBean extends AbstractActionBean {

    /** Uri. */
    private String uri;

    /**
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution defaultHandler() throws DAOException {

        RedirectResolution resolution = null;

        SubjectDTO subject = StringUtils.isBlank(uri) ? null : DAOFactory.get().getDao(HelperDAO.class).getSubject(uri);
        if (subject != null) {

            FactsheetTabMenuHelper tabsHelper = new FactsheetTabMenuHelper(uri, subject, factory.getDao(HarvestSourceDAO.class));
            List<TabElement> typeSpecificTabs = tabsHelper.getTypeSpecificTabs();
            if (typeSpecificTabs != null && !typeSpecificTabs.isEmpty()) {

                TabElement firstTab = typeSpecificTabs.get(0);
                String redirectLocation = firstTab.getHref();
                if (!StringUtils.isBlank(redirectLocation)) {

                    resolution = new RedirectResolution(redirectLocation);

                    String event = firstTab.getEvent();
                    if (!StringUtils.isBlank(event)) {
                        resolution.addParameter(event, "");
                    }

                    Map<String, Object> params = firstTab.getParams();
                    if (params != null && !params.isEmpty()) {
                        for (Map.Entry<String, Object> param : params.entrySet()) {
                            resolution.addParameter(param.getKey(), param.getValue());
                        }
                    }
                }
            }
        }

        if (resolution == null) {
            resolution = new RedirectResolution(FactsheetActionBean.class);
            resolution.addParameter("uri", uri == null ? "" : uri);
        }

        return resolution;
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
}
