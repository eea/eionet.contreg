package eionet.cr.web.security;

/**
 * Class represents authenticated user.
 * 
 * @author altnyris
 *
 */
public class CrUser {
	
	private String userName;
	
	/** Default constructor. */
	public CrUser() {
		
	}
	
	public CrUser(String userName) {
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
