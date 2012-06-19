/*
 * The contents of this file are subject to the Mozilla Public
 *
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
 * Agency. Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency. All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.dto;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class UploadDTO {

    /** */
    private String subjectUri;
    private String label;
    private String dateModified;
    private String triples;

    /**
     *
     * @param subjectUri
     */
    public UploadDTO(String subjectUri) {

        if (subjectUri == null || subjectUri.trim().length() == 0) {
            throw new IllegalArgumentException("Subject uri must not be null");
        }
        this.subjectUri = subjectUri;
    }

    /**
     * @return the subjectUri
     */
    public String getSubjectUri() {
        return subjectUri;
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
     * @return the dateModified
     */
    public String getDateModified() {
        return dateModified;
    }

    /**
     * @param dateModified the dateModified to set
     */
    public void setDateModified(String dateModified) {
        this.dateModified = dateModified;
    }

    /**
     * @return the triples
     */
    public String getTriples() {
        return triples;
    }

    /**
     * @param triples the triples to set
     */
    public void setTriples(String triples) {
        this.triples = triples;
    }

}
