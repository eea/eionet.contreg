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
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.web.util;

import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringEscapeUtils;
import org.displaytag.decorator.TableDecorator;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.util.Util;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestSourcesTableDecorator extends TableDecorator {

    /** */
    private SimpleDateFormat lastHarvestFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     *
     * @return
     */
    public String getUrl() {

        StringBuffer buf = new StringBuffer();
        String url = ((HarvestSourceDTO) getCurrentRowObject()).getUrl();
        if (url != null) {
            buf.append("<a class=\"link-plain\" href=\"source.action?view=&amp;harvestSource.url=").append(Util.urlEncode(url))
                    .append("\">").append(StringEscapeUtils.escapeXml(url)).append("</a>");
        }

        return buf.toString();
    }

    /**
     *
     * @return
     */
    public String getLastHarvest() {

        HarvestSourceDTO harvestSource = (HarvestSourceDTO) getCurrentRowObject();
        if (harvestSource.getLastHarvest() != null)
            return lastHarvestFormat.format(harvestSource.getLastHarvest());
        else
            return "&nbsp;";
    }
}
