package eionet.cr.index;

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
	 */
	public abstract void indexRDFResource(RDFResource resource);
}
