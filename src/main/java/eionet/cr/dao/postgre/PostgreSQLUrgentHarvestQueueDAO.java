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
package eionet.cr.dao.postgre;

import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.UrgentHarvestQueueDAO;
import eionet.cr.dto.UrgentHarvestQueueItemDTO;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PostgreSQLUrgentHarvestQueueDAO extends PostgreSQLBaseDAO implements UrgentHarvestQueueDAO{

	public void addPullHarvests(List<UrgentHarvestQueueItemDTO> queueItems)
			throws DAOException {
		// TODO Auto-generated method stub
		
	}

	public void addPushHarvest(UrgentHarvestQueueItemDTO queueItem)
			throws DAOException {
		// TODO Auto-generated method stub
		
	}

	public List<UrgentHarvestQueueItemDTO> getUrgentHarvestQueue()
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public UrgentHarvestQueueItemDTO poll() throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

}
