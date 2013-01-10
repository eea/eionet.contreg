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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.FolderDAO;
import eionet.cr.dao.readers.ResultSetReaderException;
import eionet.cr.dao.readers.UserFolderReader;
import eionet.cr.dto.FolderItemDTO;
import eionet.cr.util.Bindings;
import eionet.cr.util.FolderUtil;
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;
import eionet.cr.util.URIUtil;
import eionet.cr.util.URLUtil;
import eionet.cr.util.sesame.SPARQLQueryUtil;
import eionet.cr.util.sesame.SPARQLResultSetReader;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.web.security.CRUser;

/**
 * Virtuoso implementation for the {@link FolderDAO}.
 *
 * @author Jaanus Heinlaid
 */
public class VirtuosoFolderDAO extends VirtuosoBaseDAO implements FolderDAO {

    /** */
    private static final Logger LOGGER = Logger.getLogger(VirtuosoFolderDAO.class);

    /** */
    private static final String INSERT_NEVER_HARVESTED_SOURCE_SQL =
            "insert soft HARVEST_SOURCE (URL,URL_HASH,TIME_CREATED,INTERVAL_MINUTES) values (?,?,now(),0)";

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.FolderDAO#createUserHomeFolder(java.lang.String)
     */
    @Override
    public void createUserHomeFolder(String userName) throws DAOException {

        if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("User name must not be blank!");
        }
        CRUser user = new CRUser(userName);

        Connection sqlConn = null;
        RepositoryConnection repoConn = null;
        try {
            sqlConn = SesameUtil.getSQLConnection();
            sqlConn.setAutoCommit(false);

            repoConn = SesameUtil.getRepositoryConnection();
            repoConn.setAutoCommit(false);
            ValueFactory vf = repoConn.getValueFactory();

            List<Statement> statements = getHomeFolderCreationStatements(user, vf);
            repoConn.add(statements);

            createNeverHarvestedSources(sqlConn, statements);

            repoConn.commit();
            sqlConn.commit();

        } catch (OpenRDFException e) {
            SesameUtil.rollback(repoConn);
            throw new DAOException(e.getMessage(), e);
        } catch (SQLException e) {
            SQLUtil.rollback(sqlConn);
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(sqlConn);
            SesameUtil.close(repoConn);
        }
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.FolderDAO#createFolder(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void createFolder(String parentFolderUri, String folderName, String folderLabel, String homeUri) throws DAOException {

        // Make sure we have valid inputs.
        if (StringUtils.isBlank(parentFolderUri) || StringUtils.isBlank(folderName)) {
            throw new IllegalArgumentException("Parent folder URI and folder name must not be blank!");
        }

        // Remove trailing "/" from parent URI, if such exists
        if (parentFolderUri.endsWith("/")) {
            parentFolderUri = StringUtils.substringBeforeLast(parentFolderUri, "/");
        }

        // If the new folder URI is reserved, exit silently.
        String newFolderUri = parentFolderUri + "/" + URLUtil.escapeIRI(folderName);
        if (FolderUtil.isUserReservedUri(newFolderUri)) {
            LOGGER.debug("Cannot create reserved folder, exiting silently!");
            return;
        }

        Connection sqlConn = null;
        RepositoryConnection repoConn = null;
        try {
            sqlConn = SesameUtil.getSQLConnection();
            sqlConn.setAutoCommit(false);

            repoConn = SesameUtil.getRepositoryConnection();
            repoConn.setAutoCommit(false);
            ValueFactory vf = repoConn.getValueFactory();

            URI homeFolder = vf.createURI(homeUri);
            URI parentFolder = vf.createURI(parentFolderUri);
            URI hasFolder = vf.createURI(Predicates.CR_HAS_FOLDER);
            URI newFolder = vf.createURI(newFolderUri);
            URI rdfType = vf.createURI(Predicates.RDF_TYPE);
            URI rdfsLabel = vf.createURI(Predicates.RDFS_LABEL);
            URI allowSubObjectType = vf.createURI(Predicates.CR_ALLOW_SUBOBJECT_TYPE);
            Literal folderLabelLiteral = vf.createLiteral(folderLabel);
            URI folder = vf.createURI(Subjects.CR_FOLDER);
            URI file = vf.createURI(Subjects.CR_FILE);

            ArrayList<Statement> statements = new ArrayList<Statement>();
            statements.add(new ContextStatementImpl(parentFolder, hasFolder, newFolder, homeFolder));
            statements.add(new ContextStatementImpl(newFolder, rdfType, folder, homeFolder));
            if (StringUtils.isNotEmpty(folderLabel)) {
                statements.add(new ContextStatementImpl(newFolder, rdfsLabel, folderLabelLiteral, homeFolder));
            }
            statements.add(new ContextStatementImpl(newFolder, allowSubObjectType, folder, homeFolder));
            statements.add(new ContextStatementImpl(newFolder, allowSubObjectType, file, homeFolder));


            repoConn.add(statements);

            //if a new project is created add subfolders
            if (FolderUtil.isProjectRootFolder(parentFolderUri)) {
                repoConn.add( getProjectFolderCreationStatements(folderName, vf));
            }

            createNeverHarvestedSources(sqlConn, statements);

            repoConn.commit();
            sqlConn.commit();

        } catch (OpenRDFException e) {
            SesameUtil.rollback(repoConn);
            throw new DAOException(e.getMessage(), e);
        } catch (SQLException e) {
            SQLUtil.rollback(sqlConn);
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(sqlConn);
            SesameUtil.close(repoConn);
        }
    }

    /*
     * @see eionet.cr.dao.FolderDAO#folderExists(java.lang.String, java.lang.String)
     */
    @Override
    public boolean fileOrFolderExists(String parentFolderUri, String folderName) throws DAOException {

        // Make sure we have valid inputs.
        if (StringUtils.isBlank(parentFolderUri) || StringUtils.isBlank(folderName)) {
            throw new IllegalArgumentException("Parent folder URI and folder name must not be blank!");
        }

        // Prepend the parent folder with "/" if it's not done yet.
        if (!parentFolderUri.endsWith("/")) {
            parentFolderUri = parentFolderUri + "/";
        }

        return fileOrFolderExists(parentFolderUri + folderName);
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.FolderDAO#fileOrFolderExists(java.lang.String)
     */
    @Override
    public boolean fileOrFolderExists(String folderUri) throws DAOException {

        // Make sure we have valid inputs.
        if (StringUtils.isBlank(folderUri)) {
            throw new IllegalArgumentException("Folder URI must not be blank!");
        }

        String parentFolderUri = StringUtils.substringBeforeLast(folderUri, "/");
        Bindings bindings = new Bindings();
        bindings.setURI("parentFolder", parentFolderUri);
        bindings.setURI("folderUri", folderUri);
        bindings.setURI("crFolder", Subjects.CR_FOLDER);
        bindings.setURI("crHasFolder", Predicates.CR_HAS_FOLDER);
        bindings.setURI("crHasFile", Predicates.CR_HAS_FILE);

        StringBuilder sb = new StringBuilder();
        sb.append(SPARQLQueryUtil.getCrInferenceDefinitionStr());
        sb.append("select count(*) where { ");
        sb.append("?parentFolder ?p ?o . ");
        sb.append("filter (?p IN (?crHasFolder, ?crHasFile)) . ");
        sb.append("filter (?o = ?folderUri) }  ");

        String result = executeUniqueResultSPARQL(sb.toString(), bindings, new SingleObjectReader<String>());

        if (Integer.parseInt(result) > 0) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pair<FolderItemDTO, List<FolderItemDTO>> getFolderContents(String uri) throws DAOException {
        FolderItemDTO parentFolder = new FolderItemDTO();
        parentFolder.setUri(uri);
        parentFolder.setName(URIUtil.extractURILabel(parentFolder.getUri()));

        // Folder contents query
        Bindings bindings = new Bindings();
        bindings.setURI("folderUri", uri);
        bindings.setURI("hasFile", Predicates.CR_HAS_FILE);
        bindings.setURI("hasFolder", Predicates.CR_HAS_FOLDER);
        bindings.setURI("dcTitle", Predicates.DC_TITLE);
        bindings.setURI("rdfsLabel", Predicates.RDFS_LABEL);
        bindings.setURI("crLastModified", Predicates.CR_LAST_MODIFIED);

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ?type, ?item, ?label, ?lastModified WHERE { ");
        sb.append("?folderUri ?type ?item . ");
        sb.append("OPTIONAL { ?item ?rdfsLabel ?label } . ");
        sb.append("OPTIONAL { ?item ?crLastModified ?lastModified } . ");
        sb.append("FILTER (?type IN (?hasFile, ?hasFolder))");
        sb.append("}");

        SPARQLResultSetReader<FolderItemDTO> reader = new SPARQLResultSetReader<FolderItemDTO>() {
            List<FolderItemDTO> result = new ArrayList<FolderItemDTO>();

            @Override
            public List<FolderItemDTO> getResultList() {
                return result;
            }

            @Override
            public void endResultSet() {
            }

            @Override
            public void startResultSet(List<String> bindingNames) {
            }

            @Override
            public void readRow(BindingSet bindingSet) throws ResultSetReaderException {
                FolderItemDTO item = new FolderItemDTO();
                item.setUri(bindingSet.getValue("item").stringValue());
                item.setName(URIUtil.extractURILabel(item.getUri()));
                if (bindingSet.getValue("label") != null && StringUtils.isNotEmpty(bindingSet.getValue("label").stringValue())) {
                    item.setTitle(bindingSet.getValue("label").stringValue());
                }
                if (bindingSet.getValue("lastModified") != null
                        && StringUtils.isNotEmpty(bindingSet.getValue("lastModified").stringValue())) {
                    item.setLastModified(bindingSet.getValue("lastModified").stringValue());
                }
                if (Predicates.CR_HAS_FOLDER.equals(bindingSet.getValue("type").stringValue())) {
                    if (FolderUtil.isUserReservedUri(item.getUri())) {
                        item.setType(FolderItemDTO.Type.RESERVED_FOLDER);
                    } else {
                        item.setType(FolderItemDTO.Type.FOLDER);
                    }
                }
                if (Predicates.CR_HAS_FILE.equals(bindingSet.getValue("type").stringValue())) {
                    if (FolderUtil.isUserReservedUri(item.getUri())) {
                        item.setType(FolderItemDTO.Type.RESERVED_FILE);
                    } else {
                        item.setType(FolderItemDTO.Type.FILE);
                    }
                }
                result.add(item);
            }
        };

        List<FolderItemDTO> items = executeSPARQL(sb.toString(), bindings, reader);
        Collections.sort(items);

        return new Pair<FolderItemDTO, List<FolderItemDTO>>(parentFolder, items);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSubFolders(String uri) throws DAOException {

        List<String> ret = new ArrayList<String>();
        ret.add(uri);

        Bindings bindings = new Bindings();
        bindings.setURI("uri", uri);
        bindings.setURI("hasFolder", Predicates.CR_HAS_FOLDER);

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ?o WHERE { ");
        sb.append("graph ?g {");
        sb.append("?s ?p ?o . ");
        sb.append("FILTER (?g = ?uri) .");
        sb.append("FILTER (?p = ?hasFolder) .");
        sb.append("}} ORDER BY ?o");
        List<String> folders = executeSPARQL(sb.toString(), bindings, new UserFolderReader());
        if (folders != null) {
            ret.addAll(folders);
        }

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean folderHasItems(String folderUri) throws DAOException {
        Bindings bindings = new Bindings();
        bindings.setURI("folderUri", folderUri);
        bindings.setURI("hasFile", Predicates.CR_HAS_FILE);
        bindings.setURI("hasFolder", Predicates.CR_HAS_FOLDER);

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT count(*) WHERE { ");
        sb.append("?folderUri ?type ?item . ");
        sb.append("FILTER (?type IN (?hasFile, ?hasFolder))");
        sb.append("}");

        String result = executeUniqueResultSPARQL(sb.toString(), bindings, new SingleObjectReader<String>());

        if (Integer.parseInt(result) > 0) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteFileOrFolderUris(String folderUri, List<String> subjectUris) throws DAOException {

        if (subjectUris == null || subjectUris.size() == 0) {
            return;
        }

        Connection sqlConn = null;
        RepositoryConnection repoConn = null;
        try {
            repoConn = SesameUtil.getRepositoryConnection();
            ValueFactory valueFactory = repoConn.getValueFactory();

            StringBuilder strBuilder = new StringBuilder();
            for (String subjectUri : subjectUris) {

                URI subjectResource = valueFactory.createURI(subjectUri);
                URI folderContext = valueFactory.createURI(folderUri);
                URI harvesterContext = valueFactory.createURI(GeneralConfig.HARVESTER_URI);

                // JH190511: although Sesame API claims that in RepositoryConnection.remove(...)
                // the context is optional, Virtuoso requires the context (i.e. the graph) always
                // to be specified in triple removal commands. Virtuoso Sesame driver seems to
                // silently ignore the whole command, if no context specified.
                repoConn.remove(subjectResource, null, null, folderContext, harvesterContext);
                repoConn.remove(null, null, (Value) subjectResource, folderContext, harvesterContext);

                if (strBuilder.length() > 0) {
                    strBuilder.append(",");
                }
                strBuilder.append(Hashes.spoHash(subjectUri));
            }

            sqlConn = getSQLConnection();
            SQLUtil.executeUpdate("delete from SPO_BINARY where SUBJECT in (" + strBuilder + ")", sqlConn);

        } catch (RepositoryException e) {
            throw new DAOException(e.toString(), e);
        } catch (SQLException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SesameUtil.close(repoConn);
            SQLUtil.close(sqlConn);
        }
    }


    private List<Statement> getProjectFolderCreationStatements(String projectName, ValueFactory vf) {

        String projectRootFolder = FolderUtil.getProjectsFolder();

        String projectFolder = FolderUtil.getProjectFolder(projectName);
        String bookmarksFolder = projectFolder + "/bookmarks";

        URI projectsRootURI = vf.createURI(projectRootFolder);
        URI projectHomeUri = vf.createURI(projectFolder);
        URI rdfType = vf.createURI(Predicates.RDF_TYPE);
        URI rdfsLabel = vf.createURI(Predicates.RDFS_LABEL);

        URI hasFile = vf.createURI(Predicates.CR_HAS_FILE);
        URI bookmarksFile = vf.createURI(Subjects.CR_BOOKMARKS_FILE);

        URI bookmarksUri = vf.createURI(bookmarksFolder);

        Literal bookmarksLabel = vf.createLiteral(projectName + "'s bookmarks");

        ArrayList<Statement> result = new ArrayList<Statement>();
        result.add(new ContextStatementImpl(projectHomeUri, hasFile, bookmarksUri, projectsRootURI));
        result.add(new ContextStatementImpl(bookmarksUri, rdfType, bookmarksFile, projectsRootURI));
        result.add(new ContextStatementImpl(bookmarksUri, rdfsLabel, bookmarksLabel, projectsRootURI));


        return result;

    }
    /**
     *
     * @param user
     * @return
     */
    private List<Statement> getHomeFolderCreationStatements(CRUser user, ValueFactory vf) {

        URI rootHomeUri = vf.createURI(CRUser.rootHomeUri());
        URI rootProjectUri = vf.createURI(CRUser.rootProjectUri());
        URI userHomeUri = vf.createURI(user.getHomeUri());
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

        // statements about all user homes root (e.g. http://cr.eionet.europa.eu/home)
        result.add(new ContextStatementImpl(rootHomeUri, hasFolder, userHomeUri, rootHomeUri));

        // statements about project root (e.g. http://cr.eionet.europa.eu/project)
        result.add(new ContextStatementImpl(rootProjectUri, rdfType, folder, rootProjectUri));

        // statements about user home URI
        result.add(new ContextStatementImpl(userHomeUri, rdfType, userFolder, userHomeUri));
        result.add(new ContextStatementImpl(userHomeUri, rdfsLabel, homeLabel, userHomeUri));
        result.add(new ContextStatementImpl(userHomeUri, allowSubObjectType, folder, userHomeUri));
        result.add(new ContextStatementImpl(userHomeUri, allowSubObjectType, file, userHomeUri));
        result.add(new ContextStatementImpl(userHomeUri, hasFolder, reviewsUri, userHomeUri));
        result.add(new ContextStatementImpl(userHomeUri, hasFile, bookmarksUri, userHomeUri));
        result.add(new ContextStatementImpl(userHomeUri, hasFile, registrationsUri, userHomeUri));
        result.add(new ContextStatementImpl(userHomeUri, hasFile, historyUri, userHomeUri));

        // statements about reviews URI
        result.add(new ContextStatementImpl(reviewsUri, rdfType, reviewFolder, userHomeUri));
        result.add(new ContextStatementImpl(reviewsUri, rdfsLabel, reviewsLabel, userHomeUri));
        result.add(new ContextStatementImpl(reviewsUri, allowSubObjectType, folder, userHomeUri));
        result.add(new ContextStatementImpl(reviewsUri, allowSubObjectType, file, userHomeUri));

        // statements about bookmarks URI
        result.add(new ContextStatementImpl(bookmarksUri, rdfType, bookmarksFile, userHomeUri));
        result.add(new ContextStatementImpl(bookmarksUri, rdfsLabel, bookmarksLabel, userHomeUri));

        // statements about registrations URI
        result.add(new ContextStatementImpl(registrationsUri, rdfType, registrationsFile, userHomeUri));
        result.add(new ContextStatementImpl(registrationsUri, rdfsLabel, registrationsLabel, userHomeUri));

        // statements about history URI
        result.add(new ContextStatementImpl(historyUri, rdfType, historyFile, userHomeUri));
        result.add(new ContextStatementImpl(historyUri, rdfsLabel, historyLabel, userHomeUri));

        return result;
    }

    /**
     * @param sqlConn
     * @param statements
     * @throws SQLException
     */
    private void createNeverHarvestedSources(Connection sqlConn, List<Statement> statements) throws SQLException {

        // Create harvest sources for all distinct contexts found in the above statements.
        // Doing it one-by-one for convenience, because the number distinct contexts in the
        // given statements should never be more than a couple or so.

        HashSet<String> sourcesDone = new HashSet<String>();
        for (Statement statement : statements) {

            String sourceUrl = statement.getContext().stringValue();
            if (!sourcesDone.contains(sourceUrl)) {
                List<Object> values = new ArrayList<Object>();
                values.add(sourceUrl);
                values.add(Hashes.spoHash(sourceUrl));
                SQLUtil.executeUpdate(INSERT_NEVER_HARVESTED_SOURCE_SQL, values, sqlConn);
                sourcesDone.add(sourceUrl);
            }
        }
    }


    public void createProjectBookmarksFolder(String projectName) throws DAOException {

        Connection sqlConn = null;
        RepositoryConnection repoConn = null;
        try {
            sqlConn = SesameUtil.getSQLConnection();
            sqlConn.setAutoCommit(false);

            repoConn = SesameUtil.getRepositoryConnection();
            repoConn.setAutoCommit(false);
            ValueFactory vf = repoConn.getValueFactory();

            List<Statement> statements = getProjectFolderCreationStatements(projectName, vf);
            repoConn.add(statements);

            repoConn.commit();
            sqlConn.commit();

        } catch (OpenRDFException e) {
            SesameUtil.rollback(repoConn);
            throw new DAOException(e.getMessage(), e);
        } catch (SQLException e) {
            SQLUtil.rollback(sqlConn);
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(sqlConn);
            SesameUtil.close(repoConn);
        }
    }

}
