package eionet.cr.index;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

import eionet.cr.common.Predicates;
import eionet.cr.config.GeneralConfig;
import eionet.cr.harvest.util.RDFResource;
import eionet.cr.harvest.util.RDFResourceProperty;
import eionet.cr.util.FirstSeenTimestamp;
import eionet.cr.util.URIUtil;

/**
 * 
 * @author heinljab
 *
 */
public class Indexer {

	/** */
	public static final String ALL_CONTENT_FIELD = Predicates.ALL_LITERAL_CONTENT;
	
	/**
	 * 
	 * @return
	 */
	public static Analyzer getAnalyzer(){
		return new StandardAnalyzer();
	}
	
	/**
	 * 
	 * @param fieldName
	 * @return
	 */
	public static boolean isAnalyzedField(String fieldName){
		
		if (fieldName.equals(Predicates.DOC_ID))
			return false;
		else if (fieldName.equals(Predicates.SOURCE_ID))
			return false;
		else if (fieldName.equals(Predicates.FIRST_SEEN_TIMESTAMP))
			return false;
		else
			return true;
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static boolean isAnalyzedValue(String value){
		return URIUtil.isSchemedURI(value)==false;
	}
}
