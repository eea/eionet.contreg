package eionet.cr.web.action;

import java.io.IOException;
import java.net.URL;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;

import virtuoso.sesame2.driver.VirtuosoRepository;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;

/**
 *
 */
@UrlBinding("/virtuosoHarvester.action")
public class VirtuosoHarvesterActionBean extends AbstractActionBean {

    /** */
    private String sourceUrl;

    /**
     *
     * @return
     */
    @DefaultHandler
    public Resolution view() {
        return new ForwardResolution("/pages/virtuosoHarvester.jsp");
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution harvest() throws OpenRDFException, IOException {

        String repoUrl = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_URL);
        String repoUsr = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_USR);
        String repoPwd = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_PWD);

        boolean isSuccess = false;
        Repository repository = null;
        RepositoryConnection conn = null;
        try {
            repository = new VirtuosoRepository(repoUrl,repoUsr,repoPwd);
            repository.initialize();
            conn = repository.getConnection();

            // see http://www.openrdf.org/doc/sesame2/users/ch08.html#d0e1218
            // for what's a context
            org.openrdf.model.URI context = repository.getValueFactory().createURI(sourceUrl);

            // start transaction
            conn.setAutoCommit(false);

            // clear previous triples of this context
            conn.clear(context);

            // add the file's contents into repository and under this context
            conn.add(new URL(sourceUrl), sourceUrl, RDFFormat.RDFXML, context);

            // commit transaction
            conn.commit();

            // no transaction rollback needed, when reached this point
            isSuccess = true;
        } finally {
            if (!isSuccess && conn != null) {
                try {conn.rollback();}catch(RepositoryException e) {}
            }

            if (conn != null) {
                try {conn.close();}catch(RepositoryException e) {}
            }

            if (repository != null) {
                try {repository.shutDown();}catch(RepositoryException e) {}
            }
        }

        addSystemMessage("Successfully harvested!");
        return new ForwardResolution("/pages/virtuosoHarvester.jsp");
    }

    /**
     *
     * @return
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     *
     * @param sourceUrl
     */
    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }



}
