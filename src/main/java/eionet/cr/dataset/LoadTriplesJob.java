/**
 *
 */
package eionet.cr.dataset;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import eionet.cr.common.Predicates;
import eionet.cr.dao.CompiledDatasetDAO;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.CurrentHarvests;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.PullHarvest;
import eionet.cr.util.URLUtil;

/**
 * @author Risto Alt
 *
 */
public class LoadTriplesJob implements Job {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadTriplesJob.class);

    public LoadTriplesJob() {
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        @SuppressWarnings("unchecked")
        List<String> selectedFiles = (List<String>) dataMap.get("selectedFiles");
        String datasetUri = dataMap.getString("datasetUri");
        boolean overwrite = dataMap.getBoolean("overwrite");
        String userName = dataMap.getString("userName");

        try {
            if (!StringUtils.isBlank(datasetUri) && selectedFiles != null && selectedFiles.size() > 0) {
                // Perform harvest for files that are not yet harvested
                harvestUnharvestedFiles(selectedFiles, userName);

                DAOFactory.get().getDao(CompiledDatasetDAO.class).saveDataset(selectedFiles, datasetUri, overwrite);
                DAOFactory.get().getDao(HarvestSourceDAO.class).updateHarvestedStatementsTriple(datasetUri);
            }
        } catch (Exception e) {
            LOGGER.info("Error occured while loading triples: " + datasetUri);
            e.printStackTrace();
            // Remove the flag that triples are being loaded
            CurrentLoadedDatasets.removeLoadedDataset(datasetUri);
        }
    }

    /**
     * Check if file is already harvested. If not, then harvest.
     *
     * @param selectedFiles Files to check/harvest.
     * @param userName Currently acting user (may be null).
     * @throws Exception General exception.
     */
    private void harvestUnharvestedFiles(List<String> selectedFiles, String userName) throws Exception {

        if (selectedFiles != null) {
            for (String fileUri : selectedFiles) {
                String uri = URLUtil.escapeIRI(fileUri);
                boolean triplesExist = false;

                HarvestSourceDTO source = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(uri);
                if (source != null) {
                    String triplesCnt =
                            DAOFactory.get().getDao(HarvestSourceDAO.class)
                                    .getHarvestSourceMetadata(uri, Predicates.CR_HARVESTED_STATEMENTS);
                    if (!StringUtils.isBlank(triplesCnt) && Integer.parseInt(triplesCnt) > 0) {
                        triplesExist = true;
                    }
                }

                if (!triplesExist) {
                    // create and store harvest source
                    HarvestSourceDTO harvestSourceDTO = null;
                    HarvestSourceDAO dao = DAOFactory.get().getDao(HarvestSourceDAO.class);
                    try {
                        LOGGER.debug("Creating and storing harvest source");

                        source = new HarvestSourceDTO();
                        source.setUrl(uri);
                        source.setIntervalMinutes(0);

                        dao.addSourceIgnoreDuplicate(source);
                        harvestSourceDTO = dao.getHarvestSourceByUrl(uri);
                    } catch (DAOException e) {
                        LOGGER.info("Exception when trying to create harvest source for the uploaded file content", e);
                    }

                    // perform harvest
                    try {
                        if (harvestSourceDTO != null) {
                            PullHarvest harvest = new PullHarvest(harvestSourceDTO);
                            CurrentHarvests.addOnDemandHarvest(harvestSourceDTO.getUrl(), userName);
                            try {
                                harvest.execute();
                            } finally {
                                CurrentHarvests.removeOnDemandHarvest(harvestSourceDTO.getUrl());
                            }
                        } else {
                            LOGGER.debug("Harvest source was not created, so skipping harvest");
                        }
                    } catch (HarvestException e) {
                        LOGGER.info("Exception when trying to harvest file content", e);
                    }
                }
            }
        }
    }

}
