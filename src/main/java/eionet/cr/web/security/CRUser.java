package eionet.cr.web.security;

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
				logger.debug("User " + userName + " " + (result ? "has" : "does not have") + " permission " + prm + " in acl \"" + aclPath + "\"");
			}
			else
				logger.warn("acl \"" + aclPath + "\" not found!");
		}
		catch (SignOnException soe){
			logger.error(soe.toString(), soe);
		}
		
		return result;
	}
}
