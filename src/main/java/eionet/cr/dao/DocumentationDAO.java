/**
 *
 */
package eionet.cr.dao;

import java.util.List;

import eionet.cr.dto.DocumentationDTO;

/**
 * @author Risto Alt
 * 
 */
public interface DocumentationDAO extends DAO {

    /**
     * @param pageId
     * @return DocumentationDTO
     * @throws DAOException
     */
    DocumentationDTO getDocObject(String pageId) throws DAOException;

    /**
     * Return object from documentation table
     * 
     * @param boolean whether or not to show only html objects
     * 
     * @return List<DocumentationDTO>
     * @throws DAOException
     */
    List<DocumentationDTO> getDocObjects(boolean htmlOnly) throws DAOException;

    /**
     * @param pageId
     * @param contentType
     * @param title
     * @throws DAOException
     */
    void insertContent(String pageId, String contentType, String title) throws DAOException;

    /**
     * Checks if such page_id already exists in database
     * 
     * @param pageId
     * @return boolean
     * @throws DAOException
     */
    boolean idExists(String pageId) throws DAOException;
}
