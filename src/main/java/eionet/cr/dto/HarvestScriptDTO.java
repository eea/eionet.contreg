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
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */

package eionet.cr.dto;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Jaanus Heinlaid
 */
public class HarvestScriptDTO {

    /** Default value for {@link HarvestScriptDTO.Phase}. */
    public static final Phase DEFAULT_PHASE = Phase.AFTER_NEW;

    /** */
    public enum TargetType {
        SOURCE, TYPE
    };

    /** */
    private TargetType targetType;
    private String targetUrl;
    private String title;
    private String script;
    private int position;
    private boolean active;
    private boolean runOnce = true;
    private int id;
    private Date lastModified;

    /** Harvest phase where the script should be run in. */
    private Phase phase;

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the targetType
     */
    public TargetType getTargetType() {
        return targetType;
    }

    /**
     * @return the targetUrl
     */
    public String getTargetUrl() {
        return targetUrl;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the script
     */
    public String getScript() {
        return script;
    }

    /**
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @param targetType the targetType to set
     */
    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }

    /**
     * @param targetUrl the targetUrl to set
     */
    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param script the script to set
     */
    public void setScript(String script) {
        this.script = script;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * @return the runOnce
     */
    public boolean isRunOnce() {
        return runOnce;
    }

    /**
     * @param runOnce the runOnce to set
     */
    public void setRunOnce(boolean runOnce) {
        this.runOnce = runOnce;
    }

    /**
     * @return the lastModified
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * @param lastModified the lastModified to set
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return title;
    }

    /**
     * @return the phase
     */
    public Phase getPhase() {
        return phase;
    }

    /**
     * @param phase the phase to set
     */
    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    /** Enum for possible values of the harvest phase where the script should be run in. */
    public enum Phase {

        /** The after-new phase, i.e. after the source's new content has been loaded into the triple store. */
        AFTER_NEW("After harvesting new content", "After new"),

        /** The pre-purge phase, i.e. before the source's old content is to be cleared in the triple store. */
        PRE_PURGE("Before purging old content", "Pre-purge");

        /** The enum's human friendly label. */
        private String label;

        /** The enum's human friendly shortLabel.*/
        private String shortLabel;

        /**
         * Enum constructor.
         * @param label
         */
        Phase(String label, String acronym) {
            this.label = label;
            this.shortLabel = acronym;
        }

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * Returns {@link Enum#name()} of this enum.
         * @return the name
         */
        public String getName() {
            return name();
        }

        /**
         * @return the shortLabel
         */
        public String getShortLabel() {
            return StringUtils.isBlank(shortLabel) ? name() : shortLabel;
        }
    };
}
