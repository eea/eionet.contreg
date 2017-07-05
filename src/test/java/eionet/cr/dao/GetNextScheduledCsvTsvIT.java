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
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.dao;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.Pair;
import eionet.cr.util.pagination.PagingRequest;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * JUnit test tests HarvestSourceDAO functionality.
 *
 * @author altnyris
 *
 */
public class GetNextScheduledCsvTsvIT extends CRDatabaseTestCase {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getXMLDataSetFiles()
     */
    @Override
    protected List<String> getXMLDataSetFiles() {
        return Arrays.asList("get-next-scheduled-csv-tsv-data.xml");
    }

    @Test
    public void testGetNextScheduledOnlineCsvTsv() throws Exception {

        List<HarvestSourceDTO> sources = DAOFactory.get().getDao(HarvestSourceDAO.class).getNextScheduledOnlineCsvTsv(1);
        assertEquals(1, sources.size());
        assertEquals(9, sources.get(0).getSourceId().intValue());
    }
}
