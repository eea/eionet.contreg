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

/**
 *
 * @author heinljab
 *
 */
public class HarvestMessageDTO implements java.io.Serializable{

    /** */
    Integer harvestId = null;
    String type = null;
    String message = null;
    String stackTrace = null;
    Integer harvestMessageId = null;

    /**
     *
     */
    public HarvestMessageDTO(){
    }

    /**
     * @return the harvestID
     */
    public Integer getHarvestId() {
        return harvestId;
    }
    /**
     * @param harvestID the harvestID to set
     */
    public void setHarvestId(Integer harvestID) {
        this.harvestId = harvestID;
    }
    /**
     * @return the type
     */
    public String getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
    /**
     * @return the stackTrace
     */
    public String getStackTrace() {
        return stackTrace;
    }
    /**
     * @param stackTrace the stackTrace to set
     */
    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public Integer getHarvestMessageId() {
        return harvestMessageId;
    }

    public void setHarvestMessageId(Integer harvestMessageId) {
        this.harvestMessageId = harvestMessageId;
    }
}
