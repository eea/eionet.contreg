package eionet.cr.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
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
	private static Logger logger = Logger.getLogger(Searcher.class);
	
	/** */
	private String indexLocation = null;
	private Analyzer analyzer = null;
	
	/**
	 *
	 */
	public Searcher(){
		this(GeneralConfig.getProperty(GeneralConfig.LUCENE_INDEX_LOCATION));
	}

	/**
	 * 
	 * @param indexLocation
	 */
	public Searcher(String indexLocation){
		this.indexLocation= indexLocation;
		this.analyzer = new StandardAnalyzer();
	}

	/**
	 * 
	 * @param query
	 * @return
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public List search(String query) throws ParseException, IOException{

		IndexSearcher indexSearcher = null;
		try{
			indexSearcher = new IndexSearcher(indexLocation);
			QueryParser parser = new QueryParser("content", analyzer);
			Query queryObj = parser.parse(query);
			Hits hits = indexSearcher.search(queryObj);
			return processHits(hits);
		}
		finally{
			try{
				if (indexSearcher!=null) indexSearcher.close();
			}
			catch (IOException e){
				logger.error("Failed to close IndexSearcher", e);
			}
		}
	}
	
	/**
	 * 
	 * @param req
	 * @return
	 * @throws Exception 
	 */
	public static void search(HttpServletRequest req) throws Exception{
		
		String query = req.getParameter("query");
		if (query==null || query.trim().length()==0)
			throw new Exception("Missing or empty query");
		String analyzerClass = req.getParameter("analyzerClass");
		
		
		Searcher searcher = new Searcher();
		if (analyzerClass!=null && analyzerClass.trim().length()>0)
			searcher.setAnalyzer((Analyzer)Class.forName(analyzerClass).newInstance());
		
		List hits = searcher.search(query);
		Messages.addMessage(req, "messages", "Number of hits: " + (hits==null ? "0" : hits.size()));
		if (hits!=null)
			req.setAttribute("hits", hits);
	}
	
	/**
	 * @param analyzer The analyzer to set.
	 */
	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}
	
	/**
	 * 
	 * @param hits
	 * @return
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	public static List processHits(Hits hits) throws CorruptIndexException, IOException{
		
		List list = new ArrayList();
		for (int i=0; hits!=null && i<hits.length(); i++){
			
			Document doc = hits.doc(i);
			Hashtable hash = new Hashtable();
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
