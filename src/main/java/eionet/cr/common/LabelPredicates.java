package eionet.cr.common;

import java.util.List;
import java.util.ArrayList;

/**
 * This is a singleton implemented by a single-element enum type (as suggested by Joshua Bloch in his Effective Java 2nd Edition).
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 */
public enum LabelPredicates{

	/** The singleton instance. */
	INSTANCE;
	
	/** The list of label predicates in the correct order of failover. */
	List<String> predicates;
	
	/**
	 * 
	 */
	LabelPredicates(){
		predicates = new ArrayList<String>();
		predicates.add(Predicates.RDFS_LABEL);
		predicates.add(Predicates.SKOS_PREF_LABEL);
	}
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	public boolean contains(String s){
		return this.predicates.contains(s);
	}
	
	/**
	 * 
	 * @return
	 */
	public String formatForSQLInOperator(){
		
		StringBuilder buf = new StringBuilder();
		for (int i=0; i<predicates.size(); i++){
			if (i>0)
				buf.append(",");
			buf.append("'").append(predicates.get(i)).append("'");
		}
		return buf.toString();
	}
}
