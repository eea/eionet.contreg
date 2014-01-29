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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Folder item DTO.
 *
 * @author Juhan Voolaid
 */
public class FolderItemDTO implements Comparable<FolderItemDTO> {

    /**
     * The Enum for folder item type.
     */
    public enum Type {

        /** The reserved folder. */
        RESERVED_FOLDER(1),
        /** The folder. */
        FOLDER(2),
        /** The reserved file. */
        RESERVED_FILE(3),
        /** The file. */
        FILE(4);

        /** The order. */
        int order;

        /**
         * Instantiates a new type.
         *
         * @param order the order
         */
        Type(int order) {
            this.order = order;
        }

        /**
         * Gets the order.
         *
         * @return the order
         */
        public int getOrder() {
            return order;
        }
    }

    /** Uri. */
    private String uri;

    /** Title or label that is set for the file or folder. */
    private String title;

    /** Folder or file name that is parsed from uri. */
    private String name;

    /** Type. */
    private Type type;

    /** Last modified. */
    private String lastModified;

    /**
     * True, if type is reserved folder.
     *
     * @return
     */
    public boolean isReservedFolder() {
        return Type.RESERVED_FOLDER.equals(type);
    }

    /**
     * True, if type is folder.
     *
     * @return
     */
    public boolean isFolder() {
        return Type.FOLDER.equals(type);
    }

    /**
     * True, if type is file.
     *
     * @return
     */
    public boolean isFile() {
        return Type.FILE.equals(type);
    }

    /**
     * True, if type is reserved file.
     *
     * @return
     */
    public boolean isReservedFile() {
        return Type.RESERVED_FILE.equals(type);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(FolderItemDTO o) {
        if (o.getType().equals(type)) {
            return uri.compareTo(o.getUri());
        }
        return type.getOrder() - o.getType().getOrder();
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
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
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Type type) {
        this.type = type;
    }

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
     * @return the lastModified
     */
    public String getLastModified() {
        return lastModified;
    }

    /**
     * @param lastModified the lastModified to set
     */
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Returns name that is urlDecoded.
     *
     * @return
     */
    public String getUrlDecodedName() {

        try {
            return URLDecoder.decode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";
    }

}
