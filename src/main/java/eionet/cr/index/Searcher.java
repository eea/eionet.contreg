package eionet.cr.index;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import eionet.cr.index.util.RodInstrumentDTO;
import eionet.cr.index.util.RodObligationDTO;
import eionet.cr.index.walk.AllDocsWalker;
import eionet.cr.index.walk.EncodingSchemesLoader;
import eionet.cr.index.walk.SubPropertiesLoader;
import eionet.cr.util.Identifiers;
import eionet.cr.util.Messages;
import eionet.cr.util.SimpleSearchExpression;
import eionet.cr.util.Util;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class Searcher {
	
	/** */
	public static final int MAX_RESULT_SET_SIZE = 300;
	
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
	public static List<Map<String,String[]>> simpleSearch(String searchExpression) throws ParseException, IOException{
		
		if (searchExpression==null || searchExpression.trim().length()==0)
			return new ArrayList<Map<String,String[]>>();
		
		SimpleSearchExpression expressionObject = new SimpleSearchExpression(searchExpression);
		return luceneQuery(expressionObject.toLuceneQueryString(), expressionObject.getAnalyzerName());
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static List<Map<String,String[]>> luceneQuery(String query) throws ParseException, IOException{
		return luceneQuery(query, defaultAnalyzer);
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
	public static List<Map<String,String[]>> luceneQuery(String query, String analyzerName) throws CorruptIndexException, IOException, ParseException{

		if (query==null || query.trim().length()==0)
			return new ArrayList<Map<String,String[]>>();
		
		QueryParser parser = new QueryParser(DEFAULT_FIELD, getAvailableAnalyzer(analyzerName));
		Query queryObj = parser.parse(query);
		
		logger.debug("Performing search query: " + query);
		
		List<Map<String,String[]>> list = new ArrayList<Map<String,String[]>>();
		IndexSearcher indexSearcher = null;
		try{
			indexSearcher = getIndexSearcher();
			Hits hits = indexSearcher.search(queryObj);
			for (int i=0; hits!=null && i<hits.length() && i<Searcher.MAX_RESULT_SET_SIZE; i++){
				
				Document doc = hits.doc(i);
				Map<String,String[]> map = new Hashtable<String,String[]>();
				List allFields = doc.getFields();
				
				for (int j=0; allFields!=null && j<allFields.size(); j++){
					Fieldable field = (Fieldable)allFields.get(j);
					String fieldName = field.name();
					String[] fieldValues = doc.getValues(fieldName);
					if (fieldValues!=null && fieldValues.length>0)
						map.put(field.name(), fieldValues);
				}
				
				list.add(map);
			}
			
			return list;
		}
		finally{
			try{
				if (indexSearcher!=null)
					indexSearcher.close();
			}
			catch (IOException e){}
		}
	}
	
	/**
	 * 
	 * @param sourceUrl
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public static int getNumDocsBySourceUrl(String sourceUrl) throws CorruptIndexException, IOException{
		
		if (sourceUrl==null || sourceUrl.length()==0)
			return 0;

		IndexSearcher indexSearcher = null;
		try{
			indexSearcher = getIndexSearcher();
			return indexSearcher.docFreq(new Term(Identifiers.SOURCE_ID, sourceUrl));
		}
		finally{
			try{
				if (indexSearcher!=null)
					indexSearcher.close();
			}
			catch (IOException e){}
		}
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
	public static Analyzer getAvailableAnalyzer(String name){
		
		if (availableAnalyzers==null)
			initAvailableAnalyzers();
		
		if (availableAnalyzers==null || availableAnalyzers.size()==0)
			return null;
		else
			return availableAnalyzers.get(name);
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 * @throws ParseException 
	 */
	public static List<RodInstrumentDTO> getDataflowsGroupedByInstruments() throws CorruptIndexException, IOException, ParseException{

		StringBuffer qryBuf = new StringBuffer(Util.escapeForLuceneQuery(Identifiers.RDF_TYPE));
		qryBuf.append(":\"").append(Util.escapeForLuceneQuery(Identifiers.ROD_OBLIGATION)).append("\"");
		
		QueryParser parser = new QueryParser(DEFAULT_FIELD, getAvailableAnalyzer(defaultAnalyzer));
		Query queryObj = parser.parse(qryBuf.toString());
		
		logger.debug("Performing search query: " + qryBuf.toString());
		
		Map<String,RodInstrumentDTO> instrumentsMap = new HashMap<String,RodInstrumentDTO>();
		IndexSearcher indexSearcher = null;
		try{
			indexSearcher = getIndexSearcher();
			Hits hits = indexSearcher.search(queryObj);
			for (int i=0; hits!=null && i<hits.length(); i++){
				
				Document doc = hits.doc(i);
				String instrumentId = doc.get(Identifiers.ROD_INSTRUMENT);
				if (instrumentId!=null && instrumentId.length()>0){
					String instrumentLabel = EncodingSchemes.getLabel(instrumentId);
					if (instrumentLabel!=null && instrumentLabel.length()>0){
						String obligationLabel = doc.get(Identifiers.RDFS_LABEL);
						if (obligationLabel==null || obligationLabel.length()==0)
							obligationLabel = doc.get(Identifiers.DC_TITLE);
						
						if (obligationLabel!=null && obligationLabel.length()>0){
							String obligationId = doc.get(Identifiers.DOC_ID);
							if (obligationId!=null && obligationId.length()>0){
								
								RodInstrumentDTO instrumentDTO = instrumentsMap.get(instrumentId);
								if (instrumentDTO==null)
									instrumentDTO = new RodInstrumentDTO(instrumentId, instrumentLabel);
								instrumentDTO.addObligation(new RodObligationDTO(obligationId, obligationLabel));
								instrumentsMap.put(instrumentId, instrumentDTO);
							}
						}
					}
				}
			}
		}
		finally{
			try{
				if (indexSearcher!=null)
					indexSearcher.close();
			}
			catch (IOException e){}
		}
		
		if (instrumentsMap.isEmpty())
			return null;
		else{
			List<RodInstrumentDTO> resultList = new ArrayList(instrumentsMap.values());
			Collections.sort(resultList);
			return resultList;
		}
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		
		try {
			AllDocsWalker.startupWalk();
			
//			List<RodInstrumentDTO> instruments = Searcher.getDataflowsGroupedByInstruments();
//			for (int i=0; instruments!=null && i<instruments.size(); i++){
//				System.out.println(instruments.get(i).toString());
//			}
			System.out.println();
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
