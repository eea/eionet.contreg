package eionet.cr.harvest.util;

import java.util.Hashtable;

import eionet.cr.common.Subjects;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class DedicatedHarvestSourceTypes extends Hashtable<String,String>{

	/** */
	public static final String deliveredFile = "delivered file";
	public static final String qawSource = "qaw source";
	
	/** */
	private static DedicatedHarvestSourceTypes instance = null;
	
	/**
	 * 
	 */
	private DedicatedHarvestSourceTypes(){
		super();
		load();
	}
	
	/**
	 * 
	 */
	private void load(){
		
		// TODO - real loading must be done from some configuration file
		
		this.put(Subjects.ROD_DELIVERY_CLASS, deliveredFile);
		this.put(Subjects.DCTYPE_DATASET_CLASS, deliveredFile);
		this.put(Subjects.QAW_RESOURCE_CLASS, qawSource);
		this.put(Subjects.QA_REPORT_CLASS, qawSource);
	}

	/**
	 * 
	 * @return
	 */
	public static synchronized DedicatedHarvestSourceTypes getInstance(){
		if (instance==null)
			instance = new DedicatedHarvestSourceTypes();
		return instance;
	}
}
