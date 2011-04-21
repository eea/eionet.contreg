package eionet.cr.dao;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.mysql.MySQLDAOFactory;
import eionet.cr.dao.postgre.PostgreSQLDAOFactory;
import eionet.cr.dao.virtuoso.VirtuosoDAOFactory;
import eionet.cr.util.sql.DbConnectionProvider;

/**
 *
 * @author jaanus
 *
 */
public class MixedDAOFactory extends DAOFactory{

    /** */
    private static MixedDAOFactory instance;

    /**
     *
     * @return
     */
    public static MixedDAOFactory get() {

        if(instance == null) {
            instance = new MixedDAOFactory();
        }
        return instance;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.DAOFactory#getDao(java.lang.Class)
     */
    @Override
    public <T extends DAO> T getDao(Class<T> implementedInterface) {

        T implementationClass = VirtuosoDAOFactory.get().getDao(implementedInterface);
        if (GeneralConfig.useVirtuoso() == false || implementationClass == null) {

            String dbUrl = DbConnectionProvider.getConnectionUrl();
            if (dbUrl.startsWith("jdbc:mysql:"))
                return implementationClass = MySQLDAOFactory.get().getDao(implementedInterface);
            else if (dbUrl.startsWith("jdbc:postgresql:"))
                return implementationClass = PostgreSQLDAOFactory.get().getDao(implementedInterface);
        }

        return implementationClass;
    }
}
