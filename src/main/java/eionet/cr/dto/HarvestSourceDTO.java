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
/**
 *
 */
package eionet.cr.dto;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * @author altnyris
 *
 */
public class HarvestSourceDTO implements Serializable, Cloneable {

    /**
     * serial.
     */
    private static final long serialVersionUID = 1L;
    /** */
    public static final int COUNT_UNAVAIL_THRESHOLD = 5;
    public static final int DEFAULT_REFERRALS_INTERVAL = 60480;

    /** */
    private Integer sourceId;
    private String url;
    private String emails;
    private Date timeCreated;
    private Integer statements;
    private Integer countUnavail;
    private Date lastHarvest;
    private boolean lastHarvestFailed;
    private Integer intervalMinutes;
    private Long urlHash;
    private boolean prioritySource;
    private String owner;
    private boolean permanentError;
    private String mediaType;
    private Integer lastHarvestId;
    private boolean isSparqlEndpoint;

    /**
     *
     */
    public HarvestSourceDTO() {
    }

    /**
     * @return the sourceId
     */
    public Integer getSourceId() {
        return sourceId;
    }

    /**
     * @param sourceId
     *            the sourceId to set
     */
    public void setSourceId(Integer sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url
     *            the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the emails
     */
    public String getEmails() {
        return emails;
    }

    /**
     * @param emails
     *            the emails to set
     */
    public void setEmails(String emails) {
        this.emails = emails;
    }

    /**
     * @return the timeCreated
     */
    public Date getTimeCreated() {
        return timeCreated;
    }

    /**
     * @param timeCreated
     *            the timeCreated to set
     */
    public void setTimeCreated(Date timeCreated) {
        this.timeCreated = timeCreated;
    }

    /**
     * @return the statements
     */
    public Integer getStatements() {
        return statements;
    }

    /**
     * @param statements
     *            the statements to set
     */
    public void setStatements(Integer statements) {
        this.statements = statements;
    }

    /**
     * @return the countUnavail
     */
    public Integer getCountUnavail() {
        return countUnavail;
    }

    /**
     * @param countUnavail
     *            the countUnavail to set
     */
    public void setCountUnavail(Integer countUnavail) {
        this.countUnavail = countUnavail;
    }

    /**
     *
     * @return boolean
     */
    public boolean isUnavailable() {

        return countUnavail != null && countUnavail.intValue() >= COUNT_UNAVAIL_THRESHOLD;
    }

    /**
     * @return Integer
     */
    public Integer getIntervalMinutes() {
        return intervalMinutes;
    }

    /**
     * @param intervalMinutes
     */
    public void setIntervalMinutes(Integer intervalMinutes) {
        this.intervalMinutes = intervalMinutes;
    }

    /**
     * @return the lastHarvest
     */
    public Date getLastHarvest() {
        return lastHarvest;
    }

    /**
     * @param lastHarvest
     *            the lastHarvest to set
     */
    public void setLastHarvest(Date lastHarvest) {
        this.lastHarvest = lastHarvest;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new StringBuffer().append("Harvest source ").append(url).toString();
    }

    /**
     * @return the lastHarvestFailed
     */
    public boolean isLastHarvestFailed() {
        return lastHarvestFailed;
    }

    /**
     * @param lastHarvestFailed
     *            the lastHarvestFailed to set
     */
    public void setLastHarvestFailed(boolean lastHarvestFailed) {
        this.lastHarvestFailed = lastHarvestFailed;
    }

    /**
     * @return the urlHash
     */
    public Long getUrlHash() {
        return urlHash;
    }

    /**
     * @param urlHash
     *            the urlHash to set
     */
    public void setUrlHash(Long urlHash) {
        this.urlHash = urlHash;
    }

    public boolean isPrioritySource() {
        return prioritySource;
    }

    public void setPrioritySource(boolean prioritySource) {
        this.prioritySource = prioritySource;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isPermanentError() {
        return permanentError;
    }

    public void setPermanentError(boolean permanentError) {
        this.permanentError = permanentError;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * @return the lastHarvestId
     */
    public Integer getLastHarvestId() {
        return lastHarvestId;
    }

    /**
     * @param lastHarvestId
     *            the lastHarvestId to set
     */
    public void setLastHarvestId(Integer lastHarvestId) {
        this.lastHarvestId = lastHarvestId;
    }

    /**
     *
     * @param url
     * @param prioritySource
     * @param intervalMinutes
     * @return
     */
    public static HarvestSourceDTO create(String url, boolean prioritySource, int intervalMinutes, String owner) {

        HarvestSourceDTO result = new HarvestSourceDTO();
        result.setUrl(url);
        result.setPrioritySource(prioritySource);
        result.setIntervalMinutes(intervalMinutes);
        result.setOwner(owner);
        return result;
    }

    /**
     *
     * @return
     */
    public double getHarvestUrgencyScore() {

        // if harvest interval is set to 0, then so is its urgency score
        if (intervalMinutes == null || intervalMinutes.intValue() <= 0) {
            return 0.0d;
        }

        // urgency score can only be calculated if at least the last harvest
        // or creation time is known (and interval is >0, as already assured above)
        if (lastHarvest == null && timeCreated == null) {
            return 0.0d;
        }

        Date lastTime = lastHarvest == null ? null : new Date(lastHarvest.getTime());
        if (lastTime == null) {

            // if last time is not known, then last time to pseudo-value
            // which is (creation time - harvest interval)

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(timeCreated);
            calendar.add(Calendar.SECOND, -1 * intervalMinutes.intValue() * 60);
            lastTime = calendar.getTime();
        }

        long secondsSinceLastTime = (System.currentTimeMillis() / 1000L) - (lastTime.getTime() / 1000L);
        long intervalSeconds = intervalMinutes.longValue() * 60L;
        return ((double) secondsSinceLastTime) / ((double) intervalSeconds);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public HarvestSourceDTO clone() {
        try {
            return (HarvestSourceDTO) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported");
        }
    }

    /**
     * @return the isSparqlEndpoint
     */
    public boolean isSparqlEndpoint() {
        return isSparqlEndpoint;
    }

    /**
     * @param isSparqlEndpoint the isSparqlEndpoint to set
     */
    public void setSparqlEndpoint(boolean isSparqlEndpoint) {
        this.isSparqlEndpoint = isSparqlEndpoint;
    }
}
