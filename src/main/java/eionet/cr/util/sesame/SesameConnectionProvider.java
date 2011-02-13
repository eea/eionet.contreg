package eionet.cr.util.sesame;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import virtuoso.sesame2.driver.VirtuosoRepository;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;

/**
 * 
 * @author jaanus
 *
 */
public class SesameConnectionProvider {

	/** */
	private static Repository repository = null;
	
	/** */
	private static Object repositoryLock = new Object();

	/**
	 * 
	 */
	private static void initRepository(){
		
		String url = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_URL);
		String usr = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_USR);
		String pwd = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_DB_PWD);
		
		try {
			repository = new VirtuosoRepository(url, usr, pwd);
			repository.initialize();
		}
		catch (RepositoryException e) {
			throw new CRRuntimeException("Failed to initialize repository", e);
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws RepositoryException 
	 */
	public static RepositoryConnection getConnection() throws RepositoryException{
		
		if (repository==null){
			synchronized (repositoryLock) {
				
				// double-checked locking pattern
				// (http://www.ibm.com/developerworks/java/library/j-dcl.html)
				if (repository==null){
					initRepository();
				}
			}			
		}
		
		return repository.getConnection();
	}
}
