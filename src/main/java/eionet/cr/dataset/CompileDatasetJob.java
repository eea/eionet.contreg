/**
 * 
 */
package eionet.cr.dataset;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import eionet.cr.dao.CompiledDatasetDAO;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;

/**
 * @author Risto Alt
 *
 */
public class CompileDatasetJob implements Job {

    /** */
    private static Log logger = LogFactory.getLog(CompileDatasetJob.class);

    public CompileDatasetJob() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        @SuppressWarnings("unchecked")
        List<String> selectedFiles = (List<String>) dataMap.get("selectedFiles");
        String datasetUri = dataMap.getString("datasetUri");
        boolean overwrite = dataMap.getBoolean("overwrite");

        try {
            DAOFactory.get().getDao(CompiledDatasetDAO.class).saveDataset(selectedFiles, datasetUri, overwrite);
        } catch (DAOException e) {
            logger.info("Error occured while compiling dataset: " + datasetUri);
            e.printStackTrace();
            // Remove the flag that dataset is being compiled
            CurrentCompiledDatasets.removeCompiledDataset(datasetUri);
        }
    }

}
