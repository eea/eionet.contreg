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

import org.apache.commons.lang.StringUtils;
import eionet.cr.common.CRRuntimeException;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class InstantHarvester extends Thread {

    /** */
    public enum Resolution {
        ALREADY_HARVESTING, UNCOMPLETE, COMPLETE, NO_STRUCTURED_DATA, SOURCE_UNAVAILABLE, RECENTLY_HARVESTED;
    }

    /** */
    private String sourceUrl;
    private String userName;

    /** */
    private HarvestException harvestException = null;

    /** */
    private boolean rdfContentFound = false;
    private boolean sourceAvailable = false;
    private boolean needsHarvesting = true;

    private InstantHarvester(String sourceUrl, String userName) {

        this.sourceUrl = sourceUrl;
        this.userName = userName;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Thread#run()
     */
    public void run() {

        InstantHarvest instantHarvest = null;
        try {
            instantHarvest = InstantHarvest.createFullSetup(sourceUrl, userName);
            instantHarvest.execute();
        } catch (HarvestException e) {
            harvestException = e;
        } catch (Throwable e) {
            if (e instanceof HarvestException) {
                harvestException = (HarvestException) e;
            } else {
                harvestException = new HarvestException(e.toString(), e);
            }
        } finally {
            if (instantHarvest != null) {
                rdfContentFound = instantHarvest.isRdfContentFound();
                sourceAvailable = instantHarvest.getSourceAvailable() != null
                        && instantHarvest.getSourceAvailable().booleanValue();
                // Check if source is redirected and destination
                // source has been recently updated
                needsHarvesting = instantHarvest.needsHarvesting;
            }
            CurrentHarvests.removeOnDemandHarvest(sourceUrl);
        }
    }

    /**
     *
     * @return
     */
    private boolean wasException() {
        return harvestException != null;
    }

    /**
     *
     * @param sourceUrl
     * @param userName
     * @return Resolution
     * @throws HarvestException
     */
    public static Resolution harvest(String sourceUrl, String userName) throws HarvestException {

        if (StringUtils.isBlank(sourceUrl))
            throw new IllegalArgumentException("Source URL must not be null or blank!");
        else if (StringUtils.isBlank(userName))
            throw new IllegalArgumentException("Instant harvest user name must not be null or blank!");

        if (CurrentHarvests.contains(sourceUrl)) {
            return Resolution.ALREADY_HARVESTING;
        }

        CurrentHarvests.addOnDemandHarvest(sourceUrl, userName);

        InstantHarvester instantHarvester = null;
        try {
            instantHarvester = new InstantHarvester(sourceUrl, userName);
            instantHarvester.start();

            for (int loopCount = 0; instantHarvester.isAlive() && loopCount < 15; loopCount++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new CRRuntimeException(e.toString(), e);
                }
            }

            if (!instantHarvester.isAlive() && instantHarvester.wasException()) {
                throw instantHarvester.getHarvestException();
            } else {
                if (instantHarvester.isAlive()) {
                    return Resolution.UNCOMPLETE;
                } else {
                    if (!instantHarvester.isSourceAvailable())
                        return Resolution.SOURCE_UNAVAILABLE;
                    else if (!instantHarvester.isRdfContentFound())
                        return Resolution.NO_STRUCTURED_DATA;
                    else if (!instantHarvester.isNeedsHarvesting())
                        return Resolution.RECENTLY_HARVESTED;
                    else
                        return Resolution.COMPLETE;
                }
            }
        } finally {
            // if the instant harvester was never constructed or it isn't alive
            // any more, make sure the current-harvest-source-url is nullified
            if (instantHarvester == null || !instantHarvester.isAlive()) {
                CurrentHarvests.removeOnDemandHarvest(sourceUrl);
            }
        }
    }

    /**
     * @return the harvestException
     */
    public HarvestException getHarvestException() {
        return harvestException;
    }

    /**
     * @return the rdfContentFound
     */
    public boolean isRdfContentFound() {
        return rdfContentFound;
    }

    /**
     * @return the sourceAvailable
     */
    public boolean isSourceAvailable() {
        return sourceAvailable;
    }

    /**
     * @return the needsHarvesting
     */
    public boolean isNeedsHarvesting() {
        return needsHarvesting;
    }
}
