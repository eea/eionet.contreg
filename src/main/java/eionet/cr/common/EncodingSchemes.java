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
package eionet.cr.common;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class EncodingSchemes extends HashMap<String,String[]>{

    /** */
    private static Log logger = LogFactory.getLog(EncodingSchemes.class);

    /** */
    private static EncodingSchemes instance = null;

    /**
     *
     */
    private EncodingSchemes() {
        super();
    }

    /**
     *
     * @return
     */
    public static EncodingSchemes getInstance() {
        if (instance == null)
            instance = new EncodingSchemes();

        return instance;
    }

    /**
     *
     * @param id
     * @param servletContext
     * @return
     */
    public static String[] getLabels(String id) {

        return (id == null || id.trim().length() == 0) ? null : getInstance().get(id);
    }

    /**
     *
     * @param id
     * @return
     */
    public static String getLabel(String id) {
        return getLabel(id, false);
    }

    /**
     *
     * @param id
     * @param returnSelfIfNull
     * @return
     */
    public static String getLabel(String id, boolean returnSelfIfNull) {

        String[] labels = getLabels(id);
        return labels != null && labels.length>0 ? labels[0] : (returnSelfIfNull ? id : null);
    }

    /**
     *
     * @param id
     * @param labels
     * @param servletContext
     */
    public static synchronized void update(String id, String[] labels) {

        if (id == null || id.trim().length() == 0 || labels == null || labels.length == 0)
            return;

        getInstance().put(id, labels);
    }

    /**
     *
     * @return
     */
    public static int getCount() {
        return getInstance().size();
    }
}
