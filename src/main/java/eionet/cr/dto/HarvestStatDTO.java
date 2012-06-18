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

package eionet.cr.dto;

/**
 * Data of harvest statistics.
 * 
 * @author Juhan Voolaid
 */
public class HarvestStatDTO extends HarvestDTO {

    /** Harvest source url. */
    private String sourceUrl;

    /** Harvest duaration in milliseconds. */
    private Long duration;

    /** Duration / total statements */
    private Long statementDuration;

    /**
     * @return the sourceUrl
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * @param sourceUrl the sourceUrl to set
     */
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    /**
     * @return the duration
     */
    public Long getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(Long duration) {
        this.duration = duration;
    }

    /**
     * @return the statementDuration
     */
    public Long getStatementDuration() {
        return statementDuration;
    }

    /**
     * @param statementDuration the statementDuration to set
     */
    public void setStatementDuration(Long statementDuration) {
        this.statementDuration = statementDuration;
    }

}
