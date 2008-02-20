package eionet.cr.harvest;

import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

public class EncodingSchemes extends Hashtable{
	
	/** */
	private static Logger logger = Logger.getLogger(EncodingSchemes.class);
	
	/**
	 *
	 */
	public EncodingSchemes(){
		super();
	}

	/**
	 * 
	 * @param id
	 * @param labels
	 */
	public void update(String id, String[] labels){
		
		if (id==null || id.trim().length()==0 || labels==null || labels.length==0)
			return;
		
		put(id, labels);
		logger.debug("Encoding scheme updated: " + id);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public String[] getLabels(String id){
		
		return (id==null || id.trim().length()==0) ? null : (String[])get(id);
	}

	/**
	 * 
	 * @param id
	 * @param servletContext
	 * @return
	 */
	public static String[] getLabels(String id, ServletContext servletContext){
		
		if (id==null ||  id.trim().length()==0 || servletContext==null)
			return null;
		
		EncodingSchemes encSchemes = (EncodingSchemes)servletContext.getAttribute(EncodingSchemes.class.getName());
		return (encSchemes!=null && encSchemes.size()>0) ? encSchemes.getLabels(id) : null;
	}
	
	/**
	 * 
	 * @param id
	 * @param labels
	 * @param servletContext
	 */
	public static void update(String id, String[] labels, ServletContext servletContext){

		if (id==null || id.trim().length()==0 || labels==null || labels.length==0 || servletContext==null)
			return;
		
		EncodingSchemes encSchemes = (EncodingSchemes)servletContext.getAttribute(EncodingSchemes.class.getName());
		if (encSchemes==null)
			encSchemes = new EncodingSchemes();
		
		encSchemes.update(id, labels);
	}
}
