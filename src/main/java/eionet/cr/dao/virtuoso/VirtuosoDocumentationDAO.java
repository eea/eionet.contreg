/**
 *
 */
package eionet.cr.dao.virtuoso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DocumentationDAO;
import eionet.cr.dto.DocumentationDTO;
import eionet.cr.util.sql.SQLUtil;

/**
 * @author Risto Alt
 *
 */
public class VirtuosoDocumentationDAO extends VirtuosoBaseDAO implements DocumentationDAO {

    /**
     * @see eionet.cr.dao.DocumentationDAO#getDocObject(java.lang.String)
     */
    @Override
    public DocumentationDTO getDocObject(String pageId) throws DAOException {

        String docObjectSQL = "select content_type, content, title from documentation where page_id='" + pageId + "'";

        DocumentationDTO ret = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(docObjectSQL);
            while (rs.next()) {
                ret = new DocumentationDTO();
                ret.setPageId(pageId);
                ret.setContentType(rs.getString("content_type"));
                ret.setContent(rs.getString("content"));
                ret.setTitle(rs.getString("title"));
            }
        } catch (SQLException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }

        return ret;
    }

    /**
     * @see eionet.cr.dao.DocumentationDAO#getHtmlDocObjects()
     */
    @Override
    public List<DocumentationDTO> getHtmlDocObjects() throws DAOException {

        String docObjectSQL = "select page_id, content_type, content, title from documentation where content_type='text/html'";

        List<DocumentationDTO> ret = new ArrayList<DocumentationDTO>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(docObjectSQL);
            while (rs.next()) {
                DocumentationDTO doc = new DocumentationDTO();
                doc.setPageId(rs.getString("page_id"));
                doc.setContentType(rs.getString("content_type"));
                doc.setContent(rs.getString("content"));
                doc.setTitle(rs.getString("title"));
                ret.add(doc);
            }
        } catch (SQLException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }

        return ret;
    }

    /**
     * @see eionet.cr.dao.DocumentationDAO#insertFile(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void insertFile(String pageId, String contentType, String fileName, String title) throws DAOException {

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("INSERT INTO documentation VALUES (?, ?, ?, ?)");
            ps.setString(1, pageId);
            ps.setString(2, contentType);
            ps.setString(3, fileName);
            ps.setString(4, title);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /**
     * @see eionet.cr.dao.DocumentationDAO#idExists(java.lang.String)
     */
    @Override
    public boolean idExists(String pageId) throws DAOException {

        String docObjectSQL = "select page_id from documentation where page_id='" + pageId + "'";

        boolean ret = false;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(docObjectSQL);
            while (rs.next()) {
                ret = true;
            }
        } catch (SQLException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }

        return ret;
    }

}
