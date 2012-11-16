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
package eionet.cr.web.util.columns;

import java.util.Date;

import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringEscapeUtils;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.util.Util;
import eionet.cr.web.action.HarvestSourceActionBean;

/**
 * @author Aleksandr Ivanov <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class HarvestSourcesColumn extends SearchResultColumn {

    private boolean dateFormat;

    /**
     * @param dateFormat
     */
    public HarvestSourcesColumn(boolean dateFormat) {
        this.dateFormat = dateFormat;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.web.util.columns.SearchResultColumn#format(java.lang.Object)
     */
    @Override
    public String format(Object object) {
        if (object instanceof HarvestSourceDTO) {
            if (dateFormat) {
                Date date = ((HarvestSourceDTO) object).getLastHarvest();
                return date != null ? SIMPLE_DATE_FORMAT.format(date) : "";
            } else {
                String factsheetUrlBinding = HarvestSourceActionBean.class.getAnnotation(UrlBinding.class).value();
                int i = factsheetUrlBinding.lastIndexOf("/");
                String hrefName = ((HarvestSourceDTO) object).getUrl();

                StringBuffer href =
                        new StringBuffer(i >= 0 ? factsheetUrlBinding.substring(i + 1) : factsheetUrlBinding).append(
                                "?view=&amp;harvestSource.url=").append(Util.urlEncode(hrefName));

                return new StringBuffer("<a href=\"").append(href).append("\">").append(StringEscapeUtils.escapeHtml(hrefName))
                        .append("</a>").toString();
            }
        }
        return "";
    }

    /**
     * @see eionet.cr.web.util.columns.SearchResultColumn#getSortParamValue() {@inheritDoc}
     */
    @Override
    public String getSortParamValue() {
        return dateFormat ? "LAST_HARVEST" : "URL";
    }

}
