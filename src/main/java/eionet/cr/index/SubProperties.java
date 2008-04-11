package eionet.cr.index;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.IndexReader;

import eionet.cr.config.GeneralConfig;
import eionet.cr.util.Identifiers;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubProperties extends Hashtable<String,List<String>>{
	
	/** */
	private static SubProperties instance = null;

	/**
	 * 
	 */
	private SubProperties(){
		super();
	}
	
	/**
	 * 
	 * @return
	 */
	private static SubProperties getInstance(){
		if (instance==null)
			instance = new SubProperties();
		return instance;
	}
	
	/**
	 * 
	 * @param of
	 * @param subPropery
	 */
	public static synchronized void addSubProperty(String of, String subProperty){
		
		if (of!=null && subProperty!=null){
			List<String> subProperties = getInstance().get(of);
			if (subProperties==null)
				subProperties = new ArrayList<String>();
			subProperties.add(subProperty);
			getInstance().put(of, subProperties);
		}
	}
	
	/**
	 * 
	 * @param of
	 * @param subProperty
	 */
	public static synchronized void addSubProperty(String[] of, String subProperty){
		if (of!=null && of.length>0 && subProperty!=null){
			for (int i=0; i<of.length; i++)
				addSubProperty(of[i], subProperty);
		}
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public static List<String> getSubPropertiesOf(String id){
		
		if (id!=null)
			return getInstance().get(id);
		else
			return null;
	}
	
	/**
	 * 
	 * @param of
	 * @param what
	 * @return
	 */
	public static boolean isSubPropertyOf(String of, String what){
		
		if (of!=null && what!=null){
			List<String> subProperties = getInstance().get(of);
			return subProperties!=null && subProperties.contains(what);
		}
		else
			return false;
	}
	
	/**
	 * 
	 * @return
	 */
	public static int getCount(){
		return getInstance().size();
	}
}
