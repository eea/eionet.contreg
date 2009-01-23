package eionet.cr.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;

import eionet.cr.common.EncodingSchemes;
import eionet.cr.common.Predicates;
import eionet.cr.common.SubProperties;
import eionet.cr.common.Subjects;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.dto.RodInstrumentDTO;
import eionet.cr.dto.RodObligationDTO;
import eionet.cr.search.util.HitsCollector;
import eionet.cr.search.util.SearchUtil;
import eionet.cr.util.URIUtil;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class Searcher {
	
	public static final String DEFAULT_FIELD = Searcher.ALL_CONTENT_FIELD;
	
	/** */
	private static Log logger = LogFactory.getLog(Searcher.class);
	
	/**
	 * 
	 * @return
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	private static IndexSearcher getIndexSearcher() throws CorruptIndexException, IOException{
		String indexLocation = GeneralConfig.getRequiredProperty(GeneralConfig.LUCENE_INDEX_LOCATION);
		return new IndexSearcher(indexLocation);
	}
	
	/**
	 * 
	 * @param query
	 * @return
	 * @throws SearchException 
	 * @throws ParseException
	 * @throws IOException
	 */
	public static List<SubjectDTO> simpleSearch(String expression) throws SearchException{
		
		if (expression==null || expression.trim().length()==0)
			return null;
		else
			expression = expression.trim();
		
		Query query = null;
		IndexSearcher indexSearcher = null;
		try{
			if (URIUtil.isSchemedURI(expression.trim()))
				query = new TermQuery(new Term(Predicates.DOC_ID, expression));
			else{
				QueryParser parser = new QueryParser(DEFAULT_FIELD, Searcher.getAnalyzer());
				char[] escapeExceptions = {'"'};
				query = parser.parse(Util.luceneEscape(expression, escapeExceptions));
			}
					
			indexSearcher = getIndexSearcher();
			return HitsCollector.collectResourceDTOs(indexSearcher.search(query));
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
	 * @param queryStr
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static List<Map<String,String[]>> luceneQuery(String queryStr) throws ParseException, IOException{
		return luceneQuery(queryStr, Searcher.getAnalyzer());
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
			return HitsCollector.collectMaps(hits);
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
			return indexSearcher.docFreq(new Term(Predicates.SOURCE_ID, sourceUrl));
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
	public static List<SubjectDTO> dataflowSearch(String dataflow, String locality, String year) throws SearchException{
		
		List<Query> queries = new ArrayList<Query>();
		queries.add(new TermQuery(new Term(Predicates.RDF_TYPE, Subjects.ROD_DELIVERY_CLASS)));
		
		if (!Util.isNullOrEmpty(dataflow))
			queries.add(new TermQuery(new Term(Predicates.ROD_OBLIGATION_PROPERTY, dataflow)));
		
		IndexSearcher indexSearcher = null;
		try{
			if (!Util.isNullOrEmpty(locality)){
				QueryParser parser = new QueryParser(DEFAULT_FIELD, Searcher.getAnalyzer());
				StringBuffer qryBuf = new StringBuffer(Util.luceneEscape(Predicates.ROD_LOCALITY_PROPERTY));
				qryBuf.append(":\"").append(Util.luceneEscape(locality)).append("\"");
				queries.add(parser.parse(qryBuf.toString()));
			}
			if (!Util.isNullOrEmpty(year)){
				QueryParser parser = new QueryParser(DEFAULT_FIELD, Searcher.getAnalyzer());
				StringBuffer qryBuf = new StringBuffer(Util.luceneEscape(Predicates.DC_COVERAGE));
				qryBuf.append(":\"").append(Util.luceneEscape(year)).append("\"");
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
			return HitsCollector.collectResourceDTOs(indexSearcher.search(query));
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

		StringBuffer qryBuf = new StringBuffer(Util.luceneEscape(Predicates.RDF_TYPE));
		qryBuf.append(":\"").append(Util.luceneEscape(Subjects.ROD_OBLIGATION_CLASS)).append("\"");
		
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
				String instrumentId = doc.get(Predicates.ROD_INSTRUMENT_PROPERTY);
				if (instrumentId!=null && instrumentId.length()>0){
					String instrumentLabel = EncodingSchemes.getLabel(instrumentId);
					if (instrumentLabel!=null && instrumentLabel.length()>0){
						String obligationLabel = doc.get(Predicates.RDFS_LABEL);
						if (obligationLabel==null || obligationLabel.length()==0)
							obligationLabel = doc.get(Predicates.DC_TITLE);
						
						if (obligationLabel!=null && obligationLabel.length()>0){
							String obligationId = doc.get(Predicates.DOC_ID);
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
				subProperties.add(0,fieldName);
				
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
									if (!URLUtil.isURL(values[j]))
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
		
		return new TreeSet(resultSet); // TreeSet sorts the results into ascending order
	}
	
	/**
	 * 
	 * @param uri
	 * @return
	 * @throws SearchException 
	 */
	public static SubjectDTO getResourceByUri(String uri) throws SearchException{
		
		if (uri==null || uri.trim().length()==0)
			return null;
		
		IndexSearcher indexSearcher = null;
		try{
			indexSearcher = getIndexSearcher();
			Hits hits = indexSearcher.search(new TermQuery(new Term(Predicates.DOC_ID, uri)));
			if (hits==null || hits.length()==0)
				return null;
			else
				return new SubjectDTO("", false); //return new SubjectDTO(hits.doc(0));
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
	 * @param rdfType
	 * @return
	 * @throws SearchException 
	 */
	public static List<SubjectDTO> getRecentByRdfType(String rdfType, int maxResults) throws SearchException{
		
		if (rdfType==null || rdfType.trim().length()==0)
			return null;
		
		IndexSearcher indexSearcher = null;
		try{
			indexSearcher = getIndexSearcher();
			Sort sort = new Sort(Predicates.FIRST_SEEN_TIMESTAMP, true);
			Hits hits = indexSearcher.search(new TermQuery(new Term(Predicates.RDF_TYPE, rdfType)), sort);
			if (hits==null || hits.length()==0)
				hits = indexSearcher.search(new TermQuery(new Term(Predicates.RDF_TYPE, rdfType)));
			
			return HitsCollector.collectResourceDTOs(hits, maxResults);
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
	 * @param criteria
	 * @return
	 * @throws SearchException 
	 */
	public static void customSearch(Map<String,String> criteria, boolean useSubProperties, HitsCollector collector) throws SearchException{
		
		if (criteria==null || criteria.isEmpty())
			return;
		
		IndexSearcher indexSearcher = null;
		List<Query> queries = new ArrayList<Query>();
		QueryParser queryParser = new QueryParser(DEFAULT_FIELD, Searcher.getAnalyzer());
		try{
			for (Iterator<String> propertyIter = criteria.keySet().iterator(); propertyIter.hasNext();){
				String property = propertyIter.next();
				String propertyValue = criteria.get(property);
				if (!Util.isNullOrEmpty(property) && !Util.isNullOrEmpty(propertyValue)){
					
					property = property.trim();
					propertyValue = propertyValue.trim();
					String propertyValueUnquoted =
						propertyValue.startsWith("\"") && propertyValue.endsWith("\"") && propertyValue.length()>2 ?
								propertyValue.substring(1, propertyValue.length()-1) : propertyValue;
					
					List<String> subProperties = useSubProperties ? SubProperties.getSubPropertiesOf(property) : null;
					if (subProperties==null)
						subProperties = new ArrayList<String>();
					subProperties.add(0,property);
					
					BooleanQuery booleanQuery = new BooleanQuery();
					for (int j=0; j<subProperties.size(); j++){

						Query query = null;
						String subProperty = subProperties.get(j);
						if (!Searcher.isAnalyzedField(subProperty) || !Searcher.isAnalyzedValue(propertyValueUnquoted)){
							query = new TermQuery(new Term(subProperty, propertyValueUnquoted));
						}
						else{
							StringBuffer buf = new StringBuffer(Util.luceneEscape(subProperty));
							buf.append(":");
							if (propertyValueUnquoted.equals(propertyValue))
								buf.append(Util.luceneEscape(propertyValue));
							else
								buf.append("\"").append(Util.luceneEscape(propertyValueUnquoted)).append("\"");
							
							query = queryParser.parse(buf.toString());
						}
						
						booleanQuery.add(query, BooleanClause.Occur.SHOULD);
					}
					
					queries.add(booleanQuery);
				}
			}
			
			if (!queries.isEmpty()){
				
				BooleanQuery booleanQuery = new BooleanQuery();
				for (int i=0; i<queries.size(); i++){
					booleanQuery.add(queries.get(i), BooleanClause.Occur.MUST);
				}
				
				indexSearcher = getIndexSearcher();
				collector.collectHits(indexSearcher.search(booleanQuery));
			}
			else
				return;
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
			System.out.println();
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}

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
