package eionet.cr.web.action;

import eionet.cr.ApplicationTestContext;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.*;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.filestore.FileStore;
import eionet.cr.harvest.util.CsvImportUtil;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.FolderUtil;
import eionet.cr.util.TestUtils;
import eionet.cr.web.security.CRUser;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.Message;
import net.sourceforge.stripes.mock.MockHttpServletResponse;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A class for testing the behavior of {@link UploadCSVActionBean}.
 *
 * @author Jaanus
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class UploadCSVActionBeanIT extends CRDatabaseTestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadCSVActionBeanIT.class);

    /** The name of user whose folder we're testing in. */
    public static final String TEST_USER_NAME = "somebody";

    /** The name of seeded CSV file we're testing. */
    public static final String TEST_FILE_NAME = "download.csv";

    /** Abstract reference to the file under test. */
    public static final File TEST_FILE = getTestFile(TEST_FILE_NAME);

    /** The name of seeded CSV file we're testing, spaces escaped */
    public static final String TEST_FILE_NAME_ESCAPED = StringUtils.replace(TEST_FILE_NAME, " ", "%20");

    public static final String TEST_ONLINE_FILE_NAME = "download.csv";

    public static final String TEST_ONLINE_FILE_NAME_ESCAPED = StringUtils.replace(TEST_ONLINE_FILE_NAME, " ", "%20");

    /** The URI of the virtual CR folder we're testing in. */
    private String testFolderUri;

    /** The URI of the uploaded file we're testing. */
    private String testFileUri;

    /** Expected SPARQL query to be generated for the file when data-linking scripts used. Ignore whitespace when comparing! */
    private String expectedSparqlQuery;

    private String testOnlineFileUri;

    /** Size of the file under test before test. */
    private long testFileSize;

    @Autowired
    private MockServletContext ctx;

    @Override
    @Before
    public void setUp() throws Exception {

        super.setUp();

        testFileSize = TEST_FILE.length();

        testFolderUri = CRUser.rootHomeUri() + "/" + TEST_USER_NAME;
        testFileUri = testFolderUri + "/" + TEST_FILE_NAME_ESCAPED;
        testOnlineFileUri = TestUtils.getFileUrl(TEST_ONLINE_FILE_NAME_ESCAPED);

        expectedSparqlQuery = "" +
                "PREFIX tableFile: <" + testFolderUri + "/" + TEST_FILE_NAME_ESCAPED + "#> " +
                "SELECT * FROM <" + testFolderUri + "/" + TEST_FILE_NAME + "> " +
                "WHERE {" +
                "    OPTIONAL { _:rec tableFile:Presidency ?Presidency } ." +
                "    OPTIONAL { _:rec tableFile:President ?President } ." +
                "    OPTIONAL { _:rec tableFile:Wikipedia_Entry ?Wikipedia_Entry } ." +
                "    OPTIONAL { _:rec tableFile:Took_office ?Took_office } ." +
                "    OPTIONAL { _:rec tableFile:Left_office ?Left_office } ." +
                "    OPTIONAL { _:rec tableFile:Party ?Party } ." +
                "    OPTIONAL { _:rec tableFile:Portrait ?Portrait } ." +
                "    OPTIONAL { _:rec tableFile:Thumbnail ?Thumbnail } . " +
                "    OPTIONAL { _:rec tableFile:Home_State ?Home_State } ." +
                "}";

        ActionBeanUtils.addFilter(ctx);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        ActionBeanUtils.clearFilters(ctx);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#forceClearTriplesOnSetup()
     */
    @Override
    protected boolean forceClearTriplesOnSetup() {
        return true;
    }

    /**
     * A test for testing two uploads/saves of one and the same file in a row in overwrite mode.
     * First upload/save is WITH data linking scripts, the second one is without.
     *
     * @throws Exception Any sort of error that happens.
     */
    @Test
    public void testTwoUploadsInARow() throws Exception {

        // First, make a backup of the file under test, because we shall create a Stripes FileBean from it and the latter will
        // remove after the "upload". But we shall need it once more for the second run of the upload/save chain.
        File backupFile = new File(TEST_FILE.getParentFile(), TEST_FILE.getName() + ".backup");
        if (TEST_FILE.exists()) {
            if (!backupFile.exists()) {
                FileUtils.copyFile(TEST_FILE, backupFile);
            }
        } else {
            if (backupFile.exists()) {
                FileUtils.copyFile(backupFile, TEST_FILE);
            } else {
                throw new IllegalStateException("Test seed file has gone and no backup file is present either!");
            }
        }
        testFileSize = TEST_FILE.length();

        // Now do a run WITH data linking script(s).
        ArrayList<DataLinkingScript> dataLinkingScripts = new ArrayList<DataLinkingScript>();
        dataLinkingScripts.add(DataLinkingScript.create("Thumbnail", "deleteColumn"));
        doUploadSave(dataLinkingScripts, expectedSparqlQuery, true);

        // Now restore the file-under-test from the above-made backup if indeed it has been deleted as noted above.
        if (!TEST_FILE.exists()) {
            if (backupFile.exists()) {
                FileUtils.copyFile(backupFile, TEST_FILE);
            } else {
                throw new IllegalStateException("Test seed file has gone and no backup file is present either!");
            }
        }

        // Now do a run WITHOUT data linking script(s).
        doUploadSave(null, expectedSparqlQuery, true);
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testBrandNewUpload() throws Exception {

        deleteUploadedFile();

        // First, make a backup of the file under test, because we shall create a Stripes FileBean from it and the latter will
        // remove after the "upload". But we shall need it once more for the second run of the upload/save chain.
        File backupFile = new File(TEST_FILE.getParentFile(), TEST_FILE.getName() + ".backup");
        if (TEST_FILE.exists()) {
            if (!backupFile.exists()) {
                FileUtils.copyFile(TEST_FILE, backupFile);
            }
        } else {
            if (backupFile.exists()) {
                FileUtils.copyFile(backupFile, TEST_FILE);
            } else {
                throw new IllegalStateException("Test seed file has gone and no backup file is present either!");
            }
        }
        testFileSize = TEST_FILE.length();

        // Now do a run WITH data linking script(s).
        ArrayList<DataLinkingScript> dataLinkingScripts = new ArrayList<DataLinkingScript>();
        dataLinkingScripts.add(DataLinkingScript.create("Thumbnail", "deleteColumn"));
        doUploadSave(dataLinkingScripts, expectedSparqlQuery, true);

        // Try harvesting the freshly uploaded file.
        SubjectDTO subject = DAOFactory.get().getDao(HelperDAO.class).getSubject(testFileUri);
        assertNotNull("Expected existing subject for " + testFileUri, subject);
        try {
            List<String> harvestWarnings = CsvImportUtil.harvestTableFile(subject, "heinlja");
            assertTrue("Was expecting no harvest warnings!", CollectionUtils.isEmpty(harvestWarnings));
        } catch (Exception e) {
            fail("Was not expecting this exception: " + e.toString());
        }
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testOnlineSource() throws Exception {

        deleteUploadedFileOnline();

        // Now do a run WITH data linking script(s).
        ArrayList<DataLinkingScript> dataLinkingScripts = new ArrayList<DataLinkingScript>();
        dataLinkingScripts.add(DataLinkingScript.create("Thumbnail", "deleteColumn"));
        doUploadSaveOnline(dataLinkingScripts, expectedSparqlQuery, true);

        // Try harvesting the freshly uploaded file.
        SubjectDTO subject = DAOFactory.get().getDao(HelperDAO.class).getSubject(testFileUri);
        assertNotNull("Expected existing subject for " + testFileUri, subject);
        try {
            List<String> harvestWarnings = CsvImportUtil.harvestTableFile(subject, "heinlja");
            assertTrue("Was expecting no harvest warnings!", CollectionUtils.isEmpty(harvestWarnings));
        } catch (Exception e) {
            fail("Was not expecting this exception: " + e.toString());
        }
    }

    /**
     * Deletes the file (from file-store) denoted by {@link #TEST_FILE} and its metadata.
     *
     * @throws DAOException If any sort of data access error happens.
     */
    private void deleteUploadedFile() throws DAOException {

        FolderDAO folderDAO = DAOFactory.get().getDao(FolderDAO.class);
        HarvestSourceDAO harvestSourceDAO = DAOFactory.get().getDao(HarvestSourceDAO.class);

        folderDAO.deleteFileOrFolderUris(testFolderUri, Collections.singletonList(testFileUri));
        harvestSourceDAO.removeHarvestSources(Collections.singletonList(testFileUri));

        FileStore fileStore = FileStore.getInstance(TEST_USER_NAME);
        fileStore.delete(FolderUtil.extractPathInUserHome(testFolderUri + "/" + TEST_FILE_NAME));
    }

    /**
     * Deletes the file (from file-store) denoted by {@link #TEST_ONLINE_FILE_NAME} and its metadata.
     *
     * @throws DAOException If any sort of data access error happens.
     */
    private void deleteUploadedFileOnline() throws DAOException {

        FolderDAO folderDAO = DAOFactory.get().getDao(FolderDAO.class);
        HarvestSourceDAO harvestSourceDAO = DAOFactory.get().getDao(HarvestSourceDAO.class);

        folderDAO.deleteFileOrFolderUris(testFolderUri, Collections.singletonList(testOnlineFileUri));
        harvestSourceDAO.removeHarvestSources(Collections.singletonList(testOnlineFileUri));

        FileStore fileStore = FileStore.getInstance(TEST_USER_NAME);
        fileStore.delete(FolderUtil.extractPathInUserHome(testFolderUri + "/" + TEST_ONLINE_FILE_NAME));
    }

    /**
     * Does and tests the file's upload and save, using the given data linking scripts and expecting the given SPARQL query
     * to be generated for the file. The upload is done with or without overwrite, depending on the given boolean.
     *
     * @param dataLinkingScripts The data-linking scripts.
     * @param expectedSparql The expected SPARQL to be generated.
     * @param isOverwrite Overwrite or not.
     *
     * @throws Exception Any sort of error that happens.
     */
    private void doUploadSave(ArrayList<DataLinkingScript> dataLinkingScripts, String expectedSparql, boolean isOverwrite)
            throws Exception {

        doUpload(isOverwrite);
        doSave(dataLinkingScripts, expectedSparql);
    }

    /**
     * Does and tests the file's upload and save, using the given data linking scripts and expecting the given SPARQL query
     * to be generated for the file. The upload is done with or without overwrite, depending on the given boolean.
     *
     * @param dataLinkingScripts The data-linking scripts.
     * @param expectedSparql The expected SPARQL to be generated.
     * @param isOverwrite Overwrite or not.
     *
     * @throws Exception Any sort of error that happens.
     */
    private void doUploadSaveOnline(ArrayList<DataLinkingScript> dataLinkingScripts, String expectedSparql, boolean isOverwrite)
            throws Exception {

        doUploadOnline(isOverwrite);
        doSave(dataLinkingScripts, expectedSparql);
    }

    /**
     * Does and tests the file's "upload" step, doing it with or without overwrite, depending on the given boolean.
     *
     * @param isOverwrite Overwrite or not.
     * @throws Exception Any sort of error that happens.
     */
    private void doUpload(boolean isOverwrite) throws Exception {

        // Prepare the servlet context mock + Stripes action bean roundtrip.
        MockRoundtrip trip = new MockRoundtrip(ctx, UploadCSVActionBeanMock.class);

        // Prepare rich-type (e.g. file bean) request parameters. These will be picked up by CRActionBeanPropertyBinder
        // that has already been injected into the servlet context mock obtained above.
        HashMap<String, Object> richTypeRequestParams = new HashMap<String, Object>();
        trip.getRequest().setAttribute(CRActionBeanPropertyBinder.RICH_TYPE_REQUEST_PARAMS_ATTR_NAME, richTypeRequestParams);

        // Prepare simple string-based request parameters.
        trip.setParameter("fileURL", testOnlineFileUri);
        trip.setParameter("folderUri", testFolderUri);
        trip.setParameter("overwrite", String.valueOf(isOverwrite));
        trip.setParameter("fileType", "CSV");
        trip.setParameter("fileEncoding", "UTF-8");

        // Execute the event.
        trip.execute("upload");

        // Assert the returned HTTP code.
        UploadCSVActionBean actionBean = trip.getActionBean(UploadCSVActionBeanMock.class);
        MockHttpServletResponse response = (MockHttpServletResponse) actionBean.getContext().getResponse();
        assertEquals(200, response.getStatus());

        // Assert that the file was created in CR filestore.
        File storedFile = FileStore.getByUri(testFileUri);
        assertNotNull("Didn't expect the stored file to be null!", storedFile);
        long storedFileSize = storedFile.length();
        boolean flag =
                storedFile != null && storedFile.exists() && storedFile.isFile() && TEST_FILE_NAME.equals(storedFile.getName());
        assertTrue("Expected an existing stored file with name: " + TEST_FILE_NAME, flag);
        assertEquals("Expected stored file size to be equal to the uploaded file size", testFileSize, storedFileSize);

        // Assert existence of various triples about the uploaded file and its parent folder.
        String[] statement1 = {testFolderUri, Predicates.CR_HAS_FILE, testFileUri};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement1), hasResourceStatement(statement1));

        boolean b = hasLiteralStatement(testFileUri, Predicates.CR_BYTE_SIZE, String.valueOf(storedFileSize), XMLSchema.INT);
        assertTrue("Missing or unexpected value for " + Predicates.CR_BYTE_SIZE, b);

        String[] statement3 = {testFileUri, Predicates.RDF_TYPE, Subjects.CR_TABLE_FILE};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement3), hasResourceStatement(statement3));

        String[] statement4 = {testFileUri, Predicates.CR_MEDIA_TYPE, "CSV"};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement4), hasLiteralStatement(statement4));

        String[] statement5 = {testFileUri, Predicates.CR_LAST_MODIFIED, null};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement5), hasLiteralStatement(statement5));

        SubjectDTO testFileSubject = DAOFactory.get().getDao(HelperDAO.class).getSubject(testFileUri);
        assertNotNull("Expected existing subject for " + testFileUri, testFileSubject);
        ObjectDTO byteSizeLiteral = testFileSubject.getObject(Predicates.CR_BYTE_SIZE);
        assertNotNull("Expected a literal for " + Predicates.CR_BYTE_SIZE, byteSizeLiteral);
        URI datatype = byteSizeLiteral.getDatatype();
        assertNotNull("Expected a datatype for the value of " + Predicates.CR_BYTE_SIZE, datatype);
        assertEquals("Unexpected datatype", XMLSchema.INT.stringValue(), datatype.stringValue());
    }

    /**
     * Does and tests the file's "upload" step, doing it with or without overwrite, depending on the given boolean.
     *
     * @param isOverwrite Overwrite or not.
     * @throws Exception Any sort of error that happens.
     */
    private void doUploadOnline(boolean isOverwrite) throws Exception {

        // Prepare the servlet context mock + Stripes action bean roundtrip.
        MockRoundtrip trip = new MockRoundtrip(ctx, UploadCSVActionBeanMock.class);

        // Prepare rich-type (e.g. file bean) request parameters. These will be picked up by CRActionBeanPropertyBinder
        // that has already been injected into the servlet context mock obtained above.
        HashMap<String, Object> richTypeRequestParams = new HashMap<String, Object>();
        trip.getRequest().setAttribute(CRActionBeanPropertyBinder.RICH_TYPE_REQUEST_PARAMS_ATTR_NAME, richTypeRequestParams);

        // Prepare simple string-based request parameters.
        trip.setParameter("fileURL", testOnlineFileUri);
        trip.setParameter("folderUri", testFolderUri);
        trip.setParameter("overwrite", String.valueOf(isOverwrite));
        trip.setParameter("fileType", "CSV");
        trip.setParameter("fileEncoding", "UTF-8");

        // Execute the event.
        trip.execute("upload");

        // Assert the returned HTTP code.
        UploadCSVActionBean actionBean = trip.getActionBean(UploadCSVActionBeanMock.class);
        MockHttpServletResponse response = (MockHttpServletResponse) actionBean.getContext().getResponse();
        assertEquals(200, response.getStatus());

        // Assert that the file was created in CR filestore.
        File storedFile = FileStore.getByUri(testFileUri);
        assertNotNull("Didn't expect the stored file to be null!", storedFile);
        long storedFileSize = storedFile.length();
        boolean flag =
                storedFile != null && storedFile.exists() && storedFile.isFile() && TEST_ONLINE_FILE_NAME.equals(storedFile.getName());
        assertTrue("Expected an existing stored file with name: " + TEST_ONLINE_FILE_NAME, flag);

        // Assert existence of various triples about the uploaded file and its parent folder.
        String[] statement1 = {testFolderUri, Predicates.CR_HAS_FILE, testFileUri};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement1), hasResourceStatement(statement1));

        boolean b = hasLiteralStatement(testFileUri, Predicates.CR_BYTE_SIZE, String.valueOf(storedFileSize), XMLSchema.INT);
        assertTrue("Missing or unexpected value for " + Predicates.CR_BYTE_SIZE, b);

        String[] statement3 = {testFileUri, Predicates.RDF_TYPE, Subjects.CR_TABLE_FILE};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement3), hasResourceStatement(statement3));

        String[] statement4 = {testFileUri, Predicates.CR_MEDIA_TYPE, "CSV"};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement4), hasLiteralStatement(statement4));

        String[] statement5 = {testFileUri, Predicates.CR_LAST_MODIFIED, null};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement5), hasLiteralStatement(statement5));

        SubjectDTO testFileSubject = DAOFactory.get().getDao(HelperDAO.class).getSubject(testFileUri);
        assertNotNull("Expected existing subject for " + testFileUri, testFileSubject);
        ObjectDTO byteSizeLiteral = testFileSubject.getObject(Predicates.CR_BYTE_SIZE);
        assertNotNull("Expected a literal for " + Predicates.CR_BYTE_SIZE, byteSizeLiteral);
        URI datatype = byteSizeLiteral.getDatatype();
        assertNotNull("Expected a datatype for the value of " + Predicates.CR_BYTE_SIZE, datatype);
        assertEquals("Unexpected datatype", XMLSchema.INT.stringValue(), datatype.stringValue());
    }

    /**
     * Does and tests the "save" step, using the given data-linking scripts and expecting the given SPARQL to be generated.
     *
     * @param dataLinkingScripts The given data-linking scripts.
     * @param expectedSparql Expecting this SPARQL query to be generated for the saved file.
     * @throws Exception For any sort of problems.
     */
    private void doSave(ArrayList<DataLinkingScript> dataLinkingScripts, String expectedSparql) throws Exception {
        MockRoundtrip trip = new MockRoundtrip(ctx, UploadCSVActionBeanMock.class);

        // Prepare the rich-type request parameters: the given data-linking scripts (if any)
        // and specifying the list of columns that will constitute unique key.
        HashMap<String, Object> richTypeRequestParams = new HashMap<String, Object>();
        if (CollectionUtils.isNotEmpty(dataLinkingScripts)) {
            richTypeRequestParams.put("dataLinkingScripts", dataLinkingScripts);
        }
        richTypeRequestParams.put("uniqueColumns", Collections.singletonList("Presidency"));
        trip.getRequest().setAttribute(CRActionBeanPropertyBinder.RICH_TYPE_REQUEST_PARAMS_ATTR_NAME, richTypeRequestParams);

        if (CollectionUtils.isNotEmpty(dataLinkingScripts)) {
            trip.setParameter("addDataLinkingScripts", "true");
        }
        trip.setParameter("overwrite", "true");

        trip.setParameter("attribution", "testCopyright");
        trip.setParameter("license", "All rights reserved");
        trip.setParameter("publisher", "testPublisher");
        trip.setParameter("source", "testSource");

        trip.setParameter("fileType", "CSV");
        trip.setParameter("objectsType", "President");
        trip.setParameter("fileEncoding", "UTF-8");
        trip.setParameter("finalEncoding", "UTF-8");

        trip.setParameter("fileName", TEST_FILE_NAME);
        trip.setParameter("fileLabel", TEST_FILE_NAME);
        trip.setParameter("folderUri", testFolderUri);
        trip.setParameter("relativeFilePath", TEST_FILE_NAME);

        trip.execute("save");

        UploadCSVActionBean actionBean = trip.getActionBean(UploadCSVActionBeanMock.class);
        MockHttpServletResponse response = (MockHttpServletResponse) actionBean.getContext().getResponse();

        // On successful saving, we expect to be redirected, hence expecting response code 302.
        assertEquals(302, response.getStatus());

        // Assert existence of various triples about the uploaded file.
        String[] statement7 = {testFileUri, Predicates.CR_SPARQL_QUERY, null};
        assertTrue("Expected statement: " + ArrayUtils.toString(7), hasLiteralStatement(statement7));

        // Assert that the SPARQL query generated for the uploaded file is correct.
        // Keeping in mind that above we requested the delete-column script on the "Thumbnail" column.
        SubjectDTO fileSubject = DAOFactory.get().getDao(HelperDAO.class).getSubject(testFileUri);
        assertTrue("Expected a non-null file subject with predicates", fileSubject != null && fileSubject.getPredicateCount() > 0);
        String actualSparql = fileSubject.getObjectValue(Predicates.CR_SPARQL_QUERY);
        String actualSparqlCompressed = actualSparql == null ? null : actualSparql.replaceAll("\\s+", "");
        String expectedSparqlCompressed = expectedSparql.replaceAll("\\s+", "");
        assertEquals("Actual SPARQL query is not what expected", expectedSparqlCompressed, actualSparqlCompressed);
    }
    /**
     * Helper method to check if there were any messages for the user.
     */
    private boolean anyMessages(ActionBean aBean) {
        String[] queues = { "systemMessages", "cautionMessages", "warningMessages" };

        boolean foundOne = false;
        for (String queue : queues) {
            List<Message> messages = aBean.getContext().getMessages(queue);
            for (Message message : messages) {
                foundOne = true;
                LOGGER.info(queue + ": " + message.getMessage(null));
            }
        }
        return foundOne;
    }

    /**
     * Helper method for getting abstract reference to the file under test.
     *
     * @param fileName The name of the resource file to locate.
     * @return The file reference.
     */
    private static File getTestFile(String fileName) {
        try {
            return new File(UploadCSVActionBeanIT.class.getClassLoader().getResource(fileName).toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not locate resource file by the name of: " + fileName);
        }
    }

    /**
     * Tests if a 404 error is returned if the folderuri is empty
     * @throws Exception Any sort of error that happens.
     */
    @Test
    public void testUploadWithEmptyFolderURI() throws Exception {

        // Prepare the servlet context mock + Stripes action bean roundtrip.
        MockRoundtrip trip = new MockRoundtrip(ctx, UploadCSVActionBeanMock.class);

        // Prepare rich-type (e.g. file bean) request parameters. These will be picked up by CRActionBeanPropertyBinder
        // that has already been injected into the servlet context mock obtained above.
        HashMap<String, Object> richTypeRequestParams = new HashMap<String, Object>();
        FileBean fileBean = new FileBean(TEST_FILE, "text/plain", TEST_FILE.getName());
        richTypeRequestParams.put("fileBean", fileBean);
        trip.getRequest().setAttribute(CRActionBeanPropertyBinder.RICH_TYPE_REQUEST_PARAMS_ATTR_NAME, richTypeRequestParams);

        // Execute the event.
        trip.execute();

        // Assert the returned HTTP code.
        UploadCSVActionBean actionBean = trip.getActionBean(UploadCSVActionBeanMock.class);
        MockHttpServletResponse response = (MockHttpServletResponse) actionBean.getContext().getResponse();
        assertEquals(404, response.getStatus());
    }
}
