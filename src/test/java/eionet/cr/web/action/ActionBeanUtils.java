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
    }

    /**
     * Initializes Mock servlet context.
     *
     * @return test context
     */
    public static MockServletContext getServletContext() {
        if (context == null) {
            MockServletContext ctx = new MockServletContext("test");
            Map filterParams = new HashMap();
            // filterParams.put("ActionResolver.Packages", "postHarvest");
            filterParams.put("ActionResolver.Packages", "eionet.cr.web.action");
            filterParams.put("Interceptor.Classes", "eionet.cr.web.interceptor.ActionEventInterceptor");
            filterParams.put("ActionBeanContext.Class", "eionet.cr.web.action.CRTestActionBeanContext");
            // filterParams.put("LocalePicker.Locales", "en_US:UTF-8");
            ctx.addFilter(StripesFilter.class, "StripesFilter", filterParams);
            ctx.setServlet(DispatcherServlet.class, "StripesDispatcher", null);

            context = ctx;
        }

        return ActionBeanUtils.context;
    }
}
