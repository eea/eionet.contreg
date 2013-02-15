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
 *        jaanus
 */

package eionet.cr.dto;

import java.util.Date;

import eionet.cr.util.Hashes;

/**
 * A DTO for a remote SPARQL endpoint harvest query.
 *
 * @author jaanus
 */
public class EndpointHarvestQueryDTO {

    /**  */
    private int id;
    /**  */
    private String title;
    /**  */
    private String query;
    /**  */
    private String endpointUrl;
    /**  */
    private int position;
    /**  */
    private boolean active;
    /**  */
    private Date lastModified;

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
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }
    /**
     * @param query the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }
    /**
     * @return the endpointUrl
     */
    public String getEndpointUrl() {
        return endpointUrl;
    }
    /**
     * @param endpointUrl the endpointUrl to set
     */
    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }
    /**
     * @return the position
     */
    public int getPosition() {
        return position;
    }
    /**
     * @param position the position to set
     */
    public void setPosition(int position) {
        this.position = position;
    }
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
     * Gets the endpoint url hash.
     *
     * @return the endpoint url hash
     */
    public long getEndpointUrlHash() {
        return endpointUrl == null ? 0 : Hashes.spoHash(endpointUrl);
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
}
