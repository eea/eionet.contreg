/**
 *
 */
package eionet.cr.dataset;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    private static Log logger = LogFactory.getLog(LoadTriplesJob.class);

    public LoadTriplesJob() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        @SuppressWarnings("unchecked")
        List<String> selectedFiles = (List<String>) dataMap.get("selectedFiles");
        String datasetUri = dataMap.getString("datasetUri");
        boolean overwrite = dataMap.getBoolean("overwrite");

        try {
            if (!StringUtils.isBlank(datasetUri) && selectedFiles != null && selectedFiles.size() > 0) {
                // Perform harvest for files that are not yet harvested
                harvestUnharvestedFiles(selectedFiles);

                DAOFactory.get().getDao(CompiledDatasetDAO.class).saveDataset(selectedFiles, datasetUri, overwrite);
                DAOFactory.get().getDao(HarvestSourceDAO.class).updateHarvestedStatementsTriple(datasetUri);
            }
        } catch (Exception e) {
            logger.info("Error occured while loading triples: " + datasetUri);
            e.printStackTrace();
            // Remove the flag that triples are being loaded
            CurrentLoadedDatasets.removeLoadedDataset(datasetUri);
        }
    }

    /**
     * Check if file is already harvested. If not, then harvest.
     * */
    private void harvestUnharvestedFiles(List<String> selectedFiles) throws Exception {

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
                        logger.debug("Creating and storing harvest source");

                        source = new HarvestSourceDTO();
                        source.setUrl(uri);
                        source.setIntervalMinutes(0);

                        dao.addSourceIgnoreDuplicate(source);
                        harvestSourceDTO = dao.getHarvestSourceByUrl(uri);
                    } catch (DAOException e) {
                        logger.info("Exception when trying to create harvest source for the uploaded file content", e);
                    }

                    // perform harvest
                    try {
                        if (harvestSourceDTO != null) {
                            PullHarvest harvest = new PullHarvest(harvestSourceDTO);
                            CurrentHarvests.addOnDemandHarvest(harvestSourceDTO.getUrl(), "harvester");
                            try {
                                harvest.execute();
                            } finally {
                                CurrentHarvests.removeOnDemandHarvest(harvestSourceDTO.getUrl());
                            }
                        } else {
                            logger.debug("Harvest source was not created, so skipping harvest");
                        }
                    } catch (HarvestException e) {
                        logger.info("Exception when trying to harvest file content", e);
                    }
                }
            }
        }
    }

}
