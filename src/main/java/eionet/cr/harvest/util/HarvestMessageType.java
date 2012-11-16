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
package eionet.cr.harvest.util;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public enum HarvestMessageType {

    FATAL("ftl"), ERROR("err"), WARNING("wrn"), INFO("inf");

    /** */
    private String value;

    /**
     *
     * @param value
     */
    private HarvestMessageType(String value) {
        this.value = value;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     *
     * @param str
     * @return
     */
    public static HarvestMessageType parseFrom(String str) {

        if (StringUtils.isBlank(str)) {
            return null;
        } else {
            for (HarvestMessageType messageType : HarvestMessageType.values()) {

                if (str.equals(messageType.toString())) {
                    return messageType;
                }
            }
        }

        return null;
    }
}
