/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */

package eionet.cr.harvest.load;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import eionet.cr.util.sql.SQLUtil;

/**
 * Implementation of OpenRDF's {@link RDFHandler} that will be used by implementations of {@link ContentLoader}. Contains callback
 * methods for listening to the content coming from {@link ContentLoader} and loading them into repository.
 *
 * @author Jaanus Heinlaid
 */
public class RDFContentHandler implements RDFHandler {

    /** */
    private static final Logger LOGGER = Logger.getLogger(RDFContentHandler.class);

    /**
     * Triples will be logged and/or committed in batches of this size. Virtuoso's Sesame driver uses 5000 by default.
     */
    private static final int BATCH_SIZE = 5000;

    /** */
    private RepositoryConnection repoConn;
    private Connection sqlConn;

    /** The graph where the triples will be loaded into. */
    private Resource context;

    /** Number of triples loaded. */
    private int triplesLoaded;

    /**
     * @param repoConn
     * @param sqlConn
     * @param contextUri The URI of the graph where the triples will be loaded into.
     */
    public RDFContentHandler(RepositoryConnection repoConn, Connection sqlConn, String contextUri) {

        this.repoConn = repoConn;
        this.sqlConn = sqlConn;
        this.context = repoConn.getValueFactory().createURI(contextUri);
    }

    /**
     * @see org.openrdf.rio.RDFHandler#startRDF()
     */
    @Override
    public void startRDF() throws RDFHandlerException {
        // No specific handling here.
    }

    /**
     * @see org.openrdf.rio.RDFHandler#handleNamespace(java.lang.String, java.lang.String)
     */
    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {

        // Code copy-pasted from VirtuosoRepositoryConnection.

        String str = "DB.DBA.XML_SET_NS_DECL(?, ?, 1)";
        PreparedStatement stmt = null;
        try {
            stmt = sqlConn.prepareStatement(str);
            stmt.setString(1, prefix);
            stmt.setString(2, uri);
            stmt.execute();
        } catch (SQLException e) {
            throw new RDFHandlerException("Problem executing query: " + str, e);
        } finally {
            SQLUtil.close(stmt);
        }
    }

    /**
     * @see org.openrdf.rio.RDFHandler#handleStatement(org.openrdf.model.Statement)
     */
    @Override
    public void handleStatement(Statement rdfStatement) throws RDFHandlerException {

        // Add the given statement (i.e. triple) into repository.
        try {
            repoConn.add(rdfStatement, context);
            triplesLoaded++;
            if (triplesLoaded % BATCH_SIZE == 0) {
                LOGGER.trace("Statement counter = " + triplesLoaded);
            }
        } catch (RepositoryException e) {
            throw new RDFHandlerException(e.getMessage(), e);
        }
    }

    /**
     * @see org.openrdf.rio.RDFHandler#handleComment(java.lang.String)
     */
    @Override
    public void handleComment(String arg0) throws RDFHandlerException {
        // No specific handling here.
    }

    /**
     * @see org.openrdf.rio.RDFHandler#endRDF()
     */
    @Override
    public void endRDF() throws RDFHandlerException {
        // No specific handling here.
    }

    /**
     * @return the triplesLoaded
     */
    public int getNumberOfTriplesLoaded() {
        return triplesLoaded;
    }
}
