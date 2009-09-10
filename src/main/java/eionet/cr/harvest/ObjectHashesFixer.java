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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.config.GeneralConfig;
import eionet.cr.util.Hashes;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.SQLUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class ObjectHashesFixer extends Thread{
	
	/** */
	private static final String URN_UUID_PREFIX = "urn:uuid:";
	
	/** */
	private static final String prop_batchSize = ObjectHashesFixer.class.getSimpleName() + ".batchSize";
	private static final String prop_noOfBatchesToRun = ObjectHashesFixer.class.getSimpleName() + ".noOfBatchesToRun";
	
	/** */
	private static int BATCH_SIZE = 1000;
	private static int NOOF_BATCHES_TO_RUN = 1;

	/**
	 * 
	 */
	static{
		BATCH_SIZE = Integer.valueOf(GeneralConfig.getProperty(prop_batchSize, String.valueOf(BATCH_SIZE)).trim());
		
		String s = GeneralConfig.getProperty(prop_noOfBatchesToRun, String.valueOf(NOOF_BATCHES_TO_RUN)).trim();
		NOOF_BATCHES_TO_RUN = s.equals("unlimited") ? Integer.MAX_VALUE : Integer.valueOf(s);
	}
	
	/** */
	public static boolean shutdownIssued = false;

	/** */
	private static Log logger = LogFactory.getLog(ObjectHashesFixer.class);
	
	/** */
	private static final String selectSQL =
		"select distinct OBJECT, OBJECT_HASH, SOURCE, GEN_TIME from SPO where ANON_OBJ='Y' and OBJECT like 'ARP%' limit ?";
	
	/** */
	private static final String updateObjectsSQL = "update SPO set OBJECT=?,OBJECT_HASH=? where OBJECT_HASH=?";

	/** */
	private static final String updateSubjectsSQL = "update SPO set SUBJECT=? where SUBJECT=?";
	
	/** */
	private static final String updateSourceObjectsSQL = "update SPO set OBJ_SOURCE_OBJECT=? where OBJ_SOURCE_OBJECT=?";
	
	/** */
	private static final String updateResourcesSQL = "insert ignore into RESOURCE" +
			" (URI, URI_HASH, FIRSTSEEN_SOURCE, FIRSTSEEN_TIME) values (?, ?, ?, ?)";
	
	/**
	 * 
	 */
	public ObjectHashesFixer(){
	}
	
	/**
	 * 
	 * @return
	 */
	public static synchronized boolean isShutdownIssued(){
		return shutdownIssued;
	}

	/**
	 * 
	 * @param b
	 */
	public static synchronized void setShutdownIssued(boolean b){
		shutdownIssued = b;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run(){
		
		logger.debug(this.getClass().getSimpleName() + " started, batchSize=" + BATCH_SIZE + ", noOfBatchesToRun=" + NOOF_BATCHES_TO_RUN);
		
		int ret = BATCH_SIZE;
		int batchesRun = 0;
		while (ret>0 && batchesRun < NOOF_BATCHES_TO_RUN && !ObjectHashesFixer.isShutdownIssued()){
			
			logger.debug("Executing batch #" + (batchesRun+1));
			ret = runBatch();
			logger.debug("Batch done, number of necessary replacements found and performed: " + ret);
			
			batchesRun++;
		}
		
		if (ret==0)
			logger.debug("All done, found no more replacements to perform!");
		else if (batchesRun==NOOF_BATCHES_TO_RUN)
			logger.debug("Exiting because the required number of batches has been run!");
		else if (ObjectHashesFixer.isShutdownIssued())
			logger.debug("Exiting because a shutdown was issued from outside. The number of batches that was run:" + batchesRun);
		else
			logger.debug("All done, exiting!");
	}
	
	/**
	 * @throws SQLException 
	 * 
	 */
	private int runBatch(){
		
		int batchSize = 0;
		
		Connection conn = null;
		PreparedStatement selectStmt = null;
		PreparedStatement pstmtObjects = null;
		PreparedStatement pstmtSubjects = null;
		PreparedStatement pstmtSourceObjects = null;
		PreparedStatement pstmtResources = null;
		ResultSet rs = null;
		try{
			ConnectionUtil.setReturnSimpleConnection(true);
			conn = ConnectionUtil.getConnection();
			
			selectStmt = conn.prepareStatement(selectSQL);
			pstmtObjects = conn.prepareStatement(updateObjectsSQL);
			pstmtSubjects = conn.prepareStatement(updateSubjectsSQL);
			pstmtSourceObjects = conn.prepareStatement(updateSourceObjectsSQL);
			pstmtResources = conn.prepareStatement(updateResourcesSQL);

			selectStmt.setInt(1, BATCH_SIZE);
			rs = selectStmt.executeQuery();
			
			while (rs.next()){
				
				String object = rs.getString("OBJECT");
				long source = rs.getLong("SOURCE");
				long genTime = rs.getLong("GEN_TIME");
				
				String uuidNamePrefix = createUuidNamePrefix(String.valueOf(source), String.valueOf(genTime));
				String uuidUri = generateUUID(uuidNamePrefix, object);
				long newHash = Hashes.spoHash(uuidUri);
				long oldHash = rs.getLong("OBJECT_HASH");
				
//				logger.debug("Old " + object + " = " + oldHash + ", new " + uuidUri + " = " + newHash);
				
				// update objects
				pstmtObjects.setString(1, uuidUri);
				pstmtObjects.setLong(2, newHash);
				pstmtObjects.setLong(3, oldHash);
				pstmtObjects.addBatch();
				
				// update subjects
				pstmtSubjects.setLong(1, newHash);
				pstmtSubjects.setLong(2, oldHash);
				pstmtSubjects.addBatch();
				
				// update sourceObjects
				pstmtSourceObjects.setLong(1, newHash);
				pstmtSourceObjects.setLong(2, oldHash);
				pstmtSourceObjects.addBatch();
				
				// update resources
				pstmtResources.setString(1, uuidUri);
				pstmtResources.setLong(2, newHash);
				pstmtResources.setLong(3, source);
				pstmtResources.setLong(4, genTime);
				pstmtResources.addBatch();
				
				batchSize++;
			}
			
			if (batchSize>0){
				
//				logger.debug("Executing batch for objects");
				pstmtObjects.executeBatch();
//				logger.debug("Executing batch for subjects");
				pstmtSubjects.executeBatch();
//				logger.debug("Executing batch for source-objects");
				pstmtSourceObjects.executeBatch();
//				logger.debug("Executing batch for resources");
				pstmtResources.executeBatch();
			}
		}
		catch (SQLException e){
			throw new RuntimeException(e.toString(), e);
		}
		finally{
			SQLUtil.close(rs);
			SQLUtil.close(selectStmt);
			SQLUtil.close(pstmtObjects);
			SQLUtil.close(pstmtSubjects);
			SQLUtil.close(pstmtSourceObjects);
			SQLUtil.close(pstmtResources);
			SQLUtil.close(conn);
		}
		
		return batchSize;
	}

	/**
	 * 
	 * @param source
	 * @param genTime
	 * @return
	 */
	private static String createUuidNamePrefix(String source, String genTime){
		return new StringBuilder(source).append(":").append(genTime).append(":").toString();
	}
	
	private static String generateUUID(String uuidNamePrefix, String object){
		
		String uuid = UUID.nameUUIDFromBytes(new StringBuilder(uuidNamePrefix).append(object).toString().getBytes()).toString();
		return new StringBuilder(URN_UUID_PREFIX).append(uuid).toString();
	}
	
	/**
	 * 
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException{
		
		ObjectHashesFixer fixer = new ObjectHashesFixer();
		Runtime.getRuntime().addShutdownHook(new ShutdownHook(fixer));
		fixer.start();
	}
}

class ShutdownHook extends Thread {
	
	/** */
	private ObjectHashesFixer fixer;
	
	/**
	 * 
	 * @param fixer
	 */
	public ShutdownHook(ObjectHashesFixer fixer){
		this.fixer = fixer;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
    public void run() {

    	try{
    		sleep(1000);
    		if (fixer.isAlive()){
    			
    			ObjectHashesFixer.setShutdownIssued(true);
    			System.out.println("Shutdown issued, waiting for the fixer to finish...");
    			
        		while (fixer.isAlive()){
        			sleep(1000);
        		}
        		
        		System.out.println("Fixer finished, shutdown complete!");
    		}
    	}
    	catch (InterruptedException e) {
			System.out.println("InterruptedException received: ");
			e.printStackTrace(System.out);
			return;
		}
    }
}

