package eionet.cr.dao;

import eionet.cr.dao.virtuoso.VirtuosoDAOFactory;

/**
 *
 * @author jaanus
 *
 */
public class MixedDAOFactory extends DAOFactory {

    /** */
    private static MixedDAOFactory instance;

    /**
     *
     * @return
     */
    public static MixedDAOFactory get() {

        if (instance == null) {
            instance = new MixedDAOFactory();
        }
        return instance;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.DAOFactory#getDao(java.lang.Class)
     */
    @Override
    public <T extends DAO> T getDao(Class<T> implementedInterface) {

        T implementationClass = VirtuosoDAOFactory.get().getDao(implementedInterface);
        return implementationClass;
    }
}
