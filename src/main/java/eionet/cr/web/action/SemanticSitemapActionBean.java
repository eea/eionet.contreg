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

import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.log4j.Logger;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.UploadDTO;
import eionet.cr.web.util.SitemapXmlWriter;

/**
 * Semantic sitemap controller.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/sitemap.xml")
public class SemanticSitemapActionBean extends AbstractActionBean {

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(SemanticSitemapActionBean.class);

    /**
     *
     * @return
     */
    @DefaultHandler
    public Resolution getSitemap() {
        StreamingResolution result = new StreamingResolution("application/xml") {
            public void stream(HttpServletResponse response) throws Exception {
                List<UploadDTO> uploads = DAOFactory.get().getDao(HelperDAO.class).getAllRdfUploads();
                SitemapXmlWriter xmlWriter = new SitemapXmlWriter(response.getOutputStream());
                xmlWriter.writeSitemapXml(uploads);
            }
        };
        return result;
    }
}
