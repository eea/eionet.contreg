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
package eionet.cr.harvest.persist.postgresql;

import eionet.cr.harvest.persist.IHarvestPersister;
import eionet.cr.harvest.persist.PersisterConfig;
import eionet.cr.harvest.persist.PersisterException;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PostgreSQLPersister implements IHarvestPersister{

	/**
	 * 
	 */
	public PostgreSQLPersister(){
	}

	/**
	 * 
	 * @param config
	 */
	public PostgreSQLPersister(PersisterConfig config){
	}

	/* (non-Javadoc)
	 * @see eionet.cr.harvest.persist.IHarvestPersister#addResource(java.lang.String, long)
	 */
	public void addResource(String uri, long uriHash) throws PersisterException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.harvest.persist.IHarvestPersister#addTriple(long, boolean, long, java.lang.String, long, java.lang.String, boolean, boolean, long)
	 */
	public void addTriple(long subjectHash, boolean anonSubject,
			long predicateHash, String object, long objectHash,
			String objectLang, boolean litObject, boolean anonObject,
			long objSourceObject) throws PersisterException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.harvest.persist.IHarvestPersister#closeResources()
	 */
	public void closeResources() throws PersisterException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.harvest.persist.IHarvestPersister#commit()
	 */
	public void commit() throws PersisterException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.harvest.persist.IHarvestPersister#endOfFile()
	 */
	public void endOfFile() throws PersisterException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.harvest.persist.IHarvestPersister#getDistinctSubjectCount()
	 */
	public int getDistinctSubjectCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see eionet.cr.harvest.persist.IHarvestPersister#getStoredTripleCount()
	 */
	public int getStoredTripleCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see eionet.cr.harvest.persist.IHarvestPersister#openResources()
	 */
	public void openResources() throws PersisterException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.harvest.persist.IHarvestPersister#rollback()
	 */
	public void rollback() throws PersisterException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.harvest.persist.IHarvestPersister#rollbackUnfinishedHarvests()
	 */
	public void rollbackUnfinishedHarvests() throws PersisterException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see eionet.cr.harvest.persist.IHarvestPersister#tempCommit()
	 */
	public void tempCommit() throws PersisterException {
		// TODO Auto-generated method stub
		
	}
}
