package eionet.cr.web.action;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import eionet.cr.config.GeneralConfig;

public class CopyPostgreSQLTablesToVirtuoso {

    /**
     * @param args
     */
    public static void main(String[] args) {

        Connection pgreCon = null;
        Connection virtCon = null;

        try {
            String pgreDrv = GeneralConfig.getProperty(GeneralConfig.DB_DRV);
            String pgreUrl = GeneralConfig.getProperty(GeneralConfig.DB_URL);
            String pgreUsr = GeneralConfig.getProperty(GeneralConfig.DB_USR);
            String pgrePwd = GeneralConfig.getProperty(GeneralConfig.DB_PWD);

            String virtDrv = GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_DB_DRV);
            String virtUrl = GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_DB_URL);
            String virtUsr = GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_DB_USR);
            String virtPwd = GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_DB_PWD);

            Class.forName(pgreDrv);
            pgreCon = DriverManager.getConnection(pgreUrl, pgreUsr, pgrePwd);

            Class.forName(virtDrv);
            virtCon = DriverManager.getConnection(virtUrl, virtUsr, virtPwd);

            copyHarvestSource(virtCon, pgreCon);
            copyHarvestMessage(virtCon, pgreCon);
            copyHarvest(virtCon, pgreCon);
            // copyDocumentation(virtCon, pgreCon);
            copyUrgentHarvestQueue(virtCon, pgreCon);
            copySpoBinary(virtCon, pgreCon);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pgreCon != null)
                    pgreCon.close();

                if (virtCon != null)
                    virtCon.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void copyHarvestSource(Connection virtCon, Connection pgreCon) throws Exception {

        System.out.println("HARVEST_SOURCE started");

        ResultSet rs = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;

        try {
            stmt2 = virtCon.prepareStatement("INSERT INTO harvest_source VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            stmt = pgreCon.prepareStatement("SELECT * FROM harvest_source");
            rs = stmt.executeQuery();
            while (rs.next()) {
                if (rs.getRow() % 10000 == 0) {
                    stmt2.executeBatch();
                    stmt2.clearBatch();
                    System.gc();
                }
                stmt2.setInt(1, rs.getInt("harvest_source_id"));
                stmt2.setLong(2, rs.getLong("url_hash"));
                stmt2.setString(3, rs.getString("url"));
                stmt2.setString(4, rs.getString("emails"));
                stmt2.setTimestamp(5, rs.getTimestamp("time_created"));
                stmt2.setInt(6, rs.getInt("statements"));
                stmt2.setInt(7, rs.getInt("count_unavail"));
                stmt2.setTimestamp(8, rs.getTimestamp("last_harvest"));
                stmt2.setInt(9, rs.getInt("interval_minutes"));
                stmt2.setLong(10, rs.getLong("source"));
                stmt2.setLong(11, rs.getLong("gen_time"));
                stmt2.setString(12, rs.getString("last_harvest_failed"));
                stmt2.setString(13, rs.getString("priority_source"));
                stmt2.setString(14, rs.getString("source_owner"));
                stmt2.setString(15, rs.getString("permanent_error"));
                stmt2.setString(16, rs.getString("media_type"));
                stmt2.addBatch();
            }
            stmt2.executeBatch();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null)
                rs.close();

            if (stmt != null)
                stmt.close();

            if (stmt2 != null)
                stmt2.close();
        }

        System.out.println("HARVEST_SOURCE finished");
    }

    private static void copyHarvestMessage(Connection virtCon, Connection pgreCon) throws Exception {

        System.out.println("HARVEST_MESSAGE started");

        ResultSet rs = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;

        try {
            stmt2 = virtCon.prepareStatement("INSERT INTO harvest_message VALUES (?,?,?,?,?)");
            stmt = pgreCon.prepareStatement("SELECT * FROM harvest_message");
            rs = stmt.executeQuery();
            while (rs.next()) {
                if (rs.getRow() % 10000 == 0) {
                    stmt2.executeBatch();
                    stmt2.clearBatch();
                    System.gc();
                }
                stmt2.setInt(1, rs.getInt("harvest_message_id"));
                stmt2.setInt(2, rs.getInt("harvest_id"));
                stmt2.setString(3, rs.getString("type"));
                stmt2.setString(4, rs.getString("message"));
                stmt2.setString(5, rs.getString("stack_trace"));
                stmt2.addBatch();
            }
            stmt2.executeBatch();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null)
                rs.close();

            if (stmt != null)
                stmt.close();

            if (stmt2 != null)
                stmt2.close();
        }

        System.out.println("HARVEST_MESSAGE finished");
    }

    private static void copyHarvest(Connection virtCon, Connection pgreCon) throws Exception {

        System.out.println("HARVEST started");

        ResultSet rs = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;

        try {
            stmt2 = virtCon.prepareStatement("INSERT INTO harvest VALUES (?,?,?,?,?,?,?,?,?,?,?)");
            stmt = pgreCon.prepareStatement("SELECT * FROM harvest");
            rs = stmt.executeQuery();
            while (rs.next()) {
                if (rs.getRow() % 10000 == 0) {
                    stmt2.executeBatch();
                    stmt2.clearBatch();
                    System.gc();
                }
                stmt2.setInt(1, rs.getInt("harvest_id"));
                stmt2.setInt(2, rs.getInt("harvest_source_id"));
                stmt2.setString(3, rs.getString("type"));
                stmt2.setString(4, rs.getString("username"));
                stmt2.setString(5, rs.getString("status"));
                stmt2.setTimestamp(6, rs.getTimestamp("started"));
                stmt2.setTimestamp(7, rs.getTimestamp("finished"));
                stmt2.setInt(8, rs.getInt("tot_statements"));
                stmt2.setInt(9, rs.getInt("lit_statements"));
                stmt2.setInt(10, rs.getInt("res_statements"));
                stmt2.setInt(11, rs.getInt("enc_schemes"));
                stmt2.addBatch();
            }
            stmt2.executeBatch();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null)
                rs.close();

            if (stmt != null)
                stmt.close();

            if (stmt2 != null)
                stmt2.close();
        }

        System.out.println("HARVEST finished");
    }

    private static void copyDocumentation(Connection virtCon, Connection pgreCon) throws Exception {

        System.out.println("DOCUMENTATION started");

        ResultSet rs = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;

        try {
            stmt2 = virtCon.prepareStatement("INSERT INTO documentation VALUES (?,?,?,?)");
            stmt = pgreCon.prepareStatement("SELECT * FROM documentation");
            rs = stmt.executeQuery();
            while (rs.next()) {
                if (rs.getRow() % 10000 == 0) {
                    stmt2.executeBatch();
                    stmt2.clearBatch();
                    System.gc();
                }
                stmt2.setString(1, rs.getString("page_id"));
                stmt2.setString(2, rs.getString("content_type"));
                stmt2.setBinaryStream(3, rs.getBinaryStream("content"));
                stmt2.setString(4, "");
                stmt2.addBatch();
            }
            stmt2.executeBatch();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null)
                rs.close();

            if (stmt != null)
                stmt.close();

            if (stmt2 != null)
                stmt2.close();
        }

        System.out.println("DOCUMENTATION finished");
    }

    private static void copyUrgentHarvestQueue(Connection virtCon, Connection pgreCon) throws Exception {

        System.out.println("URGENT_HARVEST_QUEUE started");

        ResultSet rs = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;

        try {
            stmt2 = virtCon.prepareStatement("INSERT INTO urgent_harvest_queue VALUES (?,?,?)");
            stmt = pgreCon.prepareStatement("SELECT * FROM urgent_harvest_queue");
            rs = stmt.executeQuery();
            while (rs.next()) {
                if (rs.getRow() % 10000 == 0) {
                    stmt2.executeBatch();
                    stmt2.clearBatch();
                    System.gc();
                }
                stmt2.setString(1, rs.getString("url"));
                stmt2.setTimestamp(2, rs.getTimestamp("timestamp"));
                stmt2.setString(3, rs.getString("pushed_content"));
                stmt2.addBatch();
            }
            stmt2.executeBatch();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null)
                rs.close();

            if (stmt != null)
                stmt.close();

            if (stmt2 != null)
                stmt2.close();
        }

        System.out.println("URGENT_HARVEST_QUEUE finished");
    }

    private static void copySpoBinary(Connection virtCon, Connection pgreCon) throws Exception {

        System.out.println("SPO_BINARY started");

        ResultSet rs = null;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;

        try {
            stmt2 = virtCon.prepareStatement("INSERT INTO spo_binary VALUES (?,?,?,?)");
            stmt = pgreCon.prepareStatement("SELECT * FROM spo_binary");
            rs = stmt.executeQuery();
            while (rs.next()) {
                if (rs.getRow() % 10000 == 0) {
                    stmt2.executeBatch();
                    stmt2.clearBatch();
                    System.gc();
                }
                stmt2.setLong(1, rs.getLong("subject"));
                stmt2.setString(2, rs.getString("obj_lang"));
                stmt2.setString(3, rs.getString("datatype"));
                stmt2.setString(4, rs.getBoolean("must_embed") ? "Y" : "N");
                stmt2.addBatch();
            }
            stmt2.executeBatch();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null)
                rs.close();

            if (stmt != null)
                stmt.close();

            if (stmt2 != null)
                stmt2.close();
        }

        System.out.println("SPO_BINARY finished");
    }

}
