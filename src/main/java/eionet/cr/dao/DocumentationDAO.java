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
    public DocumentationDTO getDocObject(String pageId) throws DAOException;

    /**
     * Return object from documentation table
     * @param boolean whether or not to show only html objects
     * @return List<DocumentationDTO>
     * @throws DAOException
     */
    public List<DocumentationDTO> getDocObjects(boolean htmlOnly) throws DAOException;

    /**
     * @param pageId
     * @param contentType
     * @param fileName
     * @param title
     * @throws DAOException
     */
    public void insertContent(String pageId, String contentType, String fileName, String title) throws DAOException;

    /**
     * Checks if such page_id already exists in database
     * @param pageId
     * @return boolean
     * @throws DAOException
     */
    public boolean idExists(String pageId) throws DAOException;

}
