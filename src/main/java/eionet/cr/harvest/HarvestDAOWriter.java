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
package eionet.cr.harvest;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import org.openrdf.repository.RepositoryException;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestMessageDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.util.HarvestMessageType;
import eionet.cr.util.Util;

/**
 *
 * @author heinljab
 *
 */
public class HarvestDAOWriter {

    /** */
    private int sourceId;
    private String harvestType;
    private String userName;

    /** */
    private int harvestId;

    /**
     * @param sourceId
     * @param harvestType
     * @param userName
     */
    public HarvestDAOWriter(int sourceId, String harvestType, String userName) {

        this.sourceId = sourceId;
        this.harvestType = harvestType;
        this.userName = userName;
    }

    /**
     *
     * @throws DAOException
     */
    protected void writeStarted(Harvest harvest) throws DAOException {

        harvestId =
                DAOFactory.get().getDao(HarvestDAO.class)
                        .insertStartedHarvest(sourceId, harvestType, userName, Harvest.STATUS_STARTED);
    }

    /**
     *
     * @param harvest
     * @throws DAOException
     */
    protected void writeFinished(Harvest harvest) throws DAOException, RepositoryException {

        if (!(harvest instanceof PullHarvest) || (harvest instanceof PullHarvest && harvest.needsHarvesting)) {
            DAOFactory.get().getDao(HarvestDAO.class)
                    .updateFinishedHarvest(harvestId, Harvest.STATUS_FINISHED, harvest.getStoredTriplesCount(), 0, 0);
        }

        // harvest failed if it has a fatal error || warnings || errors
        boolean failed =
                harvest.getFatalError() != null || (harvest.getErrors() != null && !harvest.getErrors().isEmpty())
                        || (harvest.getWarnings() != null && !harvest.getWarnings().isEmpty());

        Timestamp lastHarvest = generateLastHarvestDate(harvest);

        if (harvest instanceof PullHarvest) {
            if (harvest.needsHarvesting) {
                if (harvest.isRedirectedSource) {
                    sourceId = harvest.finalSourceId;
                }

                DAOFactory
                        .get()
                        .getDao(HarvestSourceDAO.class)
                        .updateHarvestFinished(sourceId, harvest.getStoredTriplesCount(),
                                ((PullHarvest) harvest).getSourceAvailable(), failed, harvest.permanentError, lastHarvest);
            }
        } else {
            DAOFactory.get().getDao(HarvestSourceDAO.class)
                    .updateHarvestFinished(sourceId, null, null, failed, false, lastHarvest);
        }
    }

    /**
     * @param harvest
     * @return Timestamp
     * @throws DAOException
     */
    private Timestamp generateLastHarvestDate(Harvest harvest) throws DAOException {

        long result = System.currentTimeMillis();

        // If there was a "temporary" harvesting error (i.e. source was not available, but
        // the harvest's permanent error flag is down), the last harvest time is not actually
        // set to current time, but instead it is increased with 10% of the harvesting period
        // but minimum two hours. This is relevant only for PullHarvest.

        if (harvest instanceof PullHarvest) {

            PullHarvest pullHarvest = (PullHarvest) harvest;
            if (pullHarvest != null){
                Boolean isSourceAvailable = pullHarvest.getSourceAvailable();
                boolean wasTemporaryError = (isSourceAvailable!=null && isSourceAvailable.booleanValue()==false) && !pullHarvest.permanentError;
                if (wasTemporaryError) {

                    HarvestSourceDTO source = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceById(sourceId);
                    if (source != null && source.getLastHarvest() != null) {

                        long prevHarvest = source.getLastHarvest().getTime();
                        int interval = source.getIntervalMinutes();

                        int increase = (interval * 10) / 100;
                        if (increase < 120) {
                            increase = 120;
                        }

                        result = prevHarvest + (increase * 60 * 1000);
                    }
                }
            }
        }

        return new Timestamp(result);
    }

    /**
     *
     * @param harvest
     * @throws DAOException
     */
    protected void writeMessages(Harvest harvest) throws DAOException {

        // save the fatal exception
        writeThrowable(harvest.getFatalError(), HarvestMessageType.FATAL.toString());

        // save errors and warnings
        writeThrowables(harvest.getErrors(), HarvestMessageType.ERROR.toString());
        writeThrowables(harvest.getWarnings(), HarvestMessageType.WARNING.toString());

        // save infos
        List<String> infos = harvest.getInfos();
        if (infos != null && !infos.isEmpty()) {
            for (Iterator<String> i = infos.iterator(); i.hasNext();) {

                HarvestMessageDTO harvestMessageDTO = new HarvestMessageDTO();
                harvestMessageDTO.setHarvestId(new Integer(harvestId));
                harvestMessageDTO.setType(HarvestMessageType.INFO.toString());
                harvestMessageDTO.setMessage(i.next());
                harvestMessageDTO.setStackTrace("");
                DAOFactory.get().getDao(HarvestMessageDAO.class).insertHarvestMessage(harvestMessageDTO);
            }
        }
    }

    /**
     *
     * @param throwable
     * @param type
     * @throws DAOException
     */
    protected void writeThrowable(Throwable throwable, String type) throws DAOException {

        if (throwable == null)
            return;

        HarvestMessageDTO harvestMessageDTO = new HarvestMessageDTO();
        harvestMessageDTO.setHarvestId(new Integer(this.harvestId));
        harvestMessageDTO.setType(type);
        harvestMessageDTO.setMessage(throwable.toString());
        harvestMessageDTO.setStackTrace(Util.getStackTrace(throwable));
        DAOFactory.get().getDao(HarvestMessageDAO.class).insertHarvestMessage(harvestMessageDTO);
    }

    /**
     *
     * @param throwables
     * @param type
     * @throws DAOException
     */
    protected void writeThrowables(List<Throwable> throwables, String type) throws DAOException {

        if (throwables == null)
            return;

        for (int i = 0; i < throwables.size(); i++) {
            writeThrowable(throwables.get(i), type);
        }
    }

    public int getHarvestId() {
        return harvestId;
    }

    public void setHarvestId(int harvestId) {
        this.harvestId = harvestId;
    }
}
