package eionet.cr.web.action;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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

import org.apache.commons.lang.ArrayUtils;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.filestore.FileStore;
import eionet.cr.test.helpers.CRDatabaseTestCase;

/**
 * A class for testing the behavior of {@link UploadCSVActionBean}.
 *
 * @author Jaanus
 */
public class UploadCSVActionBeanTest extends CRDatabaseTestCase {

    /** The name of user whose folder we're testing in. */
    public static final String TEST_USER_NAME = "somebody";

    /** The name of seeded CSV file we're testing. */
    public static final String TEST_FILE_NAME = "USPresidents.csv";

    /** The URI of the virtual CR folder we're testing in. */
    public static final String TEST_FOLDER_URI = "http://127.0.0.1:8080/cr/home/" + TEST_USER_NAME;

    /** The URI of the uploaded file we're testing. */
    public static final String TEST_FILE_URI = TEST_FOLDER_URI + "/" + TEST_FILE_NAME;

    /** Abstract reference to the file under test. */
    public static final File TEST_FILE = getTestFile(TEST_FILE_NAME);

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

    /**
     * A test that tests the whole sequence of uploading and saving a CSV file.
     *
     * @throws Exception
     */
    public void testWholeSequence() throws Exception {

        MockServletContext ctx = createContextMock();
        MockRoundtrip trip = new MockRoundtrip(ctx, UploadCSVActionBeanMock.class);

        trip.setParameter("folderUri", TEST_FOLDER_URI);
        trip.setParameter("overwrite", "true");
        trip.setParameter("fileType", "CSV");

        trip.execute("upload");

        UploadCSVActionBean actionBean = trip.getActionBean(UploadCSVActionBeanMock.class);
        MockHttpServletResponse response = (MockHttpServletResponse) actionBean.getContext().getResponse();
        assertEquals(200, response.getStatus());

        File storedFile = FileStore.getByUri(TEST_FILE_URI);
        long storedFileSize = storedFile.length();

        boolean flag =
                storedFile != null && storedFile.exists() && storedFile.isFile() && TEST_FILE_NAME.equals(storedFile.getName());
        assertTrue("Expected an existing stored file with name: " + TEST_FILE_NAME, flag);
        assertEquals("Expected stored file size to be equal to the uploaded file size", testFileSize, storedFileSize);

        String[] statement1 = {TEST_FOLDER_URI, Predicates.CR_HAS_FILE, TEST_FILE_URI};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement1), hasResourceStatement(statement1));

        String[] statement2 = {TEST_FILE_URI, Predicates.CR_BYTE_SIZE, String.valueOf(storedFileSize)};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement2), hasLiteralStatement(statement2));

        String[] statement3 = {TEST_FILE_URI, Predicates.RDF_TYPE, Subjects.CR_TABLE_FILE};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement3), hasResourceStatement(statement3));

        String[] statement4 = {TEST_FILE_URI, Predicates.CR_MEDIA_TYPE, "CSV"};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement4), hasLiteralStatement(statement4));

        String[] statement5 = {TEST_FILE_URI, Predicates.CR_LAST_MODIFIED, null};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement5), hasLiteralStatement(statement5));
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
    public static File getTestFile(String fileName) {
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

            URL resourceURL = getClass().getClassLoader().getResource(TEST_FILE_NAME);
            try {
                URI resourceURI = resourceURL.toURI();
                File file = new File(resourceURI);
                FileBean fileBean = new FileBean(file, "text/plain", file.getName());
                BeanUtil.setPropertyValue("fileBean", bean, fileBean);

            } catch (URISyntaxException e) {
                throw new RuntimeException("Wasn't expecting a URI syntax exception: " + e);
            }

            return validationErrors;
        }
    }
}
