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
 * The Original Code is cr3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s): Enriko Käsper
 */

package eionet.cr.dto;


/**
 * Object representing User home folder.
 *
 * @author Enriko Käsper
 */
public class UserFolderDTO {

    /**
     * User files count.
     */
    private int subFilesCount;

    /**
     * User Folders count.
     */
    private int subFoldersCount;

    /**
     * Folder URL.
     */
    private String url;
    /**
     * Folder name.
     */
    private String label;
    /**
     * Parent folder URL.
     */
    private String parentFolderUrl;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public String getParentFolderUrl() {
        return parentFolderUrl;
    }

    public void setParentFolderUrl(String parentFolderUrl) {
        this.parentFolderUrl = parentFolderUrl;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getSubFilesCount() {
        return subFilesCount;
    }

    public int getSubFoldersCount() {
        return subFoldersCount;
    }

    public void setSubFilesCount(int subFilesCount) {
        this.subFilesCount = subFilesCount;
    }

    public void setSubFoldersCount(int subFoldersCount) {
        this.subFoldersCount = subFoldersCount;
    }
}
