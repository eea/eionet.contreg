package eionet.cr.index;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

import eionet.cr.config.GeneralConfig;
import eionet.cr.harvest.util.RDFResource;
import eionet.cr.harvest.util.RDFResourceProperty;
import eionet.cr.util.Identifiers;
import eionet.cr.util.Util;

/**
 * 
 * @author heinljab
 *
 */
public class MainIndexer extends Indexer{
	
	/** */
	private static final boolean AUTO_COMMIT = true;
	private static final boolean NO_AUTO_COMMIT = false;
	
	/** */
	private static Log logger = LogFactory.getLog(MainIndexer.class);
	
	/** */
	private IndexWriter indexWriter = null;
	
	/** */
	private int countDocumentsIndexed = 0;
	
	/** */
	private String firstSeenTimestamp = null;
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.index.Indexer#indexRDFResource(eionet.cr.harvest.util.RDFResource)
	 */
	public void indexRDFResource(RDFResource resource) throws IndexException{
		
		if (resource==null || resource.getId()==null || resource.getId().trim().length()==0)
			return;
		
		if (indexWriter==null){
			initIndexWriter();
			try{
				logger.debug("Clearing index for source: " + resource.getSourceId());
				indexWriter.deleteDocuments(new Term(Identifiers.SOURCE_ID, resource.getSourceId()));
			}
			catch (Exception e){
				throw new IndexException(e.toString(), e);
			}
		}
		
		if (countDocumentsIndexed==0)
			logger.debug("Indexing resources, source URL = " + resource.getSourceId());
		
		Document document = new Document();		
		document.add(new Field(Identifiers.DOC_ID, resource.getId(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		document.add(new Field(Identifiers.SOURCE_ID, resource.getSourceId(), Field.Store.YES, Field.Index.UN_TOKENIZED));

		StringBuffer contentBuf = new StringBuffer(); // for collecting all literal values
		
		List<RDFResourceProperty> properties = resource.getProperties();
		for (int i=0; properties!=null && i<properties.size(); i++){
			
			RDFResourceProperty property = properties.get(i);
			String fieldValue = property.getValue().trim();
			if (fieldValue.length()>0){
				
				String fieldName = property.getId();
				document.add(new Field(fieldName, fieldValue, Field.Store.YES, Field.Index.TOKENIZED));
				
				if (property.isLiteral())
					contentBuf.append(fieldValue).append(" ");
				
				// if this is a non-literal attribute, find decoded labels for it, and add them to the document
				if (!property.isLiteral() && property.isValueURL()){
					
					String[] decodedLabels = EncodingSchemes.getLabels(property.getValue());
					for (int j=0; decodedLabels!=null && j<decodedLabels.length; j++)
						document.add(new Field(fieldName, decodedLabels[j], Field.Store.YES, Field.Index.TOKENIZED));
				}
			}
		}
		
		// create the field that contains all literal content, add it to the document
		String trimmedContentBuf = contentBuf.toString().trim();
		if (trimmedContentBuf.length()>0)
			document.add(new Field(Indexer.ALL_CONTENT_FIELD, trimmedContentBuf, Field.Store.NO, Field.Index.TOKENIZED));
		
		// if the resource is an encoding scheme, set the corresponding field
		boolean isEncScheme = resource.isEncodingScheme();
		if (isEncScheme)
			document.add(new Field(Identifiers.IS_ENCODING_SCHEME, Boolean.TRUE.toString(), Field.Store.YES, Field.Index.TOKENIZED));
		
		// set the time the document was first seen
		document.add(new Field(Identifiers.FIRST_SEEN_TIMESTAMP,
				resource.getFirstSeenTimestamp()==null ? this.getFirstSeenTimestamp() : resource.getFirstSeenTimestamp(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		
		// finally, update the document in index
		try {
			indexWriter.updateDocument(new Term(Identifiers.DOC_ID, resource.getId()), document);
		}
		catch (Exception e) {
			throw new IndexException(e.toString(), e);
		}
		countDocumentsIndexed++;
		
		// if this document is an encoding scheme, add it into EncodingSchemes
		if (isEncScheme)
			EncodingSchemes.update(resource.getId(), document.getValues(Identifiers.RDFS_LABEL));
	}

	/**
	 * @throws IOException 
	 * @throws LockObtainFailedException 
	 * @throws CorruptIndexException 
	 * 
	 */
	private void initIndexWriter() throws IndexException{
		
		logger.debug("Initializing index writer");
		
		try{
			Analyzer analyzer = new StandardAnalyzer();
			String indexLocation = GeneralConfig.getProperty(GeneralConfig.LUCENE_INDEX_LOCATION);
			indexWriter = new IndexWriter(FSDirectory.getDirectory(indexLocation), NO_AUTO_COMMIT, analyzer);
		}
		catch (Exception e){
			throw new IndexException(e.toString(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.index.Indexer#close()
	 */
	public void close() throws CorruptIndexException, IOException{
		
		logger.debug("Closing index writer");
		
		if (indexWriter!=null){
			indexWriter.flush();
			indexWriter.optimize();
			indexWriter.close();
		}
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void abort() throws IOException {

		logger.debug("Aborting index writer");
		
		if (indexWriter!=null){
			indexWriter.abort();
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private String getFirstSeenTimestamp(){
		
		if (this.firstSeenTimestamp==null)
			this.firstSeenTimestamp = String.valueOf((int)(System.currentTimeMillis() / (long)1000));
		return this.firstSeenTimestamp;
	}
}
