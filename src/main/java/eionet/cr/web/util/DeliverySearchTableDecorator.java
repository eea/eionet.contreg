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

import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.displaytag.decorator.TableDecorator;

import eionet.cr.dto.DeliveryDTO;
import eionet.cr.util.Util;
import eionet.cr.web.action.factsheet.FactsheetActionBean;

/**
 *
 * @author altnyris
 *
 */
public class DeliverySearchTableDecorator extends TableDecorator {

    /**
     *
     * @return
     */
    public String getTitle() {

        StringBuffer buf = new StringBuffer();
        String url = ((DeliveryDTO) getCurrentRowObject()).getSubjectUri();
        String title = ((DeliveryDTO) getCurrentRowObject()).getTitle();
        if (!StringUtils.isBlank(url)) {
            String factsheetUrlBinding = FactsheetActionBean.class.getAnnotation(UrlBinding.class).value();
            int i = factsheetUrlBinding.lastIndexOf("/");

            StringBuffer href = new StringBuffer(i >= 0 ? factsheetUrlBinding.substring(i + 1) : factsheetUrlBinding).append("?");
            href.append("uri=").append(Util.urlEncode(url));

            buf.append("<a title=\"Title\" href=\"").append(href).append("\">");
            buf.append(StringEscapeUtils.escapeXml(title)).append("</a>");
        } else {
            buf.append(title);
        }

        return buf.toString();
    }

    public String getPeriodValue() {
        StringBuffer buf = new StringBuffer();
        String start = ((DeliveryDTO) getCurrentRowObject()).getStartYear();
        String end = ((DeliveryDTO) getCurrentRowObject()).getEndYear();
        if (StringUtils.isBlank(end) || end.equals("0") || (start != null && end != null && start.equals(end))) {
            buf.append(start);
        } else {
            buf.append("From ").append(start).append(" to ").append(end);
        }
        return buf.toString();
    }

}
