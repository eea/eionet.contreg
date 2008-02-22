package eionet.cr.web.security;

/**
 * Class represents authenticated user.
 * 
 * @author altnyris
 *
 */
public class CRUser {
	
	private String userName;
	
	/** Default constructor. */
	public CRUser() {
		
	}
	
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

}
