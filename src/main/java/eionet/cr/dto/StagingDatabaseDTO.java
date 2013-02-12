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

import org.apache.commons.lang.builder.ToStringBuilder;

import eionet.cr.staging.imp.ImportStatus;

/**
 * The DTO object representing a staging database.
 *
 * @author jaanus
 */
public class StagingDatabaseDTO {

    /** */
    private int id;
    private String name;
    private String creator;
    private Date created;
    private String description;
    private String defaultQuery;
    private ImportStatus importStatus;
    private String importLog;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * @param creator the creator to set
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * @return the created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @param created the created to set
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).toString();
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
     * @return the importStatus
     */
    public ImportStatus getImportStatus() {
        return importStatus;
    }

    /**
     * @param importStatus the importStatus to set
     */
    public void setImportStatus(ImportStatus importStatus) {
        this.importStatus = importStatus;
    }

    /**
     * @return the importLog
     */
    public String getImportLog() {
        return importLog;
    }

    /**
     * @param importLog the importLog to set
     */
    public void setImportLog(String importLog) {
        this.importLog = importLog;
    }

    /**
     * @return the defaultQuery
     */
    public String getDefaultQuery() {
        return defaultQuery;
    }

    /**
     * @param defaultQuery the defaultQuery to set
     */
    public void setDefaultQuery(String defaultQuery) {
        this.defaultQuery = defaultQuery;
    }
}
