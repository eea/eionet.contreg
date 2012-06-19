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
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class UnfinishedHarvestDTO {

    private long source;
    private long genTime;

    /**
     * @return the source
     */
    public long getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(long source) {
        this.source = source;
    }

    /**
     * @return the genTime
     */
    public long getGenTime() {
        return genTime;
    }

    /**
     * @param genTime the genTime to set
     */
    public void setGenTime(long genTime) {
        this.genTime = genTime;
    }

    /**
     *
     * @param source
     * @param genTime
     * @return
     */
    public static UnfinishedHarvestDTO create(long source, long genTime) {
        UnfinishedHarvestDTO dto = new UnfinishedHarvestDTO();
        dto.setSource(source);
        dto.setGenTime(genTime);
        return dto;
    }
}
