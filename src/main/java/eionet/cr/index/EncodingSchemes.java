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
	public static EncodingSchemes getInstance(){
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
	 * @return
	 */
	public static int getCount(){
		return getInstance().size();
	}
}
