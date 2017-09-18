package eionet.cr.web.action;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.mock.MockServletContext;

/**
 * Utils for testing Action Beans.
 *
 * @author kaido
 */
public final class ActionBeanUtils {
    /** test context. */
    private static MockServletContext context;

    /** prevent initialization. */
    private ActionBeanUtils() {
        throw new AssertionError("");
    }

    /**
     * Initializes Mock servlet context.
     *
     * @return test context
     */
    public static MockServletContext getServletContext() {
        if (context == null) {
            MockServletContext ctx = new MockServletContext("test");
            ctx.setServlet(DispatcherServlet.class, "StripesDispatcher", null);
            context = ctx;
        }

        return ActionBeanUtils.context;
    }

    public static void addFilter(MockServletContext context) {
        if (context != null) {
            Map filterParams = new HashMap();
            filterParams.put("ActionResolver.Packages", "postHarvest");
            filterParams.put("ActionResolver.Packages", "eionet.cr.web.action");
            filterParams.put("Interceptor.Classes", "eionet.cr.web.interceptor.ActionEventInterceptor");
            filterParams.put("Interceptor.Classes", "net.sourceforge.stripes.integration.spring.SpringInterceptor");
            filterParams.put("ActionBeanContext.Class", "eionet.cr.web.action.CRTestActionBeanContext");
            // filterParams.put("LocalePicker.Locales", "en_US:UTF-8");
            context.addFilter(StripesFilter.class, "StripesFilter", filterParams);
        }
    }
}
