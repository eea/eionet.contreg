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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Delivery filter.
 * 
 * @author Juhan Voolaid
 */
public class DeliveryFilterDTO {

    /** Id. */
    private Long id;

    /** Obligation. */
    private String obligation;

    /** Obligation label. */
    private String obligationLabel;

    /** Locality. */
    private String locality;

    /** Locality label. */
    private String localityLabel;

    /** Year. */
    private String year;

    /** Username. */
    private String username;

    /**
     * Returns displayable label of the filter.
     * 
     * @return
     */
    public String getLabel() {
        List<String> list = new ArrayList<String>();
        if (StringUtils.isNotEmpty(obligationLabel)) {
            list.add(obligationLabel);
        }
        if (StringUtils.isNotEmpty(localityLabel)) {
            list.add(localityLabel);
        }
        if (StringUtils.isNotEmpty(year)) {
            list.add(year);
        }

        return StringUtils.join(list, ", ");
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the obligation
     */
    public String getObligation() {
        return obligation;
    }

    /**
     * @param obligation the obligation to set
     */
    public void setObligation(String obligation) {
        this.obligation = obligation;
    }

    /**
     * @return the locality
     */
    public String getLocality() {
        return locality;
    }

    /**
     * @param locality the locality to set
     */
    public void setLocality(String locality) {
        this.locality = locality;
    }

    /**
     * @return the year
     */
    public String getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the obligationLabel
     */
    public String getObligationLabel() {
        return obligationLabel;
    }

    /**
     * @param obligationLabel the obligationLabel to set
     */
    public void setObligationLabel(String obligationLabel) {
        this.obligationLabel = obligationLabel;
    }

    /**
     * @return the localityLabel
     */
    public String getLocalityLabel() {
        return localityLabel;
    }

    /**
     * @param localityLabel the localityLabel to set
     */
    public void setLocalityLabel(String localityLabel) {
        this.localityLabel = localityLabel;
    }

}
