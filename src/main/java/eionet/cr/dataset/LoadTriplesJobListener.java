/**
 *
 */
package eionet.cr.dataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 * @author Risto Alt
 */
public class LoadTriplesJobListener implements JobListener {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadTriplesJobListener.class);

    /*
     * (non-Javadoc)
     * @see org.quartz.JobListener#getName()
     */
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /*
     * (non-Javadoc)
     * @see org.quartz.JobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        LOGGER.error("Execution vetoed for job " + context.getJobDetail().getName());
    }

    /*
     * (non-Javadoc)
     * @see org.quartz.JobListener#jobToBeExecuted(org.quartz.JobExecutionContext)
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String datasetUri = dataMap.getString("datasetUri");

        LOGGER.info("Loading triples started: " + datasetUri);
    }

    /*
     * (non-Javadoc)
     * @see org.quartz.JobListener#jobWasExecuted(org.quartz.JobExecutionContext, org.quartz.JobExecutionException)
     */
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException arg1) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String datasetUri = dataMap.getString("datasetUri");

        // Remove the flag that dataset is being reloaded
        CurrentLoadedDatasets.removeLoadedDataset(datasetUri);

        LOGGER.info("Loading triples finished: " + datasetUri);
    }
}
