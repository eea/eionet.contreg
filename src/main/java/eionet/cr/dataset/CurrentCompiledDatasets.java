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
package eionet.cr.dataset;

import java.util.HashMap;

/**
 *
 * @author altnyris
 *
 */
public class CurrentCompiledDatasets {

    /** */
    private static HashMap<String, String> compiledDatasets;

    /**
     *
     */
    static {
        compiledDatasets = new HashMap<String, String>();
    }

    /**
     *
     * @param url
     * @param user
     */
    public static synchronized void addCompiledDataset(String url, String user) {

        if (url != null && user != null) {
            compiledDatasets.put(url, user);
        }
    }

    /**
     *
     * @param url
     */
    public static synchronized void removeCompiledDataset(String url) {
        if (url != null) {
            compiledDatasets.remove(url);
        }
    }

    /**
     *
     * @param url
     * @return
     */
    public static synchronized boolean contains(String url) {

        if (url == null) {
            return false;
        }

        if (compiledDatasets.containsKey(url)) {
            return true;
        }

        return false;
    }
}
