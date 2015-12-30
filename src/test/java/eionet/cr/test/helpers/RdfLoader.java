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
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.test.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;

/**
 * Helper class for loading any given resource file into triple store.
 */
public class RdfLoader {

    /** SQL for deleting all triples. */
    private static final String DELETE_ALL_TRIPLES_SQL = "delete from DB.DBA.RDF_QUAD";

    /** URI prefix for generating dummy graph URIs. */
    public static final String DUMMY_GRAPH_PREFIX = "http://test.com/test/";

    /**
     * Calls {@link #loadIntoTripleStore(String, RDFFormat, String, String)} with the last two inputs set to null.
     *
     * @param fileName See {@link #loadIntoTripleStore(String, RDFFormat, String, String)}.
     * @param rdfFormat See {@link #loadIntoTripleStore(String, RDFFormat, String, String)}.
     * @throws IOException See {@link #loadIntoTripleStore(String, RDFFormat, String, String)}.
     * @throws OpenRDFException See {@link #loadIntoTripleStore(String, RDFFormat, String, String)}.
     */
    public void loadIntoTripleStore(String fileName, RDFFormat rdfFormat) throws IOException, OpenRDFException {
        loadIntoTripleStore(fileName, rdfFormat, null, null);
    }

    /**
     * Loads given file into triple store.
     *
     * @param fileName Name of the resource (i.e. seed) file to load. Must not be blank!
     * @param graphUri Target graph URI. If blank, then assumed to be {@link #DUMMY_GRAPH_PREFIX} + fileName.
     * @param baseUri Base URI for resolving relative URLs in the file. If blank then assumed same as target graph URI.
     * @throws IOException When problem with reading the resource file.
     * @throws OpenRDFException When problem with accessing the triple store or parsing the file.
     */
    public void loadIntoTripleStore(String fileName, RDFFormat rdfFormat, String graphUri, String baseUri) throws IOException,
            OpenRDFException {

        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("File name must not be blank!");
        } else {
            fileName = fileName.trim();
        }

        if (StringUtils.isBlank(graphUri)) {
            graphUri = getSeedFileGraphUri(fileName);
        }

        if (StringUtils.isBlank(baseUri)) {
            baseUri = graphUri;
        }

        if (rdfFormat == null) {
            rdfFormat = RDFFormat.RDFXML;
        }

        RepositoryConnection repoConn = null;
        InputStream inputStream = null;
        try {
            inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
            if (inputStream == null) {
                throw new IOException("Could not load resource by the name of " + fileName);
            }

            repoConn = SesameUtil.getRepositoryConnection();
            repoConn.add(new InputStreamReader(inputStream, "UTF-8"), baseUri, rdfFormat, repoConn.getValueFactory().createURI(graphUri));
        } finally {
            SesameUtil.close(repoConn);
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Deletes all triples from the triple store.
     *
     * @throws SQLException When any sort of SQL error happens.
     */
    public void clearAllTriples() throws SQLException {

        Connection conn = null;
        Statement stmt = null;
        try {
            conn = SesameUtil.getSQLConnection();
            String url = conn.getMetaData().getURL();
            if (url != null && url.contains(":1111/")) {
                throw new CRRuntimeException("Triplestore clearance not supported on port 1111, as a double security measure!");
            }

            stmt = conn.createStatement();
            stmt.executeUpdate(DELETE_ALL_TRIPLES_SQL);
        } finally {
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }
    }

    /**
     * Returns the URI of the graph where the {@link RdfLoader} would load a given file if the graph name is not given.
     * In such a case the graph URI is generated as {@link #DUMMY_GRAPH_PREFIX} + fileName, where the latter is file loaded-
     *
     * @param fileName The loadable file.
     * @return The generated graph URI.
     */
    public static String getSeedFileGraphUri(String fileName) {
        return DUMMY_GRAPH_PREFIX + fileName;
    }
}
