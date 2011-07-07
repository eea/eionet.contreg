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
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.web.action;

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dto.TagDTO;
import eionet.cr.util.Pair;
import eionet.cr.web.util.ApplicationCache;

/**
 * ActionBean class responsible for preparing data for index.jsp rendering.
 *
 * @author Aleksandr Ivanov <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
@UrlBinding("/index.jsp")
public class FrontpageActionBean extends AbstractActionBean {

    private List<Pair<String, String>> recentFiles;
    private List<TagDTO> tagCloud;

    @DefaultHandler
    public Resolution frontpage() {
        recentFiles = ApplicationCache.getRecentDiscoveredFiles(10);
        initTagCloud();
        return new ForwardResolution("/pages/index.jsp");
    }

    /**
     * @return the recentFiles
     */
    public List<Pair<String, String>> getRecentFiles() {
        return recentFiles;
    }

    /**
     * @return the tagCloud
     */
    public List<TagDTO> getTagCloud() {
        return tagCloud;
    }

    private void initTagCloud() {
        tagCloud =
                ApplicationCache.getTagCloudSortedByName(Integer.parseInt(GeneralConfig
                        .getProperty(GeneralConfig.TAGCLOUD_FRONTPAGE_SIZE)));
    }

}
