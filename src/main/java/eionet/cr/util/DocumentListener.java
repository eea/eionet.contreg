package eionet.cr.util;

import org.apache.lucene.document.Document;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public interface DocumentListener {

	/**
	 * 
	 */
	public void handleDocument(Document document);
	
	/**
	 * 
	 */
	public void done();
}
