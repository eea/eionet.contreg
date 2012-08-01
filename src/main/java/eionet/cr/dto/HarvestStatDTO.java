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

import java.text.DecimalFormat;
import java.util.Date;

/**
 * Data of harvest statistics.
 *
 * @author Juhan Voolaid
 */
public class HarvestStatDTO extends HarvestDTO {

    /** */
    private static final DecimalFormat DURATION_STATEMENTS_RATIO_FORMAT = new DecimalFormat("#.###");

    /** Harvest source url. */
    private String sourceUrl;

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
    public Integer getDuration() {

        Date started = this.getDatetimeStarted();
        Date finished = this.getDatetimeFinished();
        if (started == null || finished == null) {
            return null;
        }

        long duration = Math.max(finished.getTime() / 1000L - started.getTime() / 1000L, 0L);
        return duration == 0 ? 1 : Long.valueOf(duration).intValue();
    }

    /**
     * @return the statementDuration
     */
    public Double getDurationStatementsRatio() {

        Integer duration = getDuration();
        Integer statements = getTotalStatements();

        if (duration == null || duration.intValue() <= 0 || statements == null || statements.intValue() <= 0){
            return null;
        }
        else{
            double ratio = duration.doubleValue() / statements.doubleValue();
            return Double.valueOf(Math.round(ratio * 100) / 100.0d);
        }
    }
}
