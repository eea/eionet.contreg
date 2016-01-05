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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;

import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.Hashes;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;

/**
 * Test timestamp reading with Virtuoso JDBC4 driver.
 *
 * @author Jaanus
 */
public class JdbcTimestampReadingIT extends CRDatabaseTestCase {

    /** */
    private static final String DATE_COMPARISON_PATTERN = "yyyy-MM-dd HH";

    /** */
    private static final String URL = "http://test.ee";

    /**
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public void test() throws SQLException {

        long urlHash = Hashes.spoHash(URL);

        Connection conn = null;
        ResultSet rs = null;
        Statement stmt = null;
        try {
            conn = SesameUtil.getSQLConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate("delete from harvest_source where url_hash=" + urlHash);
            stmt.executeUpdate("insert into harvest_source (URL, url_hash, time_created) values ('" + URL + "', " + urlHash + ", now())");

            rs = stmt.executeQuery("select * from HARVEST_SOURCE where URL_HASH=" + urlHash);
            assertTrue("Result set has rows", rs.next());

            String url = rs.getString("URL");
            Timestamp created = rs.getTimestamp("TIME_CREATED");

            assertEquals("Source URL", URL, url);
            assertEquals("Time created", DateFormatUtils.format(new Date(), DATE_COMPARISON_PATTERN),
                    DateFormatUtils.format(created, DATE_COMPARISON_PATTERN));
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }
    }

}
