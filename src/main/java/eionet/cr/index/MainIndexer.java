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
import org.apache.lucene.store.LockObtainFailedException;

import eionet.cr.config.GeneralConfig;
import eionet.cr.harvest.DefaultHarvestListener;
import eionet.cr.harvest.util.RDFResource;
import eionet.cr.harvest.util.RDFResourceProperty;

/**
 * 
 * @author heinljab
 *
 */
public class MainIndexer extends Indexer{
	
	/** */
	private static Log logger = LogFactory.getLog(MainIndexer.class);
	
	/** */
	private IndexWriter indexWriter = null;
	
	/** */
	private int countDocumentsIndexed;
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.index.Indexer#indexRDFResource(eionet.cr.harvest.util.RDFResource)
	 */
	public void indexRDFResource(RDFResource resource) throws IndexException{
		
		if (resource==null || resource.getId()==null || resource.getId().trim().length()==0)
			return;
		
		// we have a resource that has at least an ID, so we have something to index,
		// so we initialize index if not initialize already
		if (indexWriter==null)
			initIndexWriter();
		
		Document document = new Document();
		document.add(new Field("ID", resource.getId(), Field.Store.YES, Field.Index.UN_TOKENIZED));

		StringBuffer contentBuf = new StringBuffer(); // for collecting all literal values
		
		List<RDFResourceProperty> resourceProperties = resource.getProperties();
		for (int i=0; resourceProperties!=null && i<resourceProperties.size(); i++){
			
			RDFResourceProperty resourceProperty = resourceProperties.get(i);
			document.add(new Field(resourceProperty.getId(), resourceProperty.getValue(), Field.Store.YES, Field.Index.TOKENIZED));
			
			if (resourceProperty.isLiteral())
				contentBuf.append(resourceProperty.getValue());
			
			// if this is a non-literal attribute, find decoded labels for it, and add them to the document
			if (!resourceProperty.isLiteral() && resourceProperty.isValueURL()){
				
				String[] decodedLabels = EncodingSchemes.getLabels(resourceProperty.getValue());
				for (int j=0; decodedLabels!=null && j<decodedLabels.length; j++)
					document.add(new Field(resourceProperty.getId(), decodedLabels[j], Field.Store.YES, Field.Index.TOKENIZED));
			}
		}
		
		// create the so called "content" field, add it to the document
		if (contentBuf.toString().trim().length()>0)
			document.add(new Field(Indexer.ALL_CONTENT_FIELD, contentBuf.toString(), Field.Store.NO, Field.Index.TOKENIZED));
		
		// if the resource is an encoding scheme, set the corresponding field
		boolean isEncScheme = resource.isEncodingScheme();
		if (isEncScheme)
			document.add(new Field("IS_ENCODING_SCHEME", Boolean.TRUE.toString(), Field.Store.YES, Field.Index.TOKENIZED));
		
		// finally, update the document in index
		try {
			indexWriter.updateDocument(new Term("ID", resource.getId()), document);
		}
		catch (Exception e) {
			throw new IndexException(e.toString(), e);
		}
		countDocumentsIndexed++;
		
		// if this document is an encoding scheme, add it into EncodingSchemes
		if (isEncScheme)
			EncodingSchemes.update(resource.getId(), document.getValues("http://www.w3.org/2000/01/rdf-schema#label"));
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.index.Indexer#close()
	 */
	public void close() throws CorruptIndexException, IOException{
		
		logger.debug("Closing index writer");
		
		if (indexWriter!=null){
			indexWriter.optimize();
			indexWriter.close();
		}
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
			indexWriter = new IndexWriter(indexLocation, analyzer);
		}
		catch (Exception e){
			throw new IndexException(e.toString(), e);
		}
	}
}
