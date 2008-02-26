package eionet.cr.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import eionet.cr.config.GeneralConfig;
import eionet.cr.util.Messages;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class Searcher {
	
	/** */
	public static final String DEFAULT_FIELD = Indexer.ALL_CONTENT_FIELD;
	
	/** */
	private static Log logger = LogFactory.getLog(Searcher.class);
	
	/** */
	private static Analyzer defaultAnalyzer = new StandardAnalyzer();

	/** */
	private static IndexSearcher indexSearcher = null;
	
	/**
	 * 
	 * @return
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	private static IndexSearcher getIndexSearcher() throws CorruptIndexException, IOException{
		if (indexSearcher==null){
			logger.debug("Initializing index searcher");
			indexSearcher = new IndexSearcher(GeneralConfig.getProperty(GeneralConfig.LUCENE_INDEX_LOCATION));
		}
		return indexSearcher;
	}
	
	/**
	 * 
	 */
	public static synchronized void close(){
		if (indexSearcher!=null){
			logger.debug("Closing index searcher");
			try{
				indexSearcher.close();
			}
			catch (IOException e){
				logger.error("Failed to close index searcher: " + e.toString(), e);
			}
		}
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static synchronized List<Hashtable<String,String[]>> search(String query) throws ParseException, IOException{
		return search(query, defaultAnalyzer);
	}
	
	/**
	 * 
	 * @param query
	 * @param analyzer
	 * @return
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static synchronized List<Hashtable<String,String[]>> search(String query, Analyzer analyzer) throws ParseException, IOException{

		logger.debug("Performing search query: " + query);
		
		QueryParser parser = new QueryParser(DEFAULT_FIELD, analyzer);
		Query queryObj = parser.parse(query);
		Hits hits = getIndexSearcher().search(queryObj);
		return processHits(hits);
	}
	
	/**
	 * 
	 * @param hits
	 * @return
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	public static List<Hashtable<String,String[]>> processHits(Hits hits) throws CorruptIndexException, IOException{
		
		List<Hashtable<String,String[]>> list = new ArrayList<Hashtable<String,String[]>>();
		for (int i=0; hits!=null && i<hits.length(); i++){
			
			Document doc = hits.doc(i);
			Hashtable<String,String[]> hash = new Hashtable<String,String[]>();
			List allFields = doc.getFields();
			
			for (int j=0; allFields!=null && j<allFields.size(); j++){
				Fieldable field = (Fieldable)allFields.get(j);
				String fieldName = field.name();
				String[] fieldValues = doc.getValues(fieldName);
				if (fieldValues!=null && fieldValues.length>0)
					hash.put(field.name(), fieldValues);
			}
			
			list.add(hash);
		}
		
		return list;
	}
}
