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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import eionet.cr.harvest.scheduled.HarvestingJob;

/**
 * A global utility class for keeping record of currently on-going harvests.
 *
 * @author <a href="mailto:jaanus.heinlaid@gmail.com">Jaanus Heinlaid</a>
 */
public final class CurrentHarvests {

    /** Harvest currently executed by {@link HarvestingJob}. This can execute only harvest at a time. */
    private static Harvest queuedHarvest;

    /** Map of on-going on-demand harvests. Key is source URL, value is harvesting user. */
    private static LinkedHashMap<String, String> onDemandHarvests = new LinkedHashMap<String, String>();

    /**
     * Hide utility class constructor.
     */
    private CurrentHarvests() {
        // Hide utility class constructor.
    }

    /**
     * Returns the harvest currently executed by {@link HarvestingJob}. This can execute only harvest at a time.
     *
     * @return the queuedHarvest
     */
    public static synchronized Harvest getQueuedHarvest() {
        return queuedHarvest;
    }

    /**
     * Sets the harvest currently executed by {@link HarvestingJob}. This can execute only harvest at a time.
     * @param queuedHarvest the queuedHarvest to set
     */
    public static synchronized void setQueuedHarvest(Harvest queuedHarvest) {
        CurrentHarvests.queuedHarvest = queuedHarvest;
    }

    /**
     * Register this URL-user pair as a currently on-going on-demand harvest.
     *
     * @param url The URL of the harvested source.
     * @param user The harvesting user.
     */
    public static synchronized void addOnDemandHarvest(String url, String user) {

        if (url != null && user != null) {
            onDemandHarvests.put(url, user);
        }
    }

    /**
     * Remove given URL from the currently on-going on-demand harvests.
     *
     * @param url The URL.
     */
    public static synchronized void removeOnDemandHarvest(String url) {
        if (url != null) {
            onDemandHarvests.remove(url);
        }
    }

    /**
     * Returns true if the given URL is currently being harvested, whether by {@link HarvestingJob} or on-demand.
     * Otherwise returns false.
     *
     * @param url The URL to check.
     * @return The flag as indicated above.
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

    /**
     * Returns an unmodifiable copy of the map of all currently on-going on-demand harvests.
     *
     * @return The map as indicated above. Key represents a harvested source URL, values represents the harvesting user.
     */
    public static synchronized Map<String, String> getOnDemandHarvests() {
        return Collections.unmodifiableMap(onDemandHarvests);
    }
}
