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
package eionet.cr.dto;

import java.io.Serializable;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class UrgentHarvestQueueItemDTO implements Serializable {

    /** */
    private String url;
    private java.util.Date timeAdded;
    private String pushedContent;

    /**
     *
     */
    public UrgentHarvestQueueItemDTO() {
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the timeAdded
     */
    public java.util.Date getTimeAdded() {
        return timeAdded;
    }

    /**
     * @param timeAdded the timeAdded to set
     */
    public void setTimeAdded(java.util.Date timeAdded) {
        this.timeAdded = timeAdded;
    }

    /**
     * @return the pushedContent
     */
    public String getPushedContent() {
        return pushedContent;
    }

    /**
     * @param pushedContent the pushedContent to set
     */
    public void setPushedContent(String pushedContent) {
        this.pushedContent = pushedContent;
    }

    /**
     *
     * @return
     */
    public boolean isPushHarvest() {
        return pushedContent != null;
    }
}
