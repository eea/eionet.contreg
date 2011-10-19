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

/**
 * @author Risto Alt
 *
 */
public class ReloadDatasetJob implements Job {

    /** */
    private static Log logger = LogFactory.getLog(CompileDatasetJob.class);

    public ReloadDatasetJob() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        String datasetUri = dataMap.getString("datasetUri");

        try {
            if (!StringUtils.isBlank(datasetUri)) {
                List<String> datasetFiles = DAOFactory.get().getDao(CompiledDatasetDAO.class).getDatasetFiles(datasetUri);
                if (datasetFiles != null && datasetFiles.size() > 0) {
                    DAOFactory.get().getDao(CompiledDatasetDAO.class).saveDataset(datasetFiles, datasetUri, true);
                }
            }
        } catch (DAOException e) {
            logger.info("Error occured while compiling dataset: " + datasetUri);
            e.printStackTrace();
            // Remove the flag that dataset is being reloaded
            CurrentCompiledDatasets.removeCompiledDataset(datasetUri);
        }
    }

}
