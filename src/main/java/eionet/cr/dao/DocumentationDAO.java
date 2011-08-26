/**
 * 
 */
package eionet.cr.dao;

import net.sourceforge.stripes.action.FileBean;
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
     * @param pageId
     * @param contentType
     * @param file
     * @throws DAOException
     */
    public void insertFile(String pageId, String contentType, FileBean file) throws DAOException;

    /**
     * Checks if such page_id already exists in database
     * @param pageId
     * @return boolean
     * @throws DAOException
     */
    public boolean idExists(String pageId) throws DAOException;

}
