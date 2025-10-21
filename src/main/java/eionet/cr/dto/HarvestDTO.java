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

import eionet.cr.harvest.BaseHarvest;
import eionet.cr.web.util.WebConstants;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.Date;

/**
 *
 * @author heinljab
 *
 */
public class HarvestDTO extends HarvestBaseDTO implements java.io.Serializable {

    private static final long serialVersionUID = -8541217014927046005L;
    /** */
    private Integer harvestId;
    private Integer harvestSourceId;
    private String harvestType;
    private String user;
    private String status;
    private java.util.Date datetimeStarted;
    private java.util.Date datetimeFinished;
    private Integer encodingSchemes;
    private Integer totalStatements;
    private Integer litObjStatements;

    /** HTTP response code. */
    private int responseCode;

    /**
     * Default constructor.
     */
    public HarvestDTO() {
        // Empty constructor.
    }

    /**
     *
     * @return
     */
    public String getDurationString() {
        if (datetimeStarted == null) {
            return WebConstants.NOT_AVAILABLE;
        }
        if (datetimeFinished == null) {
            Date now = new Date();
            return DurationFormatUtils.formatDuration(now.getTime() - datetimeStarted.getTime(), "HH:mm:ss");
        }
        return DurationFormatUtils.formatDuration(datetimeFinished.getTime() - datetimeStarted.getTime(), "HH:mm:ss");
    }

    /**
     * @return the harvestId
     */
    public Integer getHarvestId() {
        return harvestId;
    }

    /**
     * @param harvestId the harvestId to set
     */
    public void setHarvestId(Integer harvestId) {
        this.harvestId = harvestId;
    }

    /**
     * @return the harvestSourceId
     */
    public Integer getHarvestSourceId() {
        return harvestSourceId;
    }

    /**
     * @param harvestSourceId the harvestSourceId to set
     */
    public void setHarvestSourceId(Integer harvestSourceId) {
        this.harvestSourceId = harvestSourceId;
    }

    /**
     * @return the harvestType
     */
    public String getHarvestType() {
        return harvestType;
    }

    /**
     * @param harvestType the harvestType to set
     */
    public void setHarvestType(String harvestType) {
        this.harvestType = harvestType;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the datetimeStarted
     */
    public java.util.Date getDatetimeStarted() {
        return datetimeStarted;
    }

    /**
     * @param datetimeStarted the datetimeStarted to set
     */
    public void setDatetimeStarted(java.util.Date datetimeStarted) {
        this.datetimeStarted = datetimeStarted;
    }

    /**
     * @return the datetimeFinished
     */
    public java.util.Date getDatetimeFinished() {
        return datetimeFinished;
    }

    /**
     * @param datetimeFinished the datetimeFinished to set
     */
    public void setDatetimeFinished(java.util.Date datetimeFinished) {
        this.datetimeFinished = datetimeFinished;
    }

    /**
     * @return the totalStatements
     */
    public Integer getTotalStatements() {
        return totalStatements;
    }

    /**
     * @param totalStatements the totalStatements to set
     */
    public void setTotalStatements(Integer totalStatements) {
        this.totalStatements = totalStatements;
    }

    /**
     * @return the litObjStatements
     */
    public Integer getLitObjStatements() {
        return litObjStatements;
    }

    /**
     * @param litObjStatements the litObjStatements to set
     */
    public void setLitObjStatements(Integer litObjStatements) {
        this.litObjStatements = litObjStatements;
    }

    /**
     * @return the encodingSchemes
     */
    public Integer getEncodingSchemes() {
        return encodingSchemes;
    }

    /**
     * @param encodingSchemes the encodingSchemes to set
     */
    public void setEncodingSchemes(Integer encodingSchemes) {
        this.encodingSchemes = encodingSchemes;
    }

    /** HTTP Response Code output.
     * @return HTTP Response Code String representation
     */
    public String getResponseCodeString() {
        return (responseCode <= 0 ? "N/A" : String.valueOf(responseCode));
    }

    /**
     * Return the HTTP response code of this harvest.
     * @return
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * Set this harvest's HTTP response code.
     * @param responseCode
     */
    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * Returns true if this is an abandoned harvest, otherwise returns false.
     * @return Abandoned or not.
     */
    public boolean isAbandoned() {
        return BaseHarvest.STATUS_ABANDONED.equals(this.status);
    }
}
