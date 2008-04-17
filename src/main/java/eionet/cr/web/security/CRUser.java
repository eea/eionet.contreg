package eionet.cr.web.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tee.uit.security.AccessControlListIF;
import com.tee.uit.security.AccessController;
import com.tee.uit.security.SignOnException;

import eionet.cr.harvest.Harvest;

/**
 * Class represents authenticated user.
 * 
 * @author altnyris
 *
 */
public class CRUser {
	
	private static Log logger = LogFactory.getLog(CRUser.class);
	
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
	
	public boolean hasPermission(String aclPath, String prm){
		
		try{
			AccessControlListIF acl = AccessController.getAcl(aclPath);
			if (acl!=null) return acl.checkPermission(userName, prm);
		}
		catch (SignOnException soe){
			logger.error(soe.toString(),soe);
		}
		
		return false;
	}
}
