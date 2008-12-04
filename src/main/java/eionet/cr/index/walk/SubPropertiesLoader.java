package eionet.cr.index.walk;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;

import eionet.cr.common.Predicates;
import eionet.cr.index.SubProperties;
import eionet.cr.util.DocumentListener;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubPropertiesLoader implements DocumentListener {
	
	/** */
	private static Log logger = LogFactory.getLog(SubPropertiesLoader.class);
	
	/** */
	private int countLoaded = 0;
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.index.walk.DocumentListener#handleDocument(org.apache.lucene.document.Document)
	 */
	public void handleDocument(Document document) {
		
		String docID = document.get(Predicates.DOC_ID);
		String[] subPropertyOf = document.getValues(Predicates.RDFS_SUB_PROPERTY_OF);
		if (docID!=null && subPropertyOf!=null && subPropertyOf.length>0){
			SubProperties.addSubProperty(subPropertyOf, docID);
			countLoaded++;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.index.walk.DocumentListener#done()
	 */
	public void done() {
		
		logger.debug(countLoaded + " sub-properties loaded for " + SubProperties.getCount() + " resources");
		
		// we "artifically" force DublinCore's Title to be a sub-property of RDF Schema's Label, because we simply want to :) 
		SubProperties.addSubProperty(Predicates.RDFS_LABEL, Predicates.DC_TITLE);
	}
}
