package eionet.cr.dao.virtuoso.helpers;

import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 *
 * @author jaanus
 *
 */
public class ResourceRenameHandler implements RDFHandler {

    /** */
    private static final Logger LOGGER = Logger.getLogger(ResourceRenameHandler.class);

    /** */
    private ArrayList<Statement> originalStatements = new ArrayList<Statement>();
    private ArrayList<Statement> renamedStatements = new ArrayList<Statement>();

    /** */
    private RepositoryConnection repoConn;
    private Map<String, String> renamings;

    /** */
    private int handledStatementCount;

    /**
     *
     * @param repoConn
     */
    public ResourceRenameHandler(RepositoryConnection repoConn, Map<String, String> renamings) {

        if (repoConn == null || renamings == null || renamings.isEmpty()) {
            throw new IllegalArgumentException("None of the input arguments must be null or empty");
        }

        this.repoConn = repoConn;
        this.renamings = renamings;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openrdf.rio.RDFHandler#handleStatement(org.openrdf.model.Statement)
     */
    @Override
    public void handleStatement(Statement statement) throws RDFHandlerException {

        originalStatements.add(statement);

        Resource subject = statement.getSubject();
        URI predicate = statement.getPredicate();
        Value object = statement.getObject();
        Resource context = statement.getContext();

        ValueFactory valueFactory = repoConn.getValueFactory();

        String newSubjectUri = getNewUri(subject.stringValue());
        if (newSubjectUri != null) {
            subject = valueFactory.createURI(newSubjectUri);
        }

        if (object instanceof Resource) {

            String newObjectUri = getNewUri(object.stringValue());
            if (newObjectUri != null) {
                object = valueFactory.createURI(newObjectUri);
            }
        }

        Statement renamedStatement = valueFactory.createStatement(subject, predicate, object, context);
        renamedStatements.add(renamedStatement);

        handledStatementCount++;
    }

    /**
     *
     * @throws RepositoryException
     */
    public void execute() throws RepositoryException {

        int originalStatementsSize = originalStatements.size();
        if (originalStatementsSize > 0) {

            LOGGER.debug("Removing " + originalStatementsSize + " original statements from the repository");
            repoConn.remove(originalStatements);
        } else {
            LOGGER.debug("No original statements recorded by the handler");
        }

        int renamedStatementsSize = renamedStatements.size();
        if (renamedStatementsSize > 0) {

            LOGGER.debug("Adding " + renamedStatementsSize + " renamed statements to the repository");
            repoConn.add(renamedStatements);
        } else {
            LOGGER.debug("No renamed statements recorded by the handler");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openrdf.rio.RDFHandler#endRDF()
     */
    @Override
    public void endRDF() throws RDFHandlerException {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openrdf.rio.RDFHandler#handleComment(java.lang.String)
     */
    @Override
    public void handleComment(String arg0) throws RDFHandlerException {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openrdf.rio.RDFHandler#handleNamespace(java.lang.String, java.lang.String)
     */
    @Override
    public void handleNamespace(String arg0, String arg1) throws RDFHandlerException {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openrdf.rio.RDFHandler#startRDF()
     */
    @Override
    public void startRDF() throws RDFHandlerException {
    }

    /**
     *
     * @param oldUri
     * @return
     */
    private String getNewUri(String oldUri) {

        return renamings.get(oldUri);
    }

    /**
     * @return the handledStatementCount
     */
    public int getHandledStatementCount() {
        return handledStatementCount;
    }
}
