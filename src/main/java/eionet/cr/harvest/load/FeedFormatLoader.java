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
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */

package eionet.cr.harvest.load;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryConnection;

/**
 * Implementation of {@link ContentLoader} for the content in RRS/Atom format (i.e. feed formats).
 *
 * @author Jaanus Heinlaid
 */
public class FeedFormatLoader implements ContentLoader {

    /*
     * (non-Javadoc)
     * @see eionet.cr.harvest.load.ContentLoader#load(java.io.InputStream, org.openrdf.repository.RepositoryConnection, java.sql.Connection, java.lang.String, java.lang.String)
     */
    @Override
    public int load(InputStream inputStream, RepositoryConnection repoConn, Connection sqlConn, String baseUri, String contextUri)
            throws IOException, OpenRDFException, ContentParsingException {

        FeedSaver feedSaver = new FeedSaver(repoConn, sqlConn, contextUri);
        feedSaver.save(inputStream);
        return feedSaver.getNumberOfTriplesSaved();
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.harvest.load.ContentLoader#setTimeout(long)
     */
    @Override
    public void setTimeout(long timeout) {
        // not implemented yet
    }

}
