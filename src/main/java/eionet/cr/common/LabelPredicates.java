package eionet.cr.common;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import eionet.cr.util.Hashes;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 */
public class LabelPredicates{
	
	/** */
	private static final LabelPredicates instance = new LabelPredicates();

	/** */
	private LinkedHashSet<String> predicates;
	
	/** */
	private String[] predicateHashes;
	
	/** */
	private String commaSeparatedHashes;
	
	/**
	 * 
	 */
	private LabelPredicates(){
		
		load();
		
		int i = 0;
		StringBuffer buf = new StringBuffer();
		
		predicateHashes = new String[predicates.size()];
		for (Iterator<String> iter=predicates.iterator(); iter.hasNext(); i++){
			predicateHashes[i] = String.valueOf(Hashes.spoHash(iter.next()));
			if (i>0)
				buf.append(",");
			buf.append(predicateHashes[i]);
		}
		
		commaSeparatedHashes = buf.toString();
	}
	
	/**
	 * 
	 */
	private void load(){
		
		// TODO - real loading must be done from some configuration
		predicates = new LinkedHashSet<String>();
		predicates.add(Predicates.RDFS_LABEL);
		predicates.add(Predicates.SKOS_PREF_LABEL);
	}
	
	/**
	 * 
	 * @return
	 */
	public static String getCommaSeparatedHashes(){
		return LabelPredicates.instance.commaSeparatedHashes;
	}
}
