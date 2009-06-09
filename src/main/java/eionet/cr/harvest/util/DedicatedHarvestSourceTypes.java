/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
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
