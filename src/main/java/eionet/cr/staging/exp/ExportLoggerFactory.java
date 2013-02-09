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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        jaanus
 */

package eionet.cr.staging.exp;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

/**
 * An implementation of {@link LoggerFactory} that returns instances of {@link ExportLogger}.
 *
 * @author jaanus
 */
public class ExportLoggerFactory implements LoggerFactory {

    /** */
    public static final ExportLoggerFactory INSTANCE = new ExportLoggerFactory();

    /**
     * Hide the constructor.
     */
    private ExportLoggerFactory() {
        // Hide the constructor.
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.log4j.spi.LoggerFactory#makeNewLoggerInstance(java.lang.String)
     */
    @Override
    public Logger makeNewLoggerInstance(String name) {
        return new ExportLogger(name);
    }

}
