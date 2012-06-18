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
package eionet.cr.dto;

import eionet.cr.util.URIUtil;

/**
 * 
 * @author altnyris
 * 
 */
public class DeliveryDTO extends HarvestBaseDTO implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String title;
    private String subjectUri;
    private int fileCnt;
    private String period;
    private String startYear;
    private String endYear;
    private String locality;
    private String date;
    private String coverageNote;

    /**
     *
     */
    public DeliveryDTO() {
    }

    public DeliveryDTO(String subjecUri) {
        this.subjectUri = subjecUri;
        title = URIUtil.extractURILabel(subjecUri, SubjectDTO.NO_LABEL);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getFileCnt() {
        return fileCnt;
    }

    public void setFileCnt(int fileCnt) {
        this.fileCnt = fileCnt;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSubjectUri() {
        return subjectUri;
    }

    public void setSubjectUri(String subjectUri) {
        this.subjectUri = subjectUri;
    }

    public String getStartYear() {
        return startYear;
    }

    public void setStartYear(String startYear) {
        this.startYear = startYear;
    }

    public String getEndYear() {
        return endYear;
    }

    public void setEndYear(String endYear) {
        this.endYear = endYear;
    }

    public String getCoverageNote() {
        return coverageNote;
    }

    public void setCoverageNote(String coverageNote) {
        this.coverageNote = coverageNote;
    }
}
