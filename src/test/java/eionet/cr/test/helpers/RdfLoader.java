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
package eionet.cr.test.helpers;

import java.io.IOException;
import java.io.InputStream;

import org.dbunit.dataset.DataSetException;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;

/**
 *
 */
public class RdfLoader {

    private static String graphUri;
    public static String TEST_GRAPH_URI = "http://test.com/test/";

    public RdfLoader(String datasetName) throws Exception {
        InputStream is = getFile(datasetName);
        if (is != null) {
            graphUri = getGraphUri(datasetName);
            DAOFactory.get().getDao(HarvestSourceDAO.class).loadIntoRepository(is, null, graphUri, true);
        }
    }

    private InputStream getFile(String fileName) throws DataSetException, IOException {
        return CRDatabaseTestCase.class.getClassLoader().getResourceAsStream(fileName);
    }

    public String getGraphUri() {
        return graphUri;
    }

    public static String getGraphUri(String datasetName) {
        return TEST_GRAPH_URI + datasetName;
    }
}
