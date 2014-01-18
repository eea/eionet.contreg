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

import junit.framework.TestCase;

import org.junit.Test;

import virtuoso.jdbc4.VirtuosoConnectionPoolDataSource;
import virtuoso.jdbc4.VirtuosoPooledConnection;
import eionet.cr.config.GeneralConfig;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;

/**
 * Test Virtuoso Jdbc driver.
 *
 * @author Enriko Käsper
 */
public class VirtuosoJdbcDriverTest extends TestCase {

    /** Dummy graph URI. */
    private static final String DUMMY_GRAPH_URI = "http://test.virtuoso.jdbc.validity.com";

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
     * Test simple insert into RDF_QUAD and a selecy afterwards.
     *
     * @throws SQLException
     */
    @Test
    public void testSimpleInsertAndSparqlSelect() throws SQLException {

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
}
