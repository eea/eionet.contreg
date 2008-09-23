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
	public static final int DEFAULT_MAX_HITS = 300;
	
	/** */
	private int maxHits = DEFAULT_MAX_HITS;
	
	/**
	 * 
	 * @param document
	 */
	public abstract void collectDocument(Document document);
	
	/**
	 * 
	 * @param hits
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	public void collectHits(Hits hits) throws CorruptIndexException, IOException{
		
		if (hits==null || hits.length()==0)
			return;
		
		for (int i=0; i<hits.length() && i<maxHits; i++){
			collectDocument(hits.doc(i));
		}
	}

	/**
	 * 
	 * @param hits
	 * @return
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public static List<ResourceDTO> collectResourceDTOs(Hits hits) throws CorruptIndexException, IOException{
		return collectResourceDTOs(hits, DEFAULT_MAX_HITS);
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
		return collectMaps(hits, DEFAULT_MAX_HITS);
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
	 * @param collector
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	protected static void collectHits(Hits hits, int maxHits, HitsCollector collector) throws CorruptIndexException, IOException{
		
		if (hits==null || hits.length()==0 || collector==null)
			return;
		
		for (int i=0; i<hits.length() && i<maxHits; i++){
			collector.collectDocument(hits.doc(i));
		}
	}

	/**
	 * @return the maxHits
	 */
	public int getMaxHits() {
		return maxHits;
	}

	/**
	 * @param maxHits the maxHits to set
	 */
	public void setMaxHits(int maxHits) {
		this.maxHits = maxHits;
	}
}
