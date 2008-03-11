package eionet.cr.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import eionet.cr.config.GeneralConfig;
import eionet.cr.util.Identifiers;
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
	private static String defaultAnalyzer = StandardAnalyzer.class.getName();

	/** */
	private static IndexSearcher indexSearcher = null;
	
	/** */
	private static LinkedHashMap<String,Analyzer> availableAnalyzers = null;
	
	/**
	 * 
	 * @return
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	private static IndexSearcher getIndexSearcher() throws CorruptIndexException, IOException{
		String indexLocation = GeneralConfig.getRequiredProperty(GeneralConfig.LUCENE_INDEX_LOCATION);
		logger.debug("Initializing searcher on index: " + indexLocation);
		return new IndexSearcher(indexLocation);
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
	 * @param analyzerName
	 * @return
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static synchronized List<Hashtable<String,String[]>> search(String query, String analyzerName) throws CorruptIndexException, IOException, ParseException{

		QueryParser parser = new QueryParser(DEFAULT_FIELD, getAvailableAnalyzer(analyzerName));
		Query queryObj = parser.parse(query);
		
		logger.debug("Performing search query: " + query);
		
		IndexSearcher indexSearcher = null;
		try{
			indexSearcher = getIndexSearcher();
			Hits hits = indexSearcher.search(queryObj);
			return processHits(hits);
		}
		finally{
			try{
				indexSearcher.close();
			}
			catch (IOException e){}
		}
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
	
	/**
	 * 
	 * @return
	 */
	public static String[] listAvailableAnalyzers(){
		
		if (availableAnalyzers==null)
			initAvailableAnalyzers();
		
		if (availableAnalyzers==null || availableAnalyzers.size()==0)
			return null;
		
		String[] result = new String[availableAnalyzers.size()];
		Iterator<String> iter = availableAnalyzers.keySet().iterator();
		for (int i=0; iter.hasNext(); i++)
			result[i] = iter.next();
		
		return result;
	}

	/**
	 * 
	 */
	private static synchronized void initAvailableAnalyzers(){
		availableAnalyzers = new LinkedHashMap<String,Analyzer>();
		availableAnalyzers.put(StandardAnalyzer.class.getName(), new StandardAnalyzer());
		availableAnalyzers.put(KeywordAnalyzer.class.getName(), new KeywordAnalyzer());
	}
	
	/**
	 * 
	 * @return
	 */
	private static Analyzer getAvailableAnalyzer(String name){
		
		if (availableAnalyzers==null)
			initAvailableAnalyzers();
		
		if (availableAnalyzers==null || availableAnalyzers.size()==0)
			return null;
		else
			return availableAnalyzers.get(name);
	}
}
