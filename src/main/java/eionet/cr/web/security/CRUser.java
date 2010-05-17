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
package eionet.cr.web.security;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tee.uit.security.AccessControlListIF;
import com.tee.uit.security.AccessController;
import com.tee.uit.security.SignOnException;

import eionet.cr.util.Util;

/**
 * Class represents authenticated user.
 * 
 * @author altnyris
 *
 */
public class CRUser {
	
	/** */
	private static Log logger = LogFactory.getLog(CRUser.class);
	
	/** */
	public static final CRUser application = new CRUser("application");
	
	/** */
	private String userName;

	/**
	 * 
	 */
	public CRUser() {		
	}
	
	/**
	 * 
	 * @param userName
	 */
	public CRUser(String userName) {
		this.userName = userName;
	}
	
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param username the userName to set
	 */
	public void setUserName(String username) {
		this.userName = username;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isAdministrator(){
		
		return hasPermission("/", "u");
	}
	
	/**
	 * 
	 * @param aclPath
	 * @param prm
	 * @return
	 */
	public boolean hasPermission(String aclPath, String prm){
		return hasPermission(userName, aclPath, prm);
	}
	
	/**
	 * 
	 * @param userName
	 * @param aclPath
	 * @param prm
	 * @return
	 */
	public static boolean hasPermission(String userName, String aclPath, String prm){

		if (Util.isNullOrEmpty(userName) || Util.isNullOrEmpty(aclPath) || Util.isNullOrEmpty(prm))
			return false;
		
		boolean result = false;
		try{
			AccessControlListIF acl = AccessController.getAcl(aclPath);
			if (acl!=null){
				result = acl.checkPermission(userName, prm);
				if (result==false)
					logger.debug("User " + userName + " does not have permission " + prm + " in acl \"" + aclPath + "\"");
			}
			else
				logger.warn("acl \"" + aclPath + "\" not found!");
		}
		catch (SignOnException soe){
			logger.error(soe.toString(), soe);
		}
		
		return result;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getHomeUri(){
		return CRUser.homeUri(userName);
	}

	/**
	 * 
	 * @return
	 */
	public String getRegistrationsUri(){
		return CRUser.registrationsUri(userName);
	}

	/**
	 * 
	 * @return
	 */
	public String getBookmarksUri(){
		return CRUser.bookmarksUri(userName);
	}

	/**
	 * 
	 * @return
	 */
	public String getHistoryUri(){
		return CRUser.historyUri(userName);
	}

	/**
	 * 
	 * @param userName
	 * @return
	 */
	public static String homeUri(String userName){
		
		if (StringUtils.isBlank(userName))
			throw new IllegalArgumentException("userName must not be blank");
		else
			return "http://cr.eionet.europa.eu/home/" + userName;
	}

	/**
	 * 
	 * @param userName
	 * @return
	 */
	public static String bookmarksUri(String userName){
		
		return CRUser.homeUri(userName) + "/bookmarks";
	}

	/**
	 * 
	 * @param userName
	 * @return
	 */
	public static String registrationsUri(String userName){
		
		return CRUser.homeUri(userName) + "/registrations";
	}

	/**
	 * 
	 * @param userName
	 * @return
	 */
	public static String historyUri(String userName){
		
		return CRUser.homeUri(userName) + "/history";
	}
}
