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
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.harvest.persist;

import java.sql.SQLException;
import java.text.DateFormat;


/**
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public interface IHarvestPersister {
	
	/**
	 * Add triple to store.
	 * 
	 * @param subjectHash
	 * @param anonSubject
	 * @param predicateHash
	 * @param object
	 * @param objectLang
	 * @param litObject
	 * @param anonObject
	 * @param objSourceObject
	 * @throws PersisterException
	 */
	void addTriple(long subjectHash, boolean anonSubject, long predicateHash,
			String object, long objectHash, String objectLang, boolean litObject, boolean anonObject, long objSourceObject) throws PersisterException;

	/**
	 * 
	 * @param uri
	 * @param uriHash
	 * @throws PersisterException
	 */
	void addResource(String uri, long uriHash) throws PersisterException;
	
	/**
	 * rollbacks harvest.
	 * 
	 * @throws PersisterException
	 */
	void rollback() throws PersisterException;
	
	/**
	 * commits resources when harvest is finished.
	 * 
	 * @throws PersisterException
	 */
	void commit() throws PersisterException;
	
	/**
	 * called, when end of file is hit.
	 * 
	 * @throws PersisterException
	 */
	void endOfFile() throws PersisterException;
	
	/**
	 * release all related resources.
	 * 
	 * @throws PersisterException
	 */
	void closeResources() throws PersisterException;
	
	/**
	 * opens and prepares needed resources.
	 * 
	 * @throws PersisterException
	 */
	void openResources() throws PersisterException;
	
	/**
	 * rollback any unfinished harvests.
	 * 
	 * @throws PersisterException
	 */
	void rollbackUnfinishedHarvests() throws PersisterException;

	/**
	 * 
	 * @return
	 */
	int getStoredTriplesCount();
	
	/**
	 * @param subjectHash TODO
	 * @param dateFormat TODO
	 * @throws SQLException 
	 * 
	 */
	void updateLastRefreshed(long subjectHash, DateFormat dateFormat) throws SQLException;
}
