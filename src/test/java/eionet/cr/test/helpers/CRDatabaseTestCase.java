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
package eionet.cr.test.helpers;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.dbunit.DatabaseTestCase;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

import eionet.cr.test.helpers.dbunit.DbUnitDatabaseConnection;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class CRDatabaseTestCase extends DatabaseTestCase {

    /*
     * (non-Javadoc)
     *
     * @see org.dbunit.DatabaseTestCase#getConnection()
     */
    @Override
    protected IDatabaseConnection getConnection() throws Exception {

        return DbUnitDatabaseConnection.get();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dbunit.DatabaseTestCase#getDataSet()
     */
    @SuppressWarnings("deprecation")
    @Override
    protected IDataSet getDataSet() throws Exception {

        List<String> xmlDataSetFiles = getXMLDataSetFiles();
        if (CollectionUtils.isEmpty(xmlDataSetFiles)) {
            return null;
        }

        int i = 0;
        IDataSet[] dataSets = new IDataSet[xmlDataSetFiles.size()];
        for (String fileName : xmlDataSetFiles) {
            dataSets[i++] = new FlatXmlDataSet(getClass().getClassLoader().getResourceAsStream(fileName));
        }

        CompositeDataSet compositeDataSet = new CompositeDataSet(dataSets);
        return compositeDataSet;
    }

    /**
     * Abstract method to be implemented by extending classes.
     *
     * @return List of names of XML-formatted DBUnit dataset seed files. These will be loaded in the test set-up.
     */
    protected abstract List<String> getXMLDataSetFiles();
}
