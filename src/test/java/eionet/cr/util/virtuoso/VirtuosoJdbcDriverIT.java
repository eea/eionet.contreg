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
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Enriko Käsper
 */

package eionet.cr.util.virtuoso;

import java.net.URI;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import eionet.cr.ApplicationTestContext;
import org.apache.commons.lang.math.NumberUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import virtuoso.jdbc4.VirtuosoConnectionPoolDataSource;
import virtuoso.jdbc4.VirtuosoPooledConnection;
import eionet.cr.config.GeneralConfig;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;
import org.junit.Ignore;

/**
 * Test Virtuoso Jdbc driver.
 *
 * @author Enriko Käsper
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class VirtuosoJdbcDriverIT extends CRDatabaseTestCase {

    /** Dummy graph URI. */
    private static final String DUMMY_GRAPH_URI = "http://test.virtuoso.jdbc.validity.com";

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /*
         * (non-Javadoc)
         *
         * @see eionet.cr.test.helpers.CRDatabaseTestCase#getRDFXMLSeedFiles()
         */
    @Override
    protected List<String> getRDFXMLSeedFiles() {
        return Arrays.asList("obligations.rdf");
    }

    /**
     * Test if CR uses correct Virtuoso JDBC driver. It shouldn't get "Too many open statements" error.
     *
     * @throws SQLException When problem with connecting to Virtuoso.
     */
    @Test
    public void testTooManyOpenStmts() throws SQLException {

        VirtuosoConnectionPoolDataSource dbsource = new VirtuosoConnectionPoolDataSource();

        String testDbURI = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_URL);
        URI uri = URI.create(testDbURI.substring(5));

        dbsource.setServerName(uri.getHost());
        dbsource.setPortNumber(uri.getPort());
        dbsource.setPassword(GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_PWD));
        dbsource.setUser(GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_USR));
        dbsource.setCharset("UTF-8");
        VirtuosoPooledConnection pooledConnection = (VirtuosoPooledConnection) dbsource.getPooledConnection();
        virtuoso.jdbc4.VirtuosoConnection con = pooledConnection.getVirtuosoConnection();
        String jdbcComp = "DB.DBA.TTLP (?, ?, ?, ?)";
        CallableStatement stmt = null;
        int MAXIT = 10000;
        for (int i = 0; i < MAXIT; i++) {
            try {
                stmt = con.prepareCall(jdbcComp);
                stmt.setString(1, "");
                stmt.setString(2, "");
                stmt.setString(3, DUMMY_GRAPH_URI);
                stmt.setInt(4, 256);
                stmt.execute();
                con.commit();
            } catch (Exception e) {
                e.printStackTrace();
                fail("can't add data to virtuoso. ");
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
        }
    }

    /**
     * Test simple insert into RDF_QUAD and a SPARQL select afterwards.
     *
     * @throws SQLException
     */
    @Test
    public void testSimpleInsertAndSPARQLSelect() throws SQLException {

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = SesameUtil.getSQLConnection();
            stmt = conn.createStatement();
            int i =
                    stmt.executeUpdate("insert into DB.DBA.RDF_QUAD (S,P,O,G) values (" + "iri_to_id('http://test.uri/subject')"
                            + ", iri_to_id('http://test.uri/predicate')" + ", iri_to_id('http://test.uri/object')"
                            + ", iri_to_id('http://test.uri/graph'))");
            assertEquals("Expect one triple to have been inserted!", 1, i);
            SQLUtil.close(stmt);
            SQLUtil.close(conn);

            conn = SesameUtil.getSQLConnection();
            stmt = conn.createStatement();
            rs =
                    stmt.executeQuery("sparql select * where { graph ?g {"
                            + "<http://test.uri/subject> <http://test.uri/predicate> <http://test.uri/object>}} limit 1");
            assertTrue("Expected the previously inserted triple to exist in triplestore", rs.next());
            String graph = "http://test.uri/graph";
            assertEquals("Expected previously inserted triple in " + graph, graph, rs.getString(1));
        } catch (SQLException e) {
            fail("Wasn't expecting this exception: " + e);
            throw e;
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }
    }

    /**
     * Test simple insert into RDF_QUAD and an SQL select afterwards.
     *
     * @throws SQLException
     */
    @Test
    public void testSimpleInsertAndSQLSelect() throws SQLException {

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = SesameUtil.getSQLConnection();
            stmt = conn.createStatement();
            int i =
                    stmt.executeUpdate("insert into DB.DBA.RDF_QUAD (S,P,O,G) values (" + "iri_to_id('http://test.uri/subject')"
                            + ", iri_to_id('http://test.uri/predicate')" + ", iri_to_id('http://test.uri/object')"
                            + ", iri_to_id('http://test.uri/graph'))");
            assertEquals("Expect one triple to have been inserted!", 1, i);
            SQLUtil.close(stmt);
            SQLUtil.close(conn);

            conn = SesameUtil.getSQLConnection();
            stmt = conn.createStatement();
            rs =
                    stmt.executeQuery("select top(1) id_to_iri(G) from DB.DBA.RDF_QUAD where "
                            + "S=iri_to_id('http://test.uri/subject') "
                            + "and P=iri_to_id('http://test.uri/predicate') and O=iri_to_id('http://test.uri/object')");
            assertTrue("Expected the previously inserted row to exist in DB.DBA.RDF_QUAD", rs.next());
            String graph = "http://test.uri/graph";
            assertEquals("Expected previously inserted DB.DBA.RDF_QUAD row with G=" + graph, graph, rs.getString(1));
        } catch (SQLException e) {
            fail("Wasn't expecting this exception: " + e);
            throw e;
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }
    }

    /**
     * Test that the {@link RepositoryConnection#clear(org.openrdf.model.Resource...)} method works.
     *
     * @throws SQLException
     * @throws RepositoryException
     */
    @Test
    public void testRepositoryConnectionClearGraph() throws SQLException, RepositoryException {

        Connection conn = null;
        RepositoryConnection repoConn = null;
        try {
            conn = SesameUtil.getSQLConnection();
            repoConn = SesameUtil.getRepositoryConnection();

            String seedFileGraphUri = getSeedFileGraphUri("obligations.rdf");
            ValueFactory vf = repoConn.getValueFactory();
            repoConn.clear(vf.createURI(seedFileGraphUri));

            Object count =
                    SQLUtil.executeSingleReturnValueQuery("sparql select count(*) from <" + seedFileGraphUri
                            + "> where {?s ?p ?o}", conn);
            assertNotNull("Expected non-null count", count);
            assertEquals("Unexpected count", "0", count.toString());
        } finally {
            SQLUtil.close(conn);
            SesameUtil.close(repoConn);
        }
    }

    /**
     * Test that graph deletion works via SQL.
     *
     * @throws SQLException
     * @throws RepositoryException
     */
    @Test
    public void testSqlConnectionClearGraph() throws SQLException, RepositoryException {

        String uri = getSeedFileGraphUri("obligations.rdf");
        String countQuery = "sparql select count(*) from <" + uri + "> where {?s ?p ?o}";

        Statement stmt = null;
        Connection conn = null;
        try {
            conn = SesameUtil.getSQLConnection();
            stmt = conn.createStatement();

            Object count = SQLUtil.executeSingleReturnValueQuery(countQuery, conn);
            assertNotNull("Expected non-null count", count);
            assertTrue("Expected at least one triple in graph " + uri, NumberUtils.toInt(count.toString(), 0) > 0);

            stmt.executeUpdate("SPARQL CLEAR GRAPH <" + uri + ">");

            count = SQLUtil.executeSingleReturnValueQuery(countQuery, conn);
            assertNotNull("Expected non-null count", count);
            assertEquals("Unexpected count", "0", count.toString());
        } finally {
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }
    }

    /**
     * Test SPARQL for deleting all triples about a resource.
     *
     * @throws SQLException
     */
    @Test
    public void testSparqlDeleteAllTriplesAboutResource() throws SQLException {

        String uri = "http://rod.eionet.europa.eu/obligations/171";
        String deleteSparql = "DELETE {GRAPH ?g {?s ?p ?o}} WHERE {GRAPH ?g {?s ?p ?o filter (?s = <" + uri + ">)}}";
        String deleteSql = "SPARQL " + deleteSparql;

        Statement stmt = null;
        Connection conn = null;
        try {
            conn = SesameUtil.getSQLConnection();
            stmt = conn.createStatement();

            String countQuery = "sparql select count(*) where {<" + uri + "> ?p ?o}";
            Object count = SQLUtil.executeSingleReturnValueQuery(countQuery, conn);
            assertNotNull("Expected non-null count", count);
            assertTrue("Expected at least one triple about " + uri, NumberUtils.toInt(count.toString(), 0) > 0);

            stmt.executeUpdate(deleteSql);

            count = SQLUtil.executeSingleReturnValueQuery(countQuery, conn);
            assertNotNull("Expected non-null count", count);
            assertEquals("Unexpected count", "0", count.toString());
        } finally {
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }
    }
}
