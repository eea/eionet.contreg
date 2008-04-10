package eionet.cr.index.walk;

import org.apache.lucene.document.Document;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public interface AllDocsWalkerListener {

	/**
	 * 
	 */
	public void handleDocument(Document document);
	
	/**
	 * 
	 */
	public void done();
}
