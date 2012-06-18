package eionet.cr.dao.virtuoso;

import java.util.List;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.TagsDAO;
import eionet.cr.dao.readers.TagCloudReader;
import eionet.cr.dto.TagDTO;
import eionet.cr.util.Bindings;

/**
 * Queries for handling tags.
 * 
 * @author jaanus
 */
public class VirtuosoTagsDAO extends VirtuosoBaseDAO implements TagsDAO {
    /**
     * SPARQL returning distinct values of tags with corresponding tag counts.
     */
    public static final String GET_TAGS_WITH_FREQUENCIES_SPARQL = "SELECT ?o (count(?o) as ?c) WHERE { ?s ?crTagPredicate ?o } "
            + " GROUP BY (?o) ORDER BY DESC(?c)";

    /**
     * Returns tag cloud.
     * 
     * @see eionet.cr.dao.TagsDAO#getTagCloud()
     * @return List<TagDTO>
     * @throws DAOException if query fails
     */
    @Override
    public List<TagDTO> getTagCloud() throws DAOException {

        TagCloudReader reader = new TagCloudReader();
        Bindings bindings = new Bindings();
        bindings.setURI("crTagPredicate", Predicates.CR_TAG);
        executeSPARQL(GET_TAGS_WITH_FREQUENCIES_SPARQL, bindings, reader);

        return reader.getResultList();

    }
}
