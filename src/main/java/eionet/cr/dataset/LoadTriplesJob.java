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

import eionet.cr.dao.CompiledDatasetDAO;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;

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
                DAOFactory.get().getDao(CompiledDatasetDAO.class).saveDataset(selectedFiles, datasetUri, overwrite);
                DAOFactory.get().getDao(HarvestSourceDAO.class).updateHarvestedStatements(datasetUri);
            }
        } catch (DAOException e) {
            logger.info("Error occured while loading triples: " + datasetUri);
            e.printStackTrace();
            // Remove the flag that triples are being loaded
            CurrentLoadedDatasets.removeLoadedDataset(datasetUri);
        }
    }

}
