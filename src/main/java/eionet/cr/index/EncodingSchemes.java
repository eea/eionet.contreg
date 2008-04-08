package eionet.cr.index;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;

import eionet.cr.config.GeneralConfig;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.util.RDFResource;
import eionet.cr.util.Identifiers;
import eionet.cr.util.Util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class EncodingSchemes extends Hashtable<String,String[]>{
	
	/** */
	private static Log logger = LogFactory.getLog(EncodingSchemes.class);
	
	/** */
	private static EncodingSchemes instance = null;
	
	/**
	 *
	 */
	private EncodingSchemes(){
		super();
	}
	
	/**
	 * 
	 * @return
	 */
	private static EncodingSchemes getInstance(){
		if (instance==null)
			instance = new EncodingSchemes();
		
		EncodingSchemes result = instance;
		return instance;
	}

	/**
	 * 
	 * @param id
	 * @param servletContext
	 * @return
	 */
	public static String[] getLabels(String id){
		
		return (id==null || id.trim().length()==0) ? null : getInstance().get(id);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public static String getLabel(String id){
		return getLabel(id, false);
	}

	/**
	 * 
	 * @param id
	 * @param returnSelfIfNull
	 * @return
	 */
	public static String getLabel(String id, boolean returnSelfIfNull){
		
		String[] labels = getLabels(id);
		return labels!=null && labels.length>0 ? labels[0] : (returnSelfIfNull ? id : null); 
	}

	/**
	 * 
	 * @param id
	 * @param labels
	 * @param servletContext
	 */
	public static synchronized void update(String id, String[] labels){

		if (id==null || id.trim().length()==0 || labels==null || labels.length==0)
			return;
		
		getInstance().put(id, labels);
	}

	/**
	 * 
	 */
	public static void load(){
		
		logger.debug("Loading encoding schemes");
		
		IndexReader indexReader = null;
		try{
			int countLoaded = 0;
			String indexLocation = GeneralConfig.getProperty(GeneralConfig.LUCENE_INDEX_LOCATION);
			if (IndexReader.indexExists(indexLocation)){
				indexReader = IndexReader.open(indexLocation);
				String[] fields = {Identifiers.DOC_ID, Identifiers.RDFS_LABEL};
				FieldSelector fieldSelector = new MapFieldSelector(fields);
				int numDocs = indexReader.numDocs();
				int countUpdated = 0;
				for (int i=0; i<numDocs; i++){
					Document document = indexReader.document(i, fieldSelector);
					if (document!=null){
						String docID = document.get(Identifiers.DOC_ID);
						String[] labels = document.getValues(Identifiers.RDFS_LABEL);
						if (docID!=null && labels!=null && labels.length>0){
							getInstance().update(docID, labels);
							countLoaded++;
						}
					}
				}
				
				logger.debug(countLoaded + " encoding schemes loaded");
			}
			else{
				logger.debug("No encoding schemes loaded, because index does not yet exist");
			}
			
		}
		catch (Throwable t){
			logger.error("Failed to load encoding schemes: " + t.toString(), t);
		}
		
//		List<Map<String,String[]>> hits = null;
//		try{
//			hits = Searcher.search(Identifiers.IS_ENCODING_SCHEME + ":" + Boolean.TRUE.toString());
//		}
//		catch (Exception e){
//			logger.error("Failed to load encoding schemes: " + e.toString(), e);
//		}
//
//		int countLoaded = 0;
//		for (int i=0; hits!=null && i<hits.size(); i++){
//			Map<String,String[]> map = hits.get(i);
//			String[] ids = map.get(Identifiers.DOC_ID);
//			if (ids!=null && ids.length>0){
//				getInstance().update(ids[0], map.get("http://www.w3.org/2000/01/rdf-schema#label"));
//				countLoaded++;
//			}
//		}
	}
	
	/**
	 * 
	 * @return
	 */
	public static int getCount(){
		return getInstance().size();
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		
		IndexReader indexReader = null;
		try{
			EncodingSchemes.load();
			String indexLocation = GeneralConfig.getProperty(GeneralConfig.LUCENE_INDEX_LOCATION);
			if (IndexReader.indexExists(indexLocation)){
				indexReader = IndexReader.open(indexLocation);
				int i = 0;
				Collection coll = indexReader.getFieldNames(IndexReader.FieldOption.ALL);
				if (coll!=null && !coll.isEmpty()){
					for (Iterator iter=coll.iterator(); iter.hasNext(); i++){
						String fieldId = (String)iter.next();
						String label = EncodingSchemes.getLabel(fieldId);
						System.out.println((label==null || label.trim().length()==0 ? "NO_LABEL" : label) + ", " + fieldId);
					}
				}
				System.out.println("found " + i);
			}
		}
		catch (Throwable t){
			t.printStackTrace();
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
}
