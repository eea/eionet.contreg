package eionet.cr.index;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;

import eionet.cr.harvest.util.RDFResource;
import eionet.cr.util.Identifiers;

/**
 * 
 * @author heinljab
 *
 */
public abstract class Indexer {
	
	/** */
	public static final String ALL_CONTENT_FIELD = Identifiers.ALL_LITERAL_CONTENT;

	/**
	 * 
	 * @param resource
	 * @throws IndexException
	 */
	public abstract void indexRDFResource(RDFResource resource) throws IndexException;
	
	/**
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 * 
	 */
	public abstract void close() throws CorruptIndexException, IOException;
	
	/**
	 * @throws IOException 
	 * 
	 */
	public abstract void abort() throws IOException;
}
