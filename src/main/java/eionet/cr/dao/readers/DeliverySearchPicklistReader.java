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
package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.dao.util.UriLabelPair;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class DeliverySearchPicklistReader<T> extends ResultSetMixedReader<T> {

    /** */
    // KL 170611 - why does it not return resultList as the rest of readers?
    /** */
    // private LinkedHashMap<String, ArrayList<UriLabelPair>> resultMap = new LinkedHashMap<String, ArrayList<UriLabelPair>>();
    private LinkedHashMap<UriLabelPair, ArrayList<UriLabelPair>> resultMap =
            new LinkedHashMap<UriLabelPair, ArrayList<UriLabelPair>>();

    /** */
    private String currentInstrumentUri = null;
    private ArrayList<UriLabelPair> currentObligations = null;

    /**
     * @return the resultMap
     */
    public HashMap<UriLabelPair, ArrayList<UriLabelPair>> getResultMap() {
        return resultMap;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query .BindingSet)
     */
    @Override
    public void readRow(BindingSet bindingSet) {
        Value instrument = bindingSet.getValue("li_uri");
        String instrumentUri = instrument.stringValue();
        String instrumentTitle = bindingSet.getValue("li_title").stringValue();

        if (currentInstrumentUri == null || !currentInstrumentUri.equals(instrumentUri)) {
            currentObligations = new ArrayList<UriLabelPair>();
            currentInstrumentUri = instrumentUri;
            resultMap.put(UriLabelPair.create(currentInstrumentUri, instrumentTitle), currentObligations);
        }
        currentObligations.add(UriLabelPair.create(bindingSet.getValue("ro_uri").stringValue(), bindingSet.getValue("ro_title")
                .stringValue()));

    }

    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {
        // TODO Auto-generated method stub

    }

}
