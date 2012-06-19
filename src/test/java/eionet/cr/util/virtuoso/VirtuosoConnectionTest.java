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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.junit.Test;

import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;

/**
 * Test Virtuoso Connection.
 *
 * @author Enriko Käsper
 */
public class VirtuosoConnectionTest extends TestCase {
    /*
     * (non-Javadoc)
     *
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        Connection conn = null;
        try {
            conn = SesameUtil.getSQLConnection();
            SQLUtil.executeUpdate("DELETE FROM URGENT_HARVEST_QUEUE WHERE \"URL\"='http://www.google.com'", conn);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLUtil.close(conn);
        }
        super.tearDown();
    }

    /**
     * Test creating many open statements through Sesame API.
     */
    @Test
    public void testTooManyOpenStmtsSesame() throws IOException, SQLException, InterruptedException {
        String sql = "insert into URGENT_HARVEST_QUEUE (URL,\"TIMESTAMP\") VALUES (?,NOW())";
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = SesameUtil.getSQLConnection();
            ps = conn.prepareStatement(sql);
            int MAXIT = 10000;
            for (int i = 0; i < MAXIT; i++) {
                ps.setString(1, "http://www.google.com");
                ps.execute();
                conn.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("can't add data to virtuoso. ");
        } finally {
            SQLUtil.close(ps);
            SQLUtil.close(conn);
        }
    }
}
