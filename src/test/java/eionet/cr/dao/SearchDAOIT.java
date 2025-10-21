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
package eionet.cr.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import eionet.cr.ApplicationTestContext;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import eionet.cr.common.Predicates;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.helpers.FreeTextSearchHelper;
import eionet.cr.dao.util.SearchExpression;
import eionet.cr.dto.SearchResultDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.pagination.PagingRequest;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Risto Alt
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class SearchDAOIT extends CRDatabaseTestCase {

    /** Seed file. */
    private static final String SEED_FILE = "obligations.rdf";
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchDAOIT.class);

    @Autowired
    private SearchDAO searchDAO;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /*
         * (non-Javadoc)
         *
         * @see eionet.cr.test.helpers.CRDatabaseTestCase#getRDFXMLSeedFiles()
         */
    @Override
    protected List<String> getRDFXMLSeedFiles() {
        return Arrays.asList(SEED_FILE);
    }

    /**
     * Test free-text search hit counts.
     *
     * @throws Exception When any error happens.
     */
    @Test
    public void testFreeTextSearchCountResults() throws Exception {

        // Should not test full-text search if there is no real-time full-text indexing activated in the underlying repository.
        // By "real-time" we mean that the index is updated instantly after loading a triple.
        if (isRealTimeFullTextIndexingActivated() == false) {
            LOGGER.info("Skipping full-text search test, as no real-time full-text indexing has been activated!");
            return;
        }

        PagingRequest pagingRequest = PagingRequest.create(1);
        SearchResultDTO<SubjectDTO> result = searchDAO.searchByFreeText(new SearchExpression("Questionnaire"), FreeTextSearchHelper.FilterType.ANY_OBJECT,
                                false, pagingRequest, null);

        assertEquals(2, result.getMatchCount());
    }

    /**
     * @throws Exception
     *
     */
    @Test
    public void testSearchByTypeAndFilters() throws Exception {
        PagingRequest pagingRequest = PagingRequest.create(1);

        Map<String, String> filters = new LinkedHashMap<String, String>();
        filters.put(Predicates.RDF_TYPE, "http://rod.eionet.europa.eu/schema.rdf#Obligation");
        List<String> selectedPredicates = new ArrayList<String>();

        SearchResultDTO<SubjectDTO> result = searchDAO.searchByTypeAndFilters(filters, false, pagingRequest, null, selectedPredicates);

        assertEquals(3, result.getMatchCount());
    }

    /**
     * Returns true if the configuration says that the underlying triple-store has real-time full-text indexing activated.
     *
     * @return True/false.
     */
    private boolean isRealTimeFullTextIndexingActivated() {
        String value = GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_REAL_TIME_FT_INDEXING);
        return BooleanUtils.toBoolean(value);
    }
}
