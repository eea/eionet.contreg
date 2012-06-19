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
package eionet.cr.api.xmlrpc;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import eionet.cr.common.CRException;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public interface Services {

    /** */
    String OK_RETURN_STRING = "OK";

    /**
     * This service returns metadata of all resources whose rdf:type is http://cr.eionet.europa.eu/ontologies/contreg.rdf#File and
     * which have been discovered by CR after the given timestamp.
     *
     * @param timestamp
     * @return
     * @throws CRException
     */
    List getResourcesSinceTimestamp(Date timestamp) throws CRException;

    /**
     * Returns an array of eionet.qawcommons.DataflowResultDto objects, where each represents a resource that matches the given
     * input criteria. A pre-defined and hard-coded criterion is that the returned resources are members of
     * http://rod.eionet.europa.eu/schema.rdf#Delivery class.
     *
     * @param criteria
     * @return
     * @throws CRException
     */
    List dataflowSearch(Map<String, String> criteria) throws CRException;

    /**
     * This method enables to push RDF content under particular harvest source in CR.
     *
     * @param content
     * @param sourceUri
     * @return
     * @throws CRException
     */
    String pushContent(String content, String sourceUri) throws CRException;

    /**
     * This service implements what getEntries did in the old Content Registry. It used to be called by ROD, but is now deprecated
     * and replaced by {@link #getDeliveries(Integer, Integer)}.
     *
     * The purpose is to return all metadata of all resources that match the given criteria. The criteria is given as a
     * <code>java.util.Hashtable</code>, where keys represent metadata attribute names and values represent their values. Data type
     * of both keys and values is <code>java.lang.String</code>.
     *
     * The method returns a <code>java.util.Vector</code> of type <code>java.util.Hashtable</code>. Every such hashtable represents
     * one resource that contains exactly 1 key that is a String that represents the resource's URI. The value is another
     * <code>java.lang.Hashtable</code> where the data type of keys is <code>java.lang.String</code> and the data type of values is
     * <code>java.util.Vector</code>. They keys represent URIs of the resource's attributes and the value-vectors represent values
     * of attributes. These values are of type <code>java.lang.String</code>.
     *
     * @param criteria
     * @return
     * @throws CRException
     */
    Vector getEntries(Hashtable criteria) throws CRException;

    /**
     * Returns metadata of resources which have a triple where the predicate is
     * http://cr.eionet.europa.eu/ontologies/contreg.rdf#xmlSchema (Predicates.CR_SCHEMA) and object matches the given input
     * parameter.
     *
     * The service to issue is "ContRegService.getXmlFilesBySchema". As input parameter, you give the string that contains the URI
     * of the DTD or XML Schema you are searching by. The client receives the result as Object[]. Each element in this array is
     * actually a java.util.Hashtable where the keys are metadata elements (i.e. predicates) and values are their values. Only two
     * keys (of type java.lang.String) are returned as of now.
     *
     *
     * @param schemaIdentifier
     * @return
     * @throws CRException
     */
    Vector getXmlFilesBySchema(String schemaIdentifier) throws CRException;

    /**
     * This service is currently used by ROD to get the list of deliveries registered in CR. ROD uses this to keep itself up to date
     * with what has been delivered for each obligation.
     *
     * @param pageNum the number of result set page to return
     * @param pageSize the result set page size to use
     * @return
     * @throws CRException
     */
    Vector getDeliveries(Integer pageNum, Integer pageSize) throws CRException;
}
