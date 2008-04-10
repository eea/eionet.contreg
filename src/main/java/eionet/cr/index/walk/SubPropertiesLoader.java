package eionet.cr.index.walk;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;

import eionet.cr.index.EncodingSchemes;
import eionet.cr.index.SubProperties;
import eionet.cr.util.Identifiers;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubPropertiesLoader implements AllDocsWalkerListener {
	
	/** */
	private static Log logger = LogFactory.getLog(SubPropertiesLoader.class);
	
	/** */
	private int countLoaded = 0;
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.index.walk.AllDocsWalkerListener#handleDocument(org.apache.lucene.document.Document)
	 */
	public void handleDocument(Document document) {
		
		String docID = document.get(Identifiers.DOC_ID);
		String[] subPropertyOf = document.getValues(Identifiers.RDFS_SUB_PROPERTY_OF);
		if (docID!=null && subPropertyOf!=null && subPropertyOf.length>0){
			for (int i=0; i<subPropertyOf.length; i++){
				SubProperties.add(subPropertyOf[i], docID);
			}
			countLoaded++;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.index.walk.AllDocsWalkerListener#done()
	 */
	public void done() {
		logger.debug(countLoaded + " sub-properties loaded for " + SubProperties.getCount() + " resources");
	}
}
