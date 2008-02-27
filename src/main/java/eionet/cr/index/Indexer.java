package eionet.cr.index;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;

import eionet.cr.harvest.util.RDFResource;

/**
 * 
 * @author heinljab
 *
 */
public abstract class Indexer {
	
	/** */
	public static final String ALL_CONTENT_FIELD = "content";

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
}
