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

package eionet.cr.dao.virtuoso;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.repository.RepositoryConnection;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.FolderDAO;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.web.security.CRUser;

/**
 * Virtuoso implementation for the {@link FolderDAO}.
 *
 * @author Jaanus Heinlaid
 */
public class VirtuosoFolderDAO extends VirtuosoBaseDAO implements FolderDAO {

    /**
     * @see eionet.cr.dao.FolderDAO#createUserHomeFolder(java.lang.String)
     */
    @Override
    public void createUserHomeFolder(String userName) throws DAOException {

        if (StringUtils.isBlank(userName)){
            throw new IllegalArgumentException("User name must not be blank!");
        }
        CRUser user = new CRUser(userName);

        RepositoryConnection repoConn = null;
        try{
            repoConn = SesameUtil.getRepositoryConnection();
            repoConn.setAutoCommit(false);
            ValueFactory vf = repoConn.getValueFactory();

            // Check if user home already created (i.e. exists this triple: userHomeUri rdf:type cr:UserFolder).
            // If not created, create it.

            URI homeUri = vf.createURI(user.getHomeUri());
            URI rdfType = vf.createURI(Predicates.RDF_TYPE);
            Statement stmt = new StatementImpl(homeUri, rdfType, vf.createURI(Subjects.CR_USER_FOLDER));
            if (!repoConn.hasStatement(stmt, false, (Resource)null)){
                repoConn.add(stmt, homeUri);
            }

            // Check if reserved folders already existing. If not, then create them.
            URI hasFolder = vf.createURI(Predicates.CR_HAS_FOLDER);
            URI hasFile = vf.createURI(Predicates.CR_HAS_FILE);
            stmt = new StatementImpl(homeUri, hasFolder, vf.createURI(user.getReviewsUri()));
            if (!repoConn.hasStatement(stmt, false, (Resource)null)){
                repoConn.add(stmt, homeUri);
            }

            repoConn.commit();
        } catch (OpenRDFException e) {
            SesameUtil.rollback(repoConn);
            throw new DAOException(e.getMessage(), e);
        }
        finally{
            SesameUtil.close(repoConn);
        }
    }

    /**
     *
     * @param user
     * @return
     */
    private List<Statement> getHomeFolderCreationStatements(CRUser user, ValueFactory vf){


        URI homeUri = vf.createURI(user.getHomeUri());
        URI reviewsUri = vf.createURI(user.getReviewsUri());
        URI bookmarksUri = vf.createURI(user.getBookmarksUri());
        URI registrationsUri = vf.createURI(user.getRegistrationsUri());
        URI historyUri = vf.createURI(user.getHistoryUri());

        URI rdfType = vf.createURI(Predicates.RDF_TYPE);
        URI rdfsLabel = vf.createURI(Predicates.RDFS_LABEL);
        URI allowSubObjectType = vf.createURI(Predicates.CR_ALLOW_SUBOBJECT_TYPE);
        URI hasFolder = vf.createURI(Predicates.CR_HAS_FOLDER);
        URI hasFile = vf.createURI(Predicates.CR_HAS_FILE);

        URI folder = vf.createURI(Subjects.CR_FOLDER);
        URI file = vf.createURI(Subjects.CR_FILE);
        URI userFolder = vf.createURI(Subjects.CR_USER_FOLDER);
        URI reviewFolder = vf.createURI(Subjects.CR_REVIEW_FOLDER);
        URI bookmarksFile = vf.createURI(Subjects.CR_BOOKMARKS_FILE);
        URI registrationsFile = vf.createURI(Subjects.CR_REGISTRATIONS_FILE);
        URI historyFile = vf.createURI(Subjects.CR_HISTORY_FILE);

        Literal homeLabel = vf.createLiteral(user.getUserName() + "'s home");
        Literal reviewsLabel = vf.createLiteral(user.getUserName() + "'s reviews");
        Literal bookmarksLabel = vf.createLiteral(user.getUserName() + "'s bookmarks");
        Literal registrationsLabel = vf.createLiteral(user.getUserName() + "'s registrations");
        Literal historyLabel = vf.createLiteral(user.getUserName() + "'s history");

        ArrayList<Statement> result = new ArrayList<Statement>();

        // statements about home URI
        result.add(new ContextStatementImpl(homeUri, rdfType, userFolder, homeUri));
        result.add(new ContextStatementImpl(homeUri, rdfsLabel, homeLabel, homeUri));
        result.add(new ContextStatementImpl(homeUri, allowSubObjectType, folder, homeUri));
        result.add(new ContextStatementImpl(homeUri, allowSubObjectType, file, homeUri));
        result.add(new ContextStatementImpl(homeUri, hasFolder, reviewsUri, homeUri));
        result.add(new ContextStatementImpl(homeUri, hasFile, bookmarksUri, homeUri));
        result.add(new ContextStatementImpl(homeUri, hasFile, registrationsUri, homeUri));
        result.add(new ContextStatementImpl(homeUri, hasFile, historyUri, homeUri));

        // statements about reviews URI

        return result;
    }
}
