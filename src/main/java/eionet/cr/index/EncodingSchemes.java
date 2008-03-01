package eionet.cr.index;

import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.util.Identifiers;
import eionet.cr.util.Util;


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
		
		List hits = null;
		try{
			hits = Searcher.search(Identifiers.IS_ENCODING_SCHEME + ":" + Boolean.TRUE.toString());
		}
		catch (Exception e){
			logger.error("Failed to load encoding schemes: " + e.toString(), e);
		}

		int countLoaded = 0;
		for (int i=0; hits!=null && i<hits.size(); i++){
			Hashtable hash = (Hashtable)hits.get(i);
			String[] ids = (String[])hash.get(Identifiers.DOC_ID);
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
