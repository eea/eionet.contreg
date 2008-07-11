package eionet.cr.harvest.util;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class DedicatedHarvestSourceType {

	/** */
	private String name;
	private String title;
	private Set<String> classUris;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the classUris
	 */
	public String[] getClassUris() {
		return classUris==null ? null : classUris.toArray(new String[classUris.size()]);
	}

	/**
	 * 
	 * @param classUri
	 */
	public void addClassUri(String classUri){
		
		if (classUri==null || classUri.length()==0)
			return;
		
		if (classUris==null)
			classUris = new HashSet<String>();
		
		classUris.add(classUri);
	}
}
