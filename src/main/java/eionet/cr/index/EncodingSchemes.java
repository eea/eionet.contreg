package eionet.cr.index;

import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class EncodingSchemes extends Hashtable<String,String[]>{
	
	/** */
	private static Log logger = LogFactory.getLog(EncodingSchemes.class);
	
	/** */
	private static EncodingSchemes instance = null;
	
	/**
	 *
	 */
	public EncodingSchemes(){
		super();
	}
	
	/**
	 * 
	 * @return
	 */
	private static EncodingSchemes getInstance(){
		if (instance==null)
			instance = new EncodingSchemes();
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
		
		List hits = null;
		try{
			Searcher searcher = new Searcher();
			hits = searcher.search("IS_ENCODING_SCHEME:" + Boolean.TRUE.toString());
		}
		catch (Exception e){
			logger.error("Failed to load encoding schemes: " + e.toString(), e);
		}

		int countLoaded = 0;
		for (int i=0; hits!=null && i<hits.size(); i++){
			Hashtable hash = (Hashtable)hits.get(i);
			String[] ids = (String[])hash.get("ID");
			if (ids!=null && ids.length>0){
				getInstance().update(ids[0], (String[])hash.get("http://www.w3.org/2000/01/rdf-schema#label"));
				countLoaded++;
			}
		}
		
		logger.debug(countLoaded + " encoding schemes loaded");
	}
	
	/**
	 * 
	 * @return
	 */
	public static int getCount(){
		return getInstance().size();
	}
}
