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

package eionet.cr.staging.imp;

import java.io.File;

/**
 * Implementors of this interface are capable of importing a given database file into a "real" staging database in the underlying
 * DBMS.
 *
 * @author jaanus
 */
public interface ImporterIF {

    /**
     * Imports a staging database from the given file into a real DBMS database by the given name.
     *
     * @param file the file
     * @param dbName the db name
     * @throws ImportException the import exception
     */
    void doImport(File file, String dbName) throws ImportException;
}
