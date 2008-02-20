package eionet.cr.index;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;

import eionet.cr.config.GeneralConfig;
import eionet.cr.harvest.util.RDFResource;

/**
 * 
 * @author heinljab
 *
 */
public class MainIndexer extends Indexer{
	
	/** */
	private IndexWriter indexWriter = null;

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.index.Indexer#indexRDFResource(eionet.cr.harvest.util.RDFResource)
	 */
	public void indexRDFResource(RDFResource resource) throws IndexException{
		
		if (indexWriter==null)
			initIndexWriter();
		
		// ...
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.index.Indexer#close()
	 */
	public void close() throws IndexException{
		
		try{
			if (indexWriter!=null){
				indexWriter.optimize();
				indexWriter.close();
			}
		}
		catch (Exception e){
			throw new IndexException(e.toString(), e);
		}
	}

	/**
	 * @throws IOException 
	 * @throws LockObtainFailedException 
	 * @throws CorruptIndexException 
	 * 
	 */
	private void initIndexWriter() throws IndexException{
		
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
