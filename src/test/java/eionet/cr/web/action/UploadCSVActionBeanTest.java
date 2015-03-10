package eionet.cr.web.action;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.controller.DefaultActionBeanPropertyBinder;
import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.mock.MockHttpServletResponse;
import net.sourceforge.stripes.mock.MockRoundtrip;
import net.sourceforge.stripes.mock.MockServletContext;
import net.sourceforge.stripes.util.bean.BeanUtil;
import net.sourceforge.stripes.validation.ValidationErrors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.FolderDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.filestore.FileStore;
import eionet.cr.harvest.util.CsvImportUtil;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.FolderUtil;
import eionet.cr.web.security.CRUser;

/**
 * A class for testing the behavior of {@link UploadCSVActionBean}.
 *
 * @author Jaanus
 */
public class UploadCSVActionBeanTest extends CRDatabaseTestCase {

    /** Name for the request attribute via which we inject rich-type (e.g. file bean) request parameters for the action bean. */
    public static final String RICH_TYPE_REQUEST_PARAMS_ATTR_NAME = "RICH_TYPE_REQUEST_PARAMS";

    /** The name of user whose folder we're testing in. */
    public static final String TEST_USER_NAME = "somebody";

    /** The name of seeded CSV file we're testing. */
    public static final String TEST_FILE_NAME = "Presidents of USA.csv";

    /** The name of seeded CSV file we're testing, spaces escaped */
    public static final String TEST_FILE_NAME_ESCAPED = StringUtils.replace(TEST_FILE_NAME, " ", "%20");

    /** The URI of the virtual CR folder we're testing in. */
    public static final String TEST_FOLDER_URI = CRUser.rootHomeUri() + "/" + TEST_USER_NAME;

    /** The URI of the uploaded file we're testing. */
    public static final String TEST_FILE_URI = TEST_FOLDER_URI + "/" + TEST_FILE_NAME_ESCAPED;

    /** Abstract reference to the file under test. */
    public static final File TEST_FILE = getTestFile(TEST_FILE_NAME);

    /** Expected SPARQL query to be generated for the file when data-linking scripts used. Ignore whitespace when comparing! */
    public static final String EXPECTED_SPARQL_WITH_LINKING_SCRIPTS = "PREFIX tableFile: <" + TEST_FOLDER_URI + "/"
            + TEST_FILE_NAME_ESCAPED + "#> SELECT * FROM <" + TEST_FOLDER_URI + "/" + TEST_FILE_NAME_ESCAPED + "> WHERE {"
            + "    OPTIONAL { _:rec tableFile:Presidency ?Presidency } ."
            + "    OPTIONAL { _:rec tableFile:President ?President } ."
            + "    OPTIONAL { _:rec tableFile:Wikipedia_Entry ?Wikipedia_Entry } ."
            + "    OPTIONAL { _:rec tableFile:Took_office ?Took_office } ."
            + "    OPTIONAL { _:rec tableFile:Left_office ?Left_office } ." + "    OPTIONAL { _:rec tableFile:Party ?Party } ."
            + "    OPTIONAL { _:rec tableFile:Portrait ?Portrait } ."
            + "    OPTIONAL { _:rec tableFile:Home_State ?Home_State } . }";

    /** Expected SPARQL query to be generated for the file when data-linking scripts NOT used. Ignore whitespace when comparing! */
    public static final String EXPECTED_SPARQL_WITHOUT_LINKING_SCRIPTS = "PREFIX tableFile: <" + TEST_FOLDER_URI + "/"
            + TEST_FILE_NAME_ESCAPED + "#> SELECT * FROM <" + TEST_FOLDER_URI + "/" + TEST_FILE_NAME_ESCAPED + "> WHERE {"
            + "    OPTIONAL { _:rec tableFile:Presidency ?Presidency } ."
            + "    OPTIONAL { _:rec tableFile:President ?President } ."
            + "    OPTIONAL { _:rec tableFile:Wikipedia_Entry ?Wikipedia_Entry } ."
            + "    OPTIONAL { _:rec tableFile:Took_office ?Took_office } ."
            + "    OPTIONAL { _:rec tableFile:Left_office ?Left_office } ." + "    OPTIONAL { _:rec tableFile:Party ?Party } ."
            + "    OPTIONAL { _:rec tableFile:Portrait ?Portrait } ." + "    OPTIONAL { _:rec tableFile:Thumbnail ?Thumbnail } . "
            + "    OPTIONAL { _:rec tableFile:Home_State ?Home_State } . }";

    /** Size of the file under test before test. */
    private long testFileSize;

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {

        super.setUp();
        testFileSize = TEST_FILE.length();
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
        doUploadSave(dataLinkingScripts, EXPECTED_SPARQL_WITH_LINKING_SCRIPTS, true);

        // Now restore the file-under-test from the above-made backup if indeed it has been deleted as noted above.
        if (!TEST_FILE.exists()) {
            if (backupFile.exists()) {
                FileUtils.copyFile(backupFile, TEST_FILE);
            } else {
                throw new IllegalStateException("Test seed file has gone and no backup file is present either!");
            }
        }

        // Now do a run WITHOUT data linking script(s).
        doUploadSave(null, EXPECTED_SPARQL_WITHOUT_LINKING_SCRIPTS, true);
    }

    /**
     *
     * @throws Exception
     */
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
        doUploadSave(dataLinkingScripts, EXPECTED_SPARQL_WITH_LINKING_SCRIPTS, true);

        // Try harvesting the freshly uploaded file.
        SubjectDTO subject = DAOFactory.get().getDao(HelperDAO.class).getSubject(TEST_FILE_URI);
        assertNotNull("Expected existing subject for " + TEST_FILE_URI, subject);
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

        folderDAO.deleteFileOrFolderUris(TEST_FOLDER_URI, Collections.singletonList(TEST_FILE_URI));
        harvestSourceDAO.removeHarvestSources(Collections.singletonList(TEST_FILE_URI));

        FileStore fileStore = FileStore.getInstance(TEST_USER_NAME);
        fileStore.delete(FolderUtil.extractPathInUserHome(TEST_FOLDER_URI + "/" + TEST_FILE_NAME));
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
     * Does and tests the file's "upload" step, doing it with or without overwrite, depending on the given boolean.
     *
     * @param isOverwrite Overwrite or not.
     * @throws Exception Any sort of error that happens.
     */
    private void doUpload(boolean isOverwrite) throws Exception {

        // Prepare the servlet context mock + Stripes action bean roundtrip.
        MockServletContext ctx = createContextMock();
        MockRoundtrip trip = new MockRoundtrip(ctx, UploadCSVActionBeanMock.class);

        // Prepare rich-type (e.g. file bean) request parameters. These will be picked up by MyActionBeanPropertyBinder
        // that has already been injected into the servlet context mock obtained above.
        HashMap<String, Object> richTypeRequestParams = new HashMap<String, Object>();
        FileBean fileBean = new FileBean(TEST_FILE, "text/plain", TEST_FILE.getName());
        richTypeRequestParams.put("fileBean", fileBean);
        trip.getRequest().setAttribute(RICH_TYPE_REQUEST_PARAMS_ATTR_NAME, richTypeRequestParams);

        // Prepare simple string-based request parameters.
        trip.setParameter("folderUri", TEST_FOLDER_URI);
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
        File storedFile = FileStore.getByUri(TEST_FILE_URI);
        assertNotNull("Didn't expect the stored file to be null!", storedFile);
        long storedFileSize = storedFile.length();
        boolean flag =
                storedFile != null && storedFile.exists() && storedFile.isFile() && TEST_FILE_NAME.equals(storedFile.getName());
        assertTrue("Expected an existing stored file with name: " + TEST_FILE_NAME, flag);
        assertEquals("Expected stored file size to be equal to the uploaded file size", testFileSize, storedFileSize);

        // Assert existence of various triples about the uploaded file and its parent folder.
        String[] statement1 = {TEST_FOLDER_URI, Predicates.CR_HAS_FILE, TEST_FILE_URI};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement1), hasResourceStatement(statement1));

        boolean b = hasLiteralStatement(TEST_FILE_URI, Predicates.CR_BYTE_SIZE, String.valueOf(storedFileSize), XMLSchema.INT);
        assertTrue("Missing or unexpected value for " + Predicates.CR_BYTE_SIZE, b);

        String[] statement3 = {TEST_FILE_URI, Predicates.RDF_TYPE, Subjects.CR_TABLE_FILE};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement3), hasResourceStatement(statement3));

        String[] statement4 = {TEST_FILE_URI, Predicates.CR_MEDIA_TYPE, "CSV"};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement4), hasLiteralStatement(statement4));

        String[] statement5 = {TEST_FILE_URI, Predicates.CR_LAST_MODIFIED, null};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement5), hasLiteralStatement(statement5));

        SubjectDTO testFileSubject = DAOFactory.get().getDao(HelperDAO.class).getSubject(TEST_FILE_URI);
        assertNotNull("Expected existing subject for " + TEST_FILE_URI, testFileSubject);
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

        MockServletContext ctx = createContextMock();
        MockRoundtrip trip = new MockRoundtrip(ctx, UploadCSVActionBeanMock.class);

        // Prepare the rich-type request parameters: the given data-linking scripts (if any)
        // and specifying the list of columns that will constitute unique key.
        HashMap<String, Object> richTypeRequestParams = new HashMap<String, Object>();
        if (CollectionUtils.isNotEmpty(dataLinkingScripts)) {
            richTypeRequestParams.put("dataLinkingScripts", dataLinkingScripts);
        }
        richTypeRequestParams.put("uniqueColumns", Collections.singletonList("Presidency"));
        trip.getRequest().setAttribute(RICH_TYPE_REQUEST_PARAMS_ATTR_NAME, richTypeRequestParams);

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
        trip.setParameter("folderUri", TEST_FOLDER_URI);
        trip.setParameter("relativeFilePath", TEST_FILE_NAME);

        trip.execute("save");

        UploadCSVActionBean actionBean = trip.getActionBean(UploadCSVActionBeanMock.class);
        MockHttpServletResponse response = (MockHttpServletResponse) actionBean.getContext().getResponse();

        // On successful saving, we expect to be redirected, hence expecting response code 302.
        assertEquals(302, response.getStatus());

        // Assert existence of various triples about the uploaded file.
        String[] statement6 = {TEST_FILE_URI, Predicates.CR_OBJECTS_UNIQUE_COLUMN, "Presidency"};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement6), hasLiteralStatement(statement6));

        String[] statement7 = {TEST_FILE_URI, Predicates.CR_SPARQL_QUERY, null};
        assertTrue("Expected statement: " + ArrayUtils.toString(7), hasLiteralStatement(statement7));

        // Assert that the SPARQL query generated for the uploaded file is correct.
        // Keeping in mind that above we requested the delete-column script on the "Thumbnail" column.
        SubjectDTO fileSubject = DAOFactory.get().getDao(HelperDAO.class).getSubject(TEST_FILE_URI);
        assertTrue("Expected a non-null file subject with predicates", fileSubject != null && fileSubject.getPredicateCount() > 0);
        String actualSparql = fileSubject.getObjectValue(Predicates.CR_SPARQL_QUERY);
        String actualSparqlCompressed = actualSparql == null ? null : actualSparql.replaceAll("\\s+", "");
        String expectedSparqlCompressed = expectedSparql.replaceAll("\\s+", "");
        assertEquals("Actual SPARQL query is not what expected", expectedSparqlCompressed, actualSparqlCompressed);
    }

    /**
     * Creates and returns a mock of servlet context for Stripes.
     *
     * @return The mock.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private MockServletContext createContextMock() {

        MockServletContext ctx = new MockServletContext("test");
        Map filterParams = new HashMap();
        filterParams.put("ActionResolver.Packages", "eionet.cr.web.action");
        filterParams.put("Interceptor.Classes", "eionet.cr.web.interceptor.ActionEventInterceptor");
        filterParams.put("ActionBeanContext.Class", "eionet.cr.web.action.CRTestActionBeanContext");
        filterParams.put("ActionBeanPropertyBinder.Class",
                "eionet.cr.web.action.UploadCSVActionBeanTest$MyActionBeanPropertyBinder");
        ctx.addFilter(StripesFilter.class, "StripesFilter", filterParams);
        ctx.setServlet(DispatcherServlet.class, "StripesDispatcher", null);

        return ctx;
    }

    /**
     * Helper method for getting abstract reference to the file under test.
     *
     * @param fileName The name of the resource file to locate.
     * @return The file reference.
     */
    private static File getTestFile(String fileName) {
        try {
            return new File(UploadCSVActionBeanTest.class.getClassLoader().getResource(fileName).toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not locate resource file by the name of: " + fileName);
        }
    }

    /**
     * Extension of {@link DefaultActionBeanPropertyBinder} in order to directly inject the proper file bean.
     *
     * @author Jaanus
     */
    public static class MyActionBeanPropertyBinder extends DefaultActionBeanPropertyBinder {

        /**
         * Default constructor.
         */
        public MyActionBeanPropertyBinder() {
            super();
        }

        /*
         * (non-Javadoc)
         *
         * @see net.sourceforge.stripes.controller.DefaultActionBeanPropertyBinder#bind(net.sourceforge.stripes.action.ActionBean,
         * net.sourceforge.stripes.action.ActionBeanContext, boolean)
         */
        @Override
        public ValidationErrors bind(ActionBean bean, ActionBeanContext context, boolean validate) {

            ValidationErrors validationErrors = super.bind(bean, context, validate);

            if (bean != null && context != null) {
                HttpServletRequest request = context.getRequest();
                if (request != null) {
                    Object o = request.getAttribute(RICH_TYPE_REQUEST_PARAMS_ATTR_NAME);
                    if (o instanceof HashMap<?, ?>) {
                        @SuppressWarnings("unchecked")
                        HashMap<String, Object> richTypeRequestParams = (HashMap<String, Object>) o;
                        for (Entry<String, Object> entry : richTypeRequestParams.entrySet()) {

                            String paramName = entry.getKey();
                            Object paramValue = entry.getValue();
                            BeanUtil.setPropertyValue(paramName, bean, paramValue);
                        }
                    }
                }
            }

            return validationErrors;
        }
    }
}
