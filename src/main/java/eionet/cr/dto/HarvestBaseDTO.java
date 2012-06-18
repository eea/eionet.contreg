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

import eionet.cr.harvest.util.HarvestMessageType;

/**
 * 
 * @author heinljab
 * 
 */
public class HarvestBaseDTO {

    /** */
    private Boolean hasFatals;
    private Boolean hasErrors;
    private Boolean hasWarnings;

    /**
     * @return the hasFatals
     */
    public Boolean getHasFatals() {
        return hasFatals;
    }

    /**
     * @param hasFatals the hasFatals to set
     */
    public void setHasFatals(Boolean hasFatals) {
        this.hasFatals = hasFatals;
    }

    /**
     * @return the hasErrors
     */
    public Boolean getHasErrors() {
        return hasErrors;
    }

    /**
     * @param hasErrors the hasErrors to set
     */
    public void setHasErrors(Boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    /**
     * @return the hasWarnings
     */
    public Boolean getHasWarnings() {
        return hasWarnings;
    }

    /**
     * @param hasWarnings the hasWarnings to set
     */
    public void setHasWarnings(Boolean hasWarnings) {
        this.hasWarnings = hasWarnings;
    }

    /**
     * 
     * @param dto
     * @param messageType
     */
    public static final void addMessageType(HarvestBaseDTO dto, String messageType) {

        if (dto != null && messageType != null) {
            if (messageType.equals(HarvestMessageType.FATAL.toString()))
                dto.setHasFatals(Boolean.TRUE);
            else if (messageType.equals(HarvestMessageType.ERROR.toString()))
                dto.setHasErrors(Boolean.TRUE);
            else if (messageType.equals(HarvestMessageType.WARNING.toString()))
                dto.setHasWarnings(Boolean.TRUE);
        }
    }
}
