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
package eionet.cr.harvest;

import java.util.HashMap;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public final class CurrentHarvests {

    /** */
    private static Harvest queuedHarvest;
    private static HashMap<String, String> onDemandHarvests;

    /**
     *
     */
    static {
        onDemandHarvests = new HashMap<String, String>();
    }

    /**
     * Hide utility class constructor.
     */
    private CurrentHarvests() {
        // Hide utility class constructor.
    }

    /**
     * @return the queuedHarvest
     */
    public static synchronized Harvest getQueuedHarvest() {
        return queuedHarvest;
    }

    /**
     * @param queuedHarvest the queuedHarvest to set
     */
    public static synchronized void setQueuedHarvest(Harvest queuedHarvest) {
        CurrentHarvests.queuedHarvest = queuedHarvest;
    }

    /**
     *
     * @param url
     * @param user
     */
    public static synchronized void addOnDemandHarvest(String url, String user) {

        if (url != null && user != null) {
            onDemandHarvests.put(url, user);
        }
    }

    /**
     *
     * @param url
     */
    public static synchronized void removeOnDemandHarvest(String url) {
        if (url != null) {
            onDemandHarvests.remove(url);
        }
    }

    /**
     *
     * @param url
     * @return
     */
    public static synchronized boolean contains(String url) {

        if (url == null) {
            return false;
        }

        if (queuedHarvest != null && queuedHarvest.isBeingHarvested(url)) {
            return true;
        }

        if (onDemandHarvests.containsKey(url)) {
            return true;
        }

        return false;
    }
}
