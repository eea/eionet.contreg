package eionet.cr.index.walk;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;

import eionet.cr.common.Identifiers;
import eionet.cr.index.EncodingSchemes;
import eionet.cr.util.DocumentListener;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class EncodingSchemesLoader implements DocumentListener {
	
	/** */
	private static Log logger = LogFactory.getLog(EncodingSchemesLoader.class);
	
	/** */
	private int countLoaded = 0;

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.index.walk.DocumentListener#handleDocument(org.apache.lucene.document.Document)
	 */
	public void handleDocument(Document document) {
		
		String docID = document.get(Identifiers.DOC_ID);
		String[] labels = document.getValues(Identifiers.RDFS_LABEL);
		if (docID!=null && labels!=null && labels.length>0){
			EncodingSchemes.update(docID, labels);
			countLoaded++;
		}
	}

	/**
	 * @return the countLoaded
	 */
	public int getCountLoaded() {
		return countLoaded;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.index.walk.DocumentListener#done()
	 */
	public void done() {
		
		logger.debug(countLoaded + " encoding schemes loaded");
	}
}
