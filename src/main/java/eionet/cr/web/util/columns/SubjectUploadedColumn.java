/*
 * The contents of this file are subject to the Mozilla Public
 *
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
 * Agency. Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency. All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.web.util.columns;

import java.util.Date;

import eionet.cr.dto.SubjectDTO;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubjectUploadedColumn extends SearchResultColumn {

    /** */

    public SubjectUploadedColumn() {
        // blank constructor
    }

    /**
     * @param title
     * @param isSortable
     */
    public SubjectUploadedColumn(String title, boolean isSortable) {
        super(title, isSortable);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.web.util.search.SearchResultColumn#format(java.lang.Object)
     */
    public String format(Object object) {

        String result = "";
        if (object != null && object instanceof SubjectDTO) {

            Date date = ((SubjectDTO) object).getDcDate();
            if (date != null) {
                result = SIMPLE_DATE_FORMAT.format(date);
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.web.util.search.SearchResultColumn#getSortParamValue()
     */
    public String getSortParamValue() {
        return getClass().getSimpleName();
    }

}
