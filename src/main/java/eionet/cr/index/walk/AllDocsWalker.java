package eionet.cr.index.walk;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import eionet.cr.config.GeneralConfig;
import eionet.cr.util.DocumentListener;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class AllDocsWalker {

	/** */
	private List<DocumentListener> listeners = new ArrayList<DocumentListener>();
	
	/** */
	private static Log logger = LogFactory.getLog(AllDocsWalker.class);
	
	/**
	 * 
	 */
	public AllDocsWalker(){
	}
	
	/**
	 * 
	 * @param listener
	 */
	public void addListener(DocumentListener listener){
		
		if (listener==null)
			return;
		else
			listeners.add(listener);
		
		logger.debug("Listener added to " + getClass().getName() + ": " + listener.getClass().getName());
	}
	
	/**
	 * 
	 */
	public void walk(){
		
		if (listeners==null || listeners.size()==0){
			logger.warn("No point in walking through documents if no listeners added");
			return;
		}
		
		String indexLocation = GeneralConfig.getProperty(GeneralConfig.LUCENE_INDEX_LOCATION);
		
		logger.debug("Walking through all documents of index at the following location: " + indexLocation);
		
		IndexReader indexReader = null;
		try{			
			if (IndexReader.indexExists(indexLocation)){
				indexReader = IndexReader.open(indexLocation);
				int numDocs = indexReader.numDocs();
				for (int i=0; i<numDocs; i++){
					Document document = indexReader.document(i);
					if (document!=null)
						notifyListenersOfDocument(document);
				}
				notifyListenersOfDone();
				
				logger.debug("Walked through " + numDocs + " documents");
			}
			else{
				logger.info("Walker found that index does not yet exist at " + indexLocation);
			}
			
		}
		catch (Throwable t){
			logger.error("Failed to walk through documents: " + t.toString(), t);
		}
		finally{
			try{
				if (indexReader!=null)
					indexReader.close();
			}
			catch (Exception e){
				logger.error("Failed to close index reader: " + e.toString(), e);
			}
		}
	}

	/**
	 * 
	 * @param document
	 */
	private void notifyListenersOfDocument(Document document){
		
		for (int i=0; i<listeners.size(); i++)
			listeners.get(i).handleDocument(document);
	}
	
	/**
	 * 
	 */
	private void notifyListenersOfDone(){
		for (int i=0; i<listeners.size(); i++)
			listeners.get(i).done();
	}
	
	/**
	 * 
	 */
	public static void startupWalk(){
		
		AllDocsWalker walker = new AllDocsWalker();
		walker.addListener(new EncodingSchemesLoader());
		walker.addListener(new SubPropertiesLoader());
		walker.walk();
	}
}
