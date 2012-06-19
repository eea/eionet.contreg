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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import eionet.cr.dto.TripleDTO;
import eionet.cr.test.helpers.RdfLoader;
import eionet.cr.util.pagination.PagingRequest;

/**
 *
 * @author Risto Alt
 *
 */
public class HelperDAOTest {

    private static RdfLoader loader;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        loader = new RdfLoader("obligations.rdf");
    }

    @Test
    public void testGetSampleTriples() throws Exception {
        List<TripleDTO> result =
                DAOFactory.get().getDao(HelperDAO.class)
                        .getSampleTriplesInSource(loader.getGraphUri(), PagingRequest.create(1, 10));

        assertNotNull(result);
        assertEquals(100, result.size());
    }
}
