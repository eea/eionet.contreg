/**
 *
 */
package eionet.cr.dataset;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;

import eionet.cr.common.Predicates;
import eionet.cr.dao.CompiledDatasetDAO;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Util;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.web.security.CRUser;

/**
 * Creates new dataset/file under given user folder. Imports triples from provided URIs/files.
 *
 * @author Risto Alt
 *
 */
public class CreateDataset {

    private String type;
    private CRUser user;

    public CreateDataset(String type, CRUser user) {
        this.type = type;
        this.user = user;
    }

    public void create(String label, String dataset, String folder, List<String> selectedFiles, boolean overwrite,
            String searchCriteria) throws Exception {

        Connection sqlConn = null;
        RepositoryConnection repoConn = null;

        try {
            sqlConn = SesameUtil.getSQLConnection();
            sqlConn.setAutoCommit(false);

            repoConn = SesameUtil.getRepositoryConnection();
            repoConn.setAutoCommit(false);

            if (overwrite) {
                DAOFactory.get().getDao(CompiledDatasetDAO.class).clearDataset(dataset, user.getHomeUri());
            }

            // Store file as new source, but don't harvest it
            addSource(repoConn, sqlConn, dataset);

            // Add metadata
            addMetadata(repoConn, label, dataset, folder, selectedFiles, searchCriteria);

            // Raise the flag that dataset is being compiled
            CurrentLoadedDatasets.addLoadedDataset(dataset, user.getUserName());

            // Start dataset compiling job
            SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
            Scheduler sched = schedFact.getScheduler();
            sched.start();

            JobDetail jobDetail = new JobDetail("LoadTriplesJob-" + System.currentTimeMillis(), null, LoadTriplesJob.class);
            jobDetail.getJobDataMap().put("selectedFiles", selectedFiles);
            jobDetail.getJobDataMap().put("datasetUri", dataset);
            jobDetail.getJobDataMap().put("overwrite", overwrite);

            LoadTriplesJobListener listener = new LoadTriplesJobListener();
            jobDetail.addJobListener(listener.getName());
            sched.addJobListener(listener);

            SimpleTrigger trigger = new SimpleTrigger(jobDetail.getName(), null, new Date(), null, 0, 0L);
            sched.scheduleJob(jobDetail, trigger);

            repoConn.commit();
            sqlConn.commit();
        } catch (Exception e) {
            // Remove the flag that dataset is being compiled
            CurrentLoadedDatasets.removeLoadedDataset(dataset);
            SesameUtil.rollback(repoConn);
            SQLUtil.rollback(sqlConn);
            throw new Exception(e.getMessage(), e);
        } finally {
            SQLUtil.close(sqlConn);
            SesameUtil.close(repoConn);
        }
    }

    private void addMetadata(RepositoryConnection conn, String label, String dataset, String folder, List<String> selectedFiles,
            String searchCriteria) throws DAOException, RepositoryException {

        if (!StringUtils.isBlank(folder)) {
            // prepare cr:hasFile predicate
            ObjectDTO objectDTO = new ObjectDTO(dataset, false);
            objectDTO.setSourceUri(user.getHomeUri());
            SubjectDTO homeSubjectDTO = new SubjectDTO(folder, false);
            homeSubjectDTO.addObject(Predicates.CR_HAS_FILE, objectDTO);
            DAOFactory.get().getDao(HelperDAO.class).addTriples(conn, homeSubjectDTO);
        }

        if (type != null && type.equals(Predicates.CR_COMPILED_DATASET)) {
            if (StringUtils.isNotEmpty(searchCriteria)) {
                // store rdf:searchCriteria predicate
                ObjectDTO criteriaObjectDTO = new ObjectDTO(searchCriteria, true);
                criteriaObjectDTO.setSourceUri(user.getHomeUri());
                SubjectDTO criteriaSubjectDTO = new SubjectDTO(dataset, false);
                criteriaSubjectDTO.addObject(Predicates.CR_SEARCH_CRITERIA, criteriaObjectDTO);
                DAOFactory.get().getDao(HelperDAO.class).addTriples(conn, criteriaSubjectDTO);
            }

            if (!StringUtils.isBlank(folder)) {
                // store rdf:type predicate
                ObjectDTO typeObjectDTO = new ObjectDTO(Predicates.CR_COMPILED_DATASET, false);
                typeObjectDTO.setSourceUri(user.getHomeUri());
                SubjectDTO typeSubjectDTO = new SubjectDTO(dataset, false);
                typeSubjectDTO.addObject(Predicates.RDF_TYPE, typeObjectDTO);
                DAOFactory.get().getDao(HelperDAO.class).addTriples(conn, typeSubjectDTO);
            }

            // Store rdfs:label predicate
            if (StringUtils.isNotEmpty(label)) {
                ObjectDTO labelObjectDTO = new ObjectDTO(label, true);
                labelObjectDTO.setSourceUri(user.getHomeUri());
                SubjectDTO labelSubjectDTO = new SubjectDTO(dataset, false);
                labelSubjectDTO.addObject(Predicates.RDFS_LABEL, labelObjectDTO);
                DAOFactory.get().getDao(HelperDAO.class).addTriples(conn, labelSubjectDTO);
            }

            // store cr:generatedFrom predicates
            for (String file : selectedFiles) {
                ObjectDTO genFromObjectDTO = new ObjectDTO(file, false);
                genFromObjectDTO.setSourceUri(user.getHomeUri());
                SubjectDTO genFromSubjectDTO = new SubjectDTO(dataset, false);
                genFromSubjectDTO.addObject(Predicates.CR_GENERATED_FROM, genFromObjectDTO);
                DAOFactory.get().getDao(HelperDAO.class).addTriples(conn, genFromSubjectDTO);
            }
        }

    }

    private void addSource(RepositoryConnection repConn, Connection conn, String dataset) throws Exception {

        DAOFactory.get().getDao(HarvestSourceDAO.class)
                .addSourceIgnoreDuplicate(conn, HarvestSourceDTO.create(dataset, false, 0, user.getUserName()));

        DAOFactory
                .get()
                .getDao(HarvestSourceDAO.class)
                .insertUpdateSourceMetadata(repConn, dataset, Predicates.CR_LAST_MODIFIED,
                        ObjectDTO.createLiteral(Util.virtuosoDateToString(new Date()), XMLSchema.DATETIME));
    }

}
