package eionet.cr.search;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import eionet.cr.common.Identifiers;
import eionet.cr.common.Resource;
import eionet.cr.config.GeneralConfig;
import eionet.cr.index.EncodingSchemes;
import eionet.cr.index.Indexer;
import eionet.cr.index.SubProperties;
import eionet.cr.index.walk.AllDocsWalker;
import eionet.cr.index.walk.EncodingSchemesLoader;
import eionet.cr.index.walk.SubPropertiesLoader;
import eionet.cr.search.util.SearchUtil;
import eionet.cr.search.util.SimpleSearchExpression;
import eionet.cr.util.Util;
import eionet.cr.web.util.display.RodInstrumentDTO;
import eionet.cr.web.util.display.RodObligationDTO;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class Searcher {
	
	/** */
	public static final int MAX_RESULT_SET_SIZE = 300;
	public static final String DEFAULT_FIELD = Indexer.ALL_CONTENT_FIELD;
	
	/** */
	private static Log logger = LogFactory.getLog(Searcher.class);
	
	/** */
	//private static Analyzer defaultAnalyzer = new StandardAnalyzer.class.getName();

	/** */
	private static IndexSearcher indexSearcher = null;
	
	/** */
//	private static LinkedHashMap<String,Analyzer> availableAnalyzers = null;
	
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
		return luceneQuery(expressionObject.toLuceneQueryString(), expressionObject.getAnalyzer());
	}
	
	/**
	 * 
	 * @param queryStr
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static List<Map<String,String[]>> luceneQuery(String queryStr) throws ParseException, IOException{
		return luceneQuery(queryStr, Indexer.getAnalyzer());
	}
	
	/**
	 * 
	 * @param queryStr
	 * @param analyzerName
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 * @throws ParseException
	 */
	public static List<Map<String,String[]>> luceneQuery(String queryStr, String analyzerName) throws CorruptIndexException, IOException, ParseException{
		return luceneQuery(queryStr, SearchUtil.createAnalyzer(analyzerName));
	}

	/**
	 * 
	 * @param queryStr
	 * @param analyzer
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 * @throws ParseException
	 */
	public static List<Map<String,String[]>> luceneQuery(String queryStr, Analyzer analyzer) throws CorruptIndexException, IOException, ParseException{

		if (queryStr==null || queryStr.trim().length()==0)
			return new ArrayList<Map<String,String[]>>();
		
		QueryParser parser = new QueryParser(DEFAULT_FIELD, analyzer);
		Query queryObj = parser.parse(queryStr);
		
		logger.debug("Performing search query: " + queryStr);
		
		IndexSearcher indexSearcher = null;
		try{
			indexSearcher = getIndexSearcher();
			Hits hits = indexSearcher.search(queryObj);
			return SearchUtil.processHits(hits);
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
//	public static String[] listAvailableAnalyzers(){
//		
//		if (availableAnalyzers==null)
//			initAvailableAnalyzers();
//		
//		if (availableAnalyzers==null || availableAnalyzers.size()==0)
//			return null;
//		
//		String[] result = new String[availableAnalyzers.size()];
//		Iterator<String> iter = availableAnalyzers.keySet().iterator();
//		for (int i=0; iter.hasNext(); i++)
//			result[i] = iter.next();
//		
//		return result;
//	}
//
//	/**
//	 * 
//	 */
//	private static synchronized void initAvailableAnalyzers(){
//		availableAnalyzers = new LinkedHashMap<String,Analyzer>();
//		availableAnalyzers.put(StandardAnalyzer.class.getName(), new StandardAnalyzer());
//		availableAnalyzers.put(KeywordAnalyzer.class.getName(), new KeywordAnalyzer());
//	}
	
//	/**
//	 * 
//	 * @return
//	 */
//	public static Analyzer getAvailableAnalyzer(String name){
//		
//		if (availableAnalyzers==null)
//			initAvailableAnalyzers();
//		
//		if (availableAnalyzers==null || availableAnalyzers.size()==0)
//			return null;
//		else
//			return availableAnalyzers.get(name);
//	}
	
	/**
	 * 
	 * @param dataflow
	 * @param locality
	 * @param year
	 * @return
	 * @throws SearchException 
	 */
	public static List<Map<String,String[]>> dataflowSearch(String dataflow, String locality, String year) throws SearchException{
		
		List<Query> queries = new ArrayList<Query>();
		queries.add(new TermQuery(new Term(Identifiers.RDF_TYPE, Identifiers.ROD_DELIVERY_CLASS)));
		
		if (!Util.isNullOrEmpty(dataflow))
			queries.add(new TermQuery(new Term(Identifiers.ROD_OBLIGATION_PROPERTY, dataflow)));
		
		IndexSearcher indexSearcher = null;
		try{
			if (!Util.isNullOrEmpty(locality)){
				QueryParser parser = new QueryParser(DEFAULT_FIELD, Indexer.getAnalyzer());
				StringBuffer qryBuf = new StringBuffer(Util.escapeForLuceneQuery(Identifiers.ROD_LOCALITY_PROPERTY));
				qryBuf.append(":\"").append(Util.escapeForLuceneQuery(locality)).append("\"");
				queries.add(parser.parse(qryBuf.toString()));
			}
			if (!Util.isNullOrEmpty(year)){
				QueryParser parser = new QueryParser(DEFAULT_FIELD, Indexer.getAnalyzer());
				StringBuffer qryBuf = new StringBuffer(Util.escapeForLuceneQuery(Identifiers.DC_COVERAGE));
				qryBuf.append(":\"").append(Util.escapeForLuceneQuery(year)).append("\"");
				queries.add(parser.parse(qryBuf.toString()));
			}

			Query query;
			if (queries.size()==1)
				query = queries.get(0);
			else{
				query = new BooleanQuery();
				for (int i=0; i<queries.size(); i++)
					((BooleanQuery)query).add(queries.get(i), BooleanClause.Occur.MUST);
			}

			indexSearcher = getIndexSearcher();
			Hits hits = indexSearcher.search(query);
			return SearchUtil.processHits(hits);
		}
		catch (Exception e){
			throw new SearchException(e.toString(), e);
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
	 * @throws SearchException
	 */
	public static List<RodInstrumentDTO> getDataflowsGroupedByInstruments() throws SearchException{

		StringBuffer qryBuf = new StringBuffer(Util.escapeForLuceneQuery(Identifiers.RDF_TYPE));
		qryBuf.append(":\"").append(Util.escapeForLuceneQuery(Identifiers.ROD_OBLIGATION_CLASS)).append("\"");
		
		logger.debug("Performing search query: " + qryBuf.toString());
		
		Map<String,RodInstrumentDTO> instrumentsMap = new HashMap<String,RodInstrumentDTO>();
		IndexSearcher indexSearcher = null;
		try{
			indexSearcher = getIndexSearcher();
			
			QueryParser parser = new QueryParser(DEFAULT_FIELD, new KeywordAnalyzer());
			Query queryObj = parser.parse(qryBuf.toString());

			Hits hits = indexSearcher.search(queryObj);
			for (int i=0; hits!=null && i<hits.length(); i++){
				
				Document doc = hits.doc(i);
				String instrumentId = doc.get(Identifiers.ROD_INSTRUMENT_PROPERTY);
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
		catch (Exception e){
			throw new SearchException(e.toString(), e);
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
	 * @param fieldName
	 * @return
	 * @throws SearchException
	 */
	public static Set<String> getLiteralFieldValues(String fieldName) throws SearchException{
		
		Set<String> resultSet = new HashSet<String>();
		if (fieldName==null || fieldName.length()==0)
			return resultSet;
		
		String indexLocation = GeneralConfig.getProperty(GeneralConfig.LUCENE_INDEX_LOCATION);
		IndexReader indexReader = null;
		try{			
			if (IndexReader.indexExists(indexLocation)){
				indexReader = IndexReader.open(indexLocation);
				
				List<String> subProperties = SubProperties.getSubPropertiesOf(fieldName);
				if (subProperties==null)
					subProperties = new ArrayList<String>();
				subProperties.add(0, fieldName);
				
				String[] fields = subProperties.toArray(new String[0]);
				FieldSelector fieldSelector = new MapFieldSelector(fields);
				
				int numDocs = indexReader.numDocs();
				for (int docIndex=0; docIndex<numDocs; docIndex++){
					Document document = indexReader.document(docIndex, fieldSelector);
					if (document!=null){
						for (int fldIndex=0; fldIndex<fields.length; fldIndex++){
							String[] values = document.getValues(fields[fldIndex]);
							if (values!=null && values.length>0){
								for (int j=0; j<values.length; j++){
									if (!Util.isURL(values[j]))
										resultSet.add(values[j]);
								}
							}
						}
					}
				}
				
			}
			else{
				logger.info("Index does not exist at " + indexLocation);
			}
			
		}
		catch (Exception e){
			throw new SearchException(e.toString(), e);
		}
		finally{
			try{
				if (indexReader!=null) indexReader.close();
			}
			catch (Exception e){
				logger.error("Failed to close index reader: " + e.toString(), e);
			}
		}
		
		return new TreeSet(resultSet);
	}
	
	/**
	 * 
	 * @param uri
	 * @return
	 * @throws SearchException 
	 */
	public static Resource getResourceByUri(String uri) throws SearchException{
		
		if (uri==null || uri.trim().length()==0)
			return null;
		
		IndexSearcher indexSearcher = null;
		try{
			indexSearcher = getIndexSearcher();
			Hits hits = indexSearcher.search(new TermQuery(new Term(Identifiers.DOC_ID, uri)));
			if (hits==null || hits.length()==0)
				return null;
			else
				return new Resource(hits.doc(0));
		}
		catch (Exception e){
			throw new SearchException(e.toString(), e);
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
	 * @param args
	 */
	public static void main(String[] args){
		
		try {
//			List<Map<String,String[]>> list = Searcher.dataflowSearch("http://rod.eionet.eu.int/obligations/32", "Albania", "2005");
//			System.out.println(list==null ? "list==null" : "list.size()=" + list.size());
//			for (int i=0; list!=null && i<list.size(); i++){
//				System.out.println(list.get(i));
//			}
			
			AllDocsWalker.startupWalk();
			//Resource resource = Searcher.getResourceByUri("http://cdr.eionet.europa.eu/es/eea/ewn1/envrudyww");
			Resource resource = Searcher.getResourceByUri("http://cdr.eionet.europa.eu/ro/eu/ghgmm/envr4yo9w");			
			System.out.println(resource);
			System.out.println("==================================");
			List list = resource.getPropertiesForFactsheet();
			for (int i=0; list!=null && i<list.size(); i++)
				System.out.println(list.get(i).toString());
			
//			List<RodInstrumentDTO> list = Searcher.getDataflowsGroupedByInstruments();
//			for (int i=0; list!=null && i<list.size(); i++){
//				System.out.println(list.get(i));
//			}
//			Set<String> resultSet = Searcher.getLiteralFieldValues("http://www.w3.org/2000/01/rdf-schema#label");
//			for (Iterator i=resultSet.iterator(); i.hasNext(); ){
//				System.out.println(i.next());
//			}
			
			System.out.println();
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
