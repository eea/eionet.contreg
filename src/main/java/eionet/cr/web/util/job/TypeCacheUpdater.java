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
package eionet.cr.web.util.job;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.ISearchDao;
import eionet.cr.dao.mysql.MySQLDAOFactory;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.util.SortOrder;
import eionet.cr.util.PageRequest;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.web.util.ApplicationCache;

/**
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class TypeCacheUpdater implements StatefulJob {
	
	private static final Logger logger = Logger.getLogger(TypeCacheUpdater.class);

	/** 
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 * {@inheritDoc}
	 */
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			List<Pair<String,String>> types = new LinkedList<Pair<String,String>>();
			Map<String,String> criteria = new HashMap<String,String>();
			criteria.put(Predicates.RDF_TYPE, Subjects.RDFS_CLASS);

			Pair<Integer, List<SubjectDTO>> customSearch = MySQLDAOFactory.get()
					.getDao(ISearchDao.class)
					.performCustomSearch(
							criteria,
							null,
							new PageRequest(1,0),
							new SortingRequest(Predicates.RDFS_LABEL, SortOrder.ASCENDING));
			
			if (customSearch != null && customSearch.getValue() != null){
				for(SubjectDTO subject : customSearch.getValue()) {
					if (!subject.isAnonymous()){
						String label = subject.getObjectValue(Predicates.RDFS_LABEL);
						if (!StringUtils.isBlank(label)){
							types.add(new Pair<String,String>(subject.getUri(), label));
						}
					}
				}
			}
			ApplicationCache.updateTypes(types);
			logger.debug("type cache successfully updated!");
		} catch (DAOException e) {
			logger.error("Exception is thrown while updating type cache", e);
		}
	}

}