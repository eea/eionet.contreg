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

import java.util.List;

import org.dbunit.dataset.IDataSet;
import org.junit.Test;

import eionet.cr.dto.TripleDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.pagination.PagingRequest;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HelperDAOTest extends CRDatabaseTestCase {

    /*
     * (non-Javadoc)
     *
     * @see org.dbunit.DatabaseTestCase#getDataSet()
     */
    @Override
    protected IDataSet getDataSet() throws Exception {
        return getXmlDataSet("simple-db.xml");
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testGetSampleTriples() throws Exception {

        List<TripleDTO> result =
                DAOFactory.get().getDao(HelperDAO.class)
                        .getSampleTriplesInSource("http://cr.eionet.europa.eu/testsource.rdf", PagingRequest.create(1, 10));

        assertNotNull(result);
        assertEquals(10, result.size());
    }
}
