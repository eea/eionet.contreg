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

import org.apache.commons.lang.StringEscapeUtils;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.web.action.HarvestSourcesActionBean;

/**
 * Basic implementation of {@link SearchResultColumn}.
 *
 * @author Aleksandr Ivanov <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class GenericColumn extends SearchResultColumn {

    /*
     * (non-Javadoc)
     * @see eionet.cr.web.util.columns.SearchResultColumn#format(java.lang.Object)
     */
    @Override
    public String format(Object object) {

        if (object instanceof HarvestSourceDTO && actionBean instanceof HarvestSourcesActionBean) {

            return "<input type='checkbox' value='" + StringEscapeUtils.escapeHtml(((HarvestSourceDTO) object).getUrl())
                    + "' name='sourceUrl'/>";
        }
        return "";
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.web.util.columns.SearchResultColumn#getSortParamValue()
     */
    @Override
    public String getSortParamValue() {
        // TODO Auto-generated method stub
        return null;
    }
}
