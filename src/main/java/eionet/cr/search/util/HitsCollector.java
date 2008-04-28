package eionet.cr.search.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.Hits;

import eionet.cr.common.ResourceDTO;
import eionet.cr.util.DocumentListener;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class HitsCollector {
	
	/** */
	public static final int MAX_HITS = 300;

	/**
	 * 
	 * @param hits
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public static List<ResourceDTO> collectResourceDTOs(Hits hits) throws CorruptIndexException, IOException{
		return collectResourceDTOs(hits, MAX_HITS);
	}
	
	/**
	 * 
	 * @param hits
	 * @param maxHits
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public static List<ResourceDTO> collectResourceDTOs(Hits hits, int maxHits) throws CorruptIndexException, IOException{
		ResourceDTOCollector collector = new ResourceDTOCollector();
		collectHits(hits, maxHits, collector);
		return collector.getResultList();
	}

	/**
	 * 
	 * @param hits
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public static List<Map<String,String[]>> collectMaps(Hits hits) throws CorruptIndexException, IOException{
		return collectMaps(hits, MAX_HITS);
	}
	
	/**
	 * 
	 * @param hits
	 * @param maxHits
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public static List<Map<String,String[]>> collectMaps(Hits hits, int maxHits) throws CorruptIndexException, IOException{
		PlainMapCollector collector = new PlainMapCollector();
		collectHits(hits, maxHits, collector);
		return collector.getResultList();
	}

	/**
	 * 
	 * @param hits
	 * @param maxHits
	 * @param docListener
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	protected static void collectHits(Hits hits, int maxHits, DocumentListener docListener) throws CorruptIndexException, IOException{
		
		if (hits==null || hits.length()==0 || docListener==null)
			return;
		
		for (int i=0; i<hits.length() && i<maxHits; i++){
			docListener.handleDocument(hits.doc(i));
		}
		
		docListener.done();
	}
}
