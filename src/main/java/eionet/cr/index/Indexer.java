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

	/**
	 * 
	 * @param resource
	 * @throws IndexException
	 */
	public abstract void indexRDFResource(RDFResource resource) throws IndexException;

	/**
	 * 
	 * @throws IndexException
	 */
	public abstract void close() throws IndexException;
}
