package eionet.cr.harvest.util;

import java.util.ArrayList;
import java.util.Hashtable;

import eionet.cr.common.Identifiers;

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
		
		this.put(Identifiers.ROD_DELIVERY_CLASS, deliveredFile);
		this.put(Identifiers.DCTYPE_DATASET_CLASS, deliveredFile);
		this.put(Identifiers.QAW_RESOURCE_CLASS, qawSource);
		this.put(Identifiers.QA_REPORT_CLASS, qawSource);
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
