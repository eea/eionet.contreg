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
import eionet.cr.test.helpers.CRDatabaseTestCase;

/**
 * A class for testing the behavior of {@link UploadCSVActionBean}.
 *
 * @author Jaanus
 */
public class UploadCSVActionBeanTest extends CRDatabaseTestCase {

    /**
     * A test that tests the whole sequence of uploading and saving a CSV file.
     *
     * @throws Exception
     */
    public void testWholeSequence() throws Exception {

        MockServletContext ctx = createContextMock();
        MockRoundtrip trip = new MockRoundtrip(ctx, UploadCSVActionBeanMock.class);

        trip.setParameter("folderUri", "http://127.0.0.1:8080/cr/home/somebody");
        trip.setParameter("overwrite", "true");
        trip.setParameter("fileType", "CSV");

        trip.execute("upload");

        UploadCSVActionBean actionBean = trip.getActionBean(UploadCSVActionBeanMock.class);
        MockHttpServletResponse response = (MockHttpServletResponse) actionBean.getContext().getResponse();
        assertEquals(200, response.getStatus());

        String[] statement =
                {"http://127.0.0.1:8080/cr/home/somebody", Predicates.CR_HAS_FILE,
                        "http://127.0.0.1:8080/cr/home/somebody/USPresidents.csv"};
        assertTrue("Expected statement: " + ArrayUtils.toString(statement), hasResourceStatement(statement));
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

            URL resourceURL = getClass().getClassLoader().getResource("USPresidents.csv");
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
