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
    public static final String OK_RETURN_STRING = "OK";
    /**
     * @param timestamp
     * @return
     * @throws CRException
     */
    public abstract List getResourcesSinceTimestamp(Date timestamp) throws CRException;
    /**
     * @param criteria
     * @return
     * @throws CRException
     */
    public abstract List dataflowSearch(Map<String,String> criteria) throws CRException;
    /**
     *
     * @param content
     * @param sourceUri
     * @return
     * @throws CRException
     */
    public abstract String pushContent(String content, String sourceUri) throws CRException;
    /**
     *
     * @param criteria
     * @return
     * @throws CRException
     */
    public abstract Vector getEntries(Hashtable criteria) throws CRException;

    /**
     *
     * @param schemaIdentifier
     * @return
     * @throws CRException
     */
    public Vector getXmlFilesBySchema(String schemaIdentifier) throws CRException;

    /**
     *
     * @param pageNum
     * @param pageSize
     * @return
     * @throws CRException
     */
    public Vector getDeliveries(Integer pageNum, Integer pageSize) throws CRException;
}
