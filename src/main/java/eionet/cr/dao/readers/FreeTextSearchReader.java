package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.openrdf.query.Binding;
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
		
		Iterator<Binding> iterator = bindingSet.iterator();
		
		// expecting the URI of the matching subject to be in column "s"
		String subjectUri = bindingSet.getValue("s").stringValue();
		resultList.add((T)subjectUri);
		
		// expecting the column "g" to contain the URI of the triple source
		// where the search hit came from
		String hitSourceUri = bindingSet.getValue("g").stringValue();
		
		Long subjectHash = Long.valueOf(Hashes.spoHash(subjectUri));
		Long hitSourceHash = Long.valueOf(Hashes.spoHash(hitSourceUri));
		hitSourcesBySubjectHashes.put(subjectHash, hitSourceHash);
	}

	/**
	 * 
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
}
