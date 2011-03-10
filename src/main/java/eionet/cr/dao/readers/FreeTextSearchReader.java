package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Hashes;

/**
 *
 * @author jaanus
 *
 * @param <T>
 */
public class FreeTextSearchReader<T> extends ResultSetMixedReader<T>{

    /** */
    private LinkedHashMap<Long,Long> hitSourcesBySubjectHashes = new LinkedHashMap<Long, Long>();
    private List<String> graphUris = new ArrayList<String>();

    /*
     * (non-Javadoc)
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        // expecting the hash of the matching subject URI to be in the 1st column
        Long subjectHash = Long.valueOf(rs.getLong(1));
        resultList.add((T)subjectHash);

        // expecting the 2nd column to contain the hash of the triple source
        // where the search hit came from
        Long hitSourceHash = Long.valueOf(rs.getLong(2));
        hitSourcesBySubjectHashes.put(subjectHash, hitSourceHash);
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void readRow(BindingSet bindingSet) throws ResultSetReaderException {

        Value subjectValue = bindingSet.getValue("s");

        // expecting the URI of the matching subject to be in column "s"
        String subjectUri = subjectValue.stringValue();
        if (subjectValue instanceof BNode && blankNodeUriPrefix!=null){
            if (!subjectUri.startsWith(blankNodeUriPrefix)){
                subjectUri = blankNodeUriPrefix + subjectUri;
            }
        }
        resultList.add((T)subjectUri);

        // expecting the column "g" to contain the URI of the triple source
        // where the search hit came from
        String hitSourceUri = bindingSet.getValue("g").stringValue();

        //Store graph uris to get cr:contentLastModified later
        if(graphUris != null && !graphUris.contains(hitSourceUri))
            graphUris.add(hitSourceUri);

        Long subjectHash = Long.valueOf(Hashes.spoHash(subjectUri));
        Long hitSourceHash = Long.valueOf(Hashes.spoHash(hitSourceUri));
        hitSourcesBySubjectHashes.put(subjectHash, hitSourceHash);
    }

    /**
     * @param subjects
     */
    public void populateHitSources(Collection<SubjectDTO> subjects){

        for (SubjectDTO subjectDTO : subjects){

            Long subjectHash = Long.valueOf(subjectDTO.getUriHash());
            Long hitSource = hitSourcesBySubjectHashes.get(subjectHash);
            if (hitSource!=null){
                subjectDTO.setHitSource(hitSource.longValue());
            }
        }
    }

    /**
     * @return List<String>
     */
    public List<String> getGraphUris() {
        return graphUris;
    }
}
