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

package eionet.cr.dao.util;

/**
 * Objects of this class represent rows in the "browse VoID datasets" result set, i.e. datasets matching the search criteria.
 *
 * @author jaanus
 */
public class VoidDatasetsResultRow {

    /** */
    private String uri;
    private String label;
    private String creator;
    private String subjects;

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
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
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
     * @return the subjects
     */
    public String getSubjects() {
        return subjects;
    }
    /**
     * @param subjects the subjects to set
     */
    public void setSubjects(String subjects) {
        this.subjects = subjects;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "VoidDatasetsResultRow [uri=" + uri + ", label=" + label + ", creator=" + creator + ", subjects=" + subjects + "]";
    }
}
