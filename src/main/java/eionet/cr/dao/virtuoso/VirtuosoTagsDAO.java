package eionet.cr.dao.virtuoso;

import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.TagsDAO;
import eionet.cr.dto.TagDTO;

/**
 *
 * @author jaanus
 *
 */
public class VirtuosoTagsDAO extends VirtuosoBaseDAO implements TagsDAO {

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.TagsDAO#getTagCloud()
     */
    @Override
    public List<TagDTO> getTagCloud() throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

}
