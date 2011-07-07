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
package eionet.cr.harvest.persist;

/**
 *
 * Configuration object. Used to configure persister implementation.
 *
 * @author Aleksandr Ivanov <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class PersisterConfig {

    private boolean deriveInferredTriples;
    private boolean clearPreviousContent;
    private long sourceLastModified;
    private String sourceUrl;
    private long genTime;
    private long sourceUrlHash;
    private String instantHarvestUser;

    /**
     * @param deriveInferredTriples
     * @param clearPreviousContent
     * @param sourceLastModified
     * @param sourceUrl
     * @param genTime
     * @param sourceUrlHash
     */
    public PersisterConfig(boolean deriveInferredTriples, boolean clearPreviousContent, long sourceLastModified, String sourceUrl,
            long genTime, long sourceUrlHash, String instantHarvestUser) {
        this.deriveInferredTriples = deriveInferredTriples;
        this.clearPreviousContent = clearPreviousContent;
        this.sourceLastModified = sourceLastModified;
        this.sourceUrl = sourceUrl;
        this.genTime = genTime;
        this.sourceUrlHash = sourceUrlHash;
        this.instantHarvestUser = instantHarvestUser;
    }

    /**
     * @return the deriveInferredTriples
     */
    public boolean isDeriveInferredTriples() {
        return deriveInferredTriples;
    }

    /**
     * @param deriveInferredTriples
     *            the deriveInferredTriples to set
     */
    public void setDeriveInferredTriples(boolean deriveInferredTriples) {
        this.deriveInferredTriples = deriveInferredTriples;
    }

    /**
     * @return the clearPreviousContent
     */
    public boolean isClearPreviousContent() {
        return clearPreviousContent;
    }

    /**
     * @param clearPreviousContent
     *            the clearPreviousContent to set
     */
    public void setClearPreviousContent(boolean clearPreviousContent) {
        this.clearPreviousContent = clearPreviousContent;
    }

    /**
     * @return the sourceLastModified
     */
    public long getSourceLastModified() {
        return sourceLastModified;
    }

    /**
     * @param sourceLastModified
     *            the sourceLastModified to set
     */
    public void setSourceLastModified(long sourceLastModified) {
        this.sourceLastModified = sourceLastModified;
    }

    /**
     * @return the sourceUrl
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * @param sourceUrl
     *            the sourceUrl to set
     */
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    /**
     * @return the genTime
     */
    public long getGenTime() {
        return genTime;
    }

    /**
     * @param genTime
     *            the genTime to set
     */
    public void setGenTime(long genTime) {
        this.genTime = genTime;
    }

    /**
     * @return the sourceUrlHash
     */
    public long getSourceUrlHash() {
        return sourceUrlHash;
    }

    /**
     * @param sourceUrlHash
     *            the sourceUrlHash to set
     */
    public void setSourceUrlHash(long sourceUrlHash) {
        this.sourceUrlHash = sourceUrlHash;
    }

    /**
     * @return the instantHarvestUser
     */
    public String getInstantHarvestUser() {
        return instantHarvestUser;
    }

    /**
     * @param instantHarvestUser
     *            the instantHarvestUser to set
     */
    public void setInstantHarvestUser(String instantHarvestUser) {
        this.instantHarvestUser = instantHarvestUser;
    }
}
