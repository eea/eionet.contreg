package eionet.cr.dao.virtuoso;

import java.util.HashMap;
import java.util.Map;

import eionet.cr.dao.BrowseVoidDatasetsDAO;
import eionet.cr.dao.CompiledDatasetDAO;
import eionet.cr.dao.DAO;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.DeliveryFilterDAO;
import eionet.cr.dao.EndpointHarvestQueryDAO;
import eionet.cr.dao.ExporterDAO;
import eionet.cr.dao.FolderDAO;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestMessageDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.PostHarvestScriptDAO;
import eionet.cr.dao.ReviewsDAO;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dao.SpoBinaryDAO;
import eionet.cr.dao.StagingDatabaseDAO;
import eionet.cr.dao.TagsDAO;
import eionet.cr.dao.UrgentHarvestQueueDAO;
import eionet.cr.dao.UserHomeDAO;

/**
 *
 * @author jaanus
 *
 */
public final class VirtuosoDAOFactory extends DAOFactory {

    /** */
    private static VirtuosoDAOFactory instance;
    private Map<Class<? extends DAO>, Class<? extends VirtuosoBaseDAO>> registeredDaos;

    /**
     *
     */
    private VirtuosoDAOFactory() {
        init();
    }

    /**
     *
     */
    private void init() {

        registeredDaos = new HashMap<Class<? extends DAO>, Class<? extends VirtuosoBaseDAO>>();
        registeredDaos.put(ExporterDAO.class, VirtuosoExporterDAO.class);
        registeredDaos.put(HarvestDAO.class, VirtuosoHarvestDAO.class);
        registeredDaos.put(HarvestSourceDAO.class, VirtuosoHarvestSourceDAO.class);
        registeredDaos.put(HarvestMessageDAO.class, VirtuosoHarvestMessageDAO.class);
        registeredDaos.put(HelperDAO.class, VirtuosoHelperDAO.class);
        registeredDaos.put(SearchDAO.class, VirtuosoSearchDAO.class);
        registeredDaos.put(TagsDAO.class, VirtuosoTagsDAO.class);
        registeredDaos.put(UserHomeDAO.class, VirtuosoUserHomeDAO.class);
        registeredDaos.put(UrgentHarvestQueueDAO.class, VirtuosoUrgentHarvestQueueDAO.class);
        registeredDaos.put(SpoBinaryDAO.class, VirtuosoSpoBinaryDAO.class);
        registeredDaos.put(CompiledDatasetDAO.class, VirtuosoCompiledDatasetDAO.class);
        registeredDaos.put(PostHarvestScriptDAO.class, VirtuosoPostHarvestScriptDAO.class);
        registeredDaos.put(FolderDAO.class, VirtuosoFolderDAO.class);
        registeredDaos.put(ReviewsDAO.class, VirtuosoReviewsDAO.class);
        registeredDaos.put(DeliveryFilterDAO.class, VirtuosoDeliveryFilterDAO.class);
        registeredDaos.put(BrowseVoidDatasetsDAO.class, VirtuosoBrowseVoidDatasetsDAO.class);
        registeredDaos.put(StagingDatabaseDAO.class, VirtuosoStagingDatabaseDAO.class);
        registeredDaos.put(EndpointHarvestQueryDAO.class, VirtuosoEndpointHarvestQueryDAO.class);
    }

    /**
     *
     * @return
     */
    public static VirtuosoDAOFactory get() {
        if (instance == null) {
            instance = new VirtuosoDAOFactory();
        }
        return instance;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.DAOFactory#getDao(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends DAO> T getDao(Class<T> implementedInterface) {

        // due to synchronization problems we have to create DAOs for each method invocation.
        try {
            Class<?> implClass = registeredDaos.get(implementedInterface);
            if (implClass == null) {
                return null;
            } else {
                return (T) implClass.newInstance();
            }
        } catch (Exception fatal) {
            throw new RuntimeException(fatal);
        }
    }
}
