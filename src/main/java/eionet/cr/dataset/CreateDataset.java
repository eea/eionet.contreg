/**
 *
 */
package eionet.cr.dataset;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.vocabulary.XMLSchema;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.web.security.CRUser;

/**
 * Creates new dataset/file under given user folder. Imports triples from provided URIs/files.
 *
 * @author Risto Alt
 *
 */
public class CreateDataset {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private String type;
    private CRUser user;

    public CreateDataset(String type, CRUser user) {
        this.type = type;
        this.user = user;
    }

    public void create(String dataset, String folder, List<String> selectedFiles, boolean overwrite) throws Exception {

        try {
            // Store file as new source, but don't harvest it
            addSource(dataset);

            // Add metadata
            addMetadata(dataset, folder, selectedFiles);

            // Raise the flag that dataset is being compiled
            CurrentLoadedDatasets.addLoadedDataset(dataset, user.getUserName());

            // Start dataset compiling job
            SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
            Scheduler sched = schedFact.getScheduler();
            sched.start();

            JobDetail jobDetail = new JobDetail("LoadTriplesJob", null, LoadTriplesJob.class);
            jobDetail.getJobDataMap().put("selectedFiles", selectedFiles);
            jobDetail.getJobDataMap().put("datasetUri", dataset);
            jobDetail.getJobDataMap().put("overwrite", overwrite);

            LoadTriplesJobListener listener = new LoadTriplesJobListener();
            jobDetail.addJobListener(listener.getName());
            sched.addJobListener(listener);

            SimpleTrigger trigger = new SimpleTrigger(jobDetail.getName(), null, new Date(), null, 0, 0L);
            sched.scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            e.printStackTrace();
            // Remove the flag that dataset is being compiled
            CurrentLoadedDatasets.removeLoadedDataset(dataset);
            throw new Exception(e.getMessage(), e);
        }
    }

    private void addMetadata(String dataset, String folder, List<String> selectedFiles) {

        try {
            if (!StringUtils.isBlank(folder)) {
                // prepare cr:hasFile predicate
                ObjectDTO objectDTO = new ObjectDTO(dataset, false);
                objectDTO.setSourceUri(user.getHomeUri());
                SubjectDTO homeSubjectDTO = new SubjectDTO(folder, false);
                homeSubjectDTO.addObject(Predicates.CR_HAS_FILE, objectDTO);
                DAOFactory.get().getDao(HelperDAO.class).addTriples(homeSubjectDTO);
            }

            if (type != null && type.equals(Predicates.CR_COMPILED_DATASET)) {
                if (!StringUtils.isBlank(folder)) {
                    // store rdf:type predicate
                    ObjectDTO typeObjectDTO = new ObjectDTO(Predicates.CR_COMPILED_DATASET, false);
                    typeObjectDTO.setSourceUri(user.getHomeUri());
                    SubjectDTO typeSubjectDTO = new SubjectDTO(dataset, false);
                    typeSubjectDTO.addObject(Predicates.RDF_TYPE, typeObjectDTO);
                    DAOFactory.get().getDao(HelperDAO.class).addTriples(typeSubjectDTO);
                }

                // store cr:generatedFrom predicates
                for (String file : selectedFiles) {
                    ObjectDTO genFromObjectDTO = new ObjectDTO(file, false);
                    genFromObjectDTO.setSourceUri(user.getHomeUri());
                    SubjectDTO genFromSubjectDTO = new SubjectDTO(dataset, false);
                    genFromSubjectDTO.addObject(Predicates.CR_GENERATED_FROM, genFromObjectDTO);
                    DAOFactory.get().getDao(HelperDAO.class).addTriples(genFromSubjectDTO);
                }
            }

        } catch (DAOException e) {
            e.printStackTrace();
            return;
        }
    }

    private void addSource(String dataset) throws Exception {

        DAOFactory.get().getDao(HarvestSourceDAO.class)
        .addSourceIgnoreDuplicate(HarvestSourceDTO.create(dataset, false, 0, user.getUserName()));

        DAOFactory.get().getDao(HarvestSourceDAO.class)
        .insertUpdateSourceMetadata(dataset, Predicates.CR_LAST_MODIFIED,
                ObjectDTO.createLiteral(dateFormat.format(new Date()), XMLSchema.DATETIME));
    }

}
