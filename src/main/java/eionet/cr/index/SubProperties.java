package eionet.cr.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.IndexReader;

import eionet.cr.common.Identifiers;
import eionet.cr.config.GeneralConfig;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubProperties extends HashMap<String,HashSet<String>>{
	
	/** */
	private static SubProperties instance = null;

	/**
	 * 
	 */
	public SubProperties(){
		super();
	}
	
	/**
	 * 
	 * @return
	 */
	public static SubProperties getInstance(){
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
			if (!of.equals(subProperty)){
				HashSet<String> subProperties = getInstance().get(of);
				if (subProperties==null)
					subProperties = new HashSet<String>();
				subProperties.add(subProperty);
				getInstance().put(of, subProperties);
			}
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
		
		if (id!=null){
			HashSet hashSet = getInstance().get(id);
			return hashSet==null ? null : new ArrayList(hashSet);
		}
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
			HashSet<String> subProperties = getInstance().get(of);
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
