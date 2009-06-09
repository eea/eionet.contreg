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
package eionet.cr.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
