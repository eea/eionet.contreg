/*
* The contents of this file are subject to the Mozilla Public
* 
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
* Agency. Portions created by Tieto Eesti are Copyright
* (C) European Environment Agency. All Rights Reserved.
* 
* Contributor(s):
* Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.harvest;

import java.util.HashMap;

import eionet.cr.util.Hashes;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class CurrentHarvests {

	/** */
	private static Harvest queuedHarvest;
	private static HashMap<String,String> instantHarvests;
	
	/**
	 * 
	 */
	static{
		instantHarvests = new HashMap<String,String>();
	}

	/**
	 * @return the queuedHarvest
	 */
	public static synchronized Harvest getQueuedHarvest() {
		return queuedHarvest;
	}

	/**
	 * @param queuedHarvest the queuedHarvest to set
	 */
	public static synchronized void setQueuedHarvest(Harvest queuedHarvest) {
		CurrentHarvests.queuedHarvest = queuedHarvest;
	}
	
	/**
	 * 
	 * @param url
	 * @param user
	 */
	public static synchronized void addInstantHarvest(String url, String user) {
		
		if (url!=null && user!=null){
			instantHarvests.put(url, user);
		}
	}
	
	/**
	 * 
	 * @param url
	 */
	public static synchronized void removeInstantHarvest(String url) {
		if (url!=null){
			instantHarvests.remove(url);
		}
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	public static synchronized boolean containsInstantHarvest(String url) {
		return url==null ? false : instantHarvests.containsKey(url);
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	public static synchronized boolean contains(String url) {		
		return url==null ? false : (queuedHarvest!=null && url.equals(queuedHarvest.getSourceUrlString())) || instantHarvests.containsKey(url);
	}

	/**
	 * 
	 * @param urlHash
	 * @return
	 */
	public static synchronized boolean contains(long urlHash) {
		
		if (queuedHarvest!=null && Hashes.spoHash(queuedHarvest.getSourceUrlString())==urlHash)
			return true;
		
		for (String url:instantHarvests.keySet()){
			if (Hashes.spoHash(url)==urlHash){
				return true;
			}
		}
		
		return false;
	}
}
