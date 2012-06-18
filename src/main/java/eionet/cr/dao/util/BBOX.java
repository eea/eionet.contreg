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
package eionet.cr.dao.util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 * 
 */
public class BBOX {

    /** */
    private Double latitudeSouth;
    private Double latitudeNorth;
    private Double longitudeWest;
    private Double longitudeEast;

    /**
     *
     */
    public BBOX() {
    }

    /**
     * @return the latitudeSouth
     */
    public Double getLatitudeSouth() {
        return latitudeSouth;
    }

    /**
     * @param latitudeSouth the latitudeSouth to set
     */
    public void setLatitudeSouth(Double lowerLat) {
        this.latitudeSouth = lowerLat;
    }

    /**
     * @return the latitudeNorth
     */
    public Double getLatitudeNorth() {
        return latitudeNorth;
    }

    /**
     * @param latitudeNorth the latitudeNorth to set
     */
    public void setLatitudeNorth(Double upperLat) {
        this.latitudeNorth = upperLat;
    }

    /**
     * @return the longitudeWest
     */
    public Double getLongitudeWest() {
        return longitudeWest;
    }

    /**
     * @param longitudeWest the longitudeWest to set
     */
    public void setLongitudeWest(Double lowerLong) {
        this.longitudeWest = lowerLong;
    }

    /**
     * @return the longitudeEast
     */
    public Double getLongitudeEast() {
        return longitudeEast;
    }

    /**
     * @param longitudeEast the longitudeEast to set
     */
    public void setLongitudeEast(Double upperLong) {
        this.longitudeEast = upperLong;
    }

    /**
     * 
     * @return
     */
    public boolean isUndefined() {
        return latitudeSouth == null && latitudeNorth == null && longitudeWest == null && longitudeEast == null;
    }

    /**
     * 
     * @return
     */
    public boolean hasLatitude() {
        return latitudeSouth != null || latitudeNorth != null;
    }

    /**
     * 
     * @return
     */
    public boolean hasLongitude() {
        return longitudeWest != null || longitudeEast != null;
    }
}
