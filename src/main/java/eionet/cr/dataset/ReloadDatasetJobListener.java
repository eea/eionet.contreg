/**
 *
 */
package eionet.cr.dataset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 * @author Risto Alt
 *
 */
public class ReloadDatasetJobListener implements JobListener {

    /** */
    private static Log logger = LogFactory.getLog(ReloadDatasetJobListener.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        logger.error("Execution vetoed for job " + context.getJobDetail().getName());
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String datasetUri = dataMap.getString("datasetUri");

        logger.info("Dataset reload started: " + datasetUri);
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException arg1) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String datasetUri = dataMap.getString("datasetUri");

        // Remove the flag that dataset is being reloaded
        CurrentCompiledDatasets.removeCompiledDataset(datasetUri);

        logger.info("Dataset reload finished: " + datasetUri);
    }
}
