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

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.URIUtil;
import eionet.cr.web.util.ApplicationCache;

/**
 * @author Aleksandr Ivanov <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class TypeCacheUpdater implements StatefulJob {
    /**
     * Internal logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TypeCacheUpdater.class);

    /**
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext) {@inheritDoc}
     */
    public void execute(final JobExecutionContext arg0) throws JobExecutionException {

        try {
            List<Pair<String, String>> types = new LinkedList<Pair<String, String>>();

            // TODO change back to use original filtered search if Virtuoso inferencing issue is clarified

            // Map<String, String> criteria = new HashMap<String, String>();
            // criteria.put(Predicates.RDF_TYPE, Subjects.RDFS_CLASS);
            //
            // List<String> predicates = new ArrayList<String>();
            // predicates.add(Predicates.RDFS_LABEL);
            //
            // Pair<Integer, List<SubjectDTO>> customSearch = DAOFactory.get()
            // .getDao(SearchDAO.class)
            // .searchByFilters(
            // criteria,
            // null,
            // null,
            // new SortingRequest(Predicates.RDFS_LABEL, SortOrder.ASCENDING),
            // predicates);

            // if (customSearch != null) {
            //
            // List<SubjectDTO> subjects = customSearch.getRight();

            List<SubjectDTO> subjects = DAOFactory.get().getDao(SearchDAO.class).getTypes();

            if (subjects != null && !subjects.isEmpty()) {
                for (SubjectDTO subject : subjects) {

                    String uri = subject.getUri();
                    if (uri != null && !subject.isAnonymous()) {

                        String label = subject.getObjectValue(Predicates.RDFS_LABEL);
                        if (StringUtils.isBlank(label)) {
                            label = URIUtil.extractURILabel(uri, uri);
                        }

                        types.add(new Pair<String, String>(uri, label));
                    }
                }
            }
            // }

            ApplicationCache.updateTypes(types);
            LOGGER.debug("type cache successfully updated!");

        } catch (DAOException e) {
            LOGGER.error("Exception is thrown while updating type cache", e);
        }
    }
}
