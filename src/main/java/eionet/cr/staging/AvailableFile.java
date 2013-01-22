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

package eionet.cr.staging;

import java.io.File;
import java.util.Date;


/**
 * A bean that represents a file available for creating a staging database.
 *
 * @author jaanus
 */
public class AvailableFile {

    /** */
    private String name;

    /** */
    private long size;

    /** */
    private Date lastModified;

    /**
     *
     * @param file
     * @return
     */
    public static AvailableFile create(File file) {

        AvailableFile availableFile = new AvailableFile();
        availableFile.setName(file.getName());
        availableFile.setSize(file.length());
        availableFile.setLastModified(file.lastModified());
        return availableFile;
    }

    /**
     * @param name the name to set
     */
    public void setName(String fileName) {
        this.name = fileName;
    }

    /**
     * @param size the size to set
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the size
     */
    public long getSize() {
        return size;
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
    public void setLastModified(long lastModified) {
        this.lastModified = new Date(lastModified);
    }
}
