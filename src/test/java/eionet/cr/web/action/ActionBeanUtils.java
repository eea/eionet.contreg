package eionet.cr.web.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.mock.MockServletContext;
import org.apache.commons.lang.StringUtils;

import javax.servlet.Filter;

/**
 * Utils for testing Action Beans.
 *
 * @author kaido
 */
public final class ActionBeanUtils {

    /** prevent initialization. */
    private ActionBeanUtils() {
        throw new AssertionError("");
    }

    // XXX: this might be useful when upgrading - https://stackoverflow.com/questions/32223323/stripes-1-6-missing-springinterceptor
    public static void addFilter(MockServletContext context) {
        if (context != null) {
            Map filterParams = new HashMap();
            filterParams.put("ActionResolver.Packages", "eionet.cr.web.action");
            filterParams.put("Interceptor.Classes", getInterceptorClassesParameter());
            filterParams.put("ActionBeanContext.Class", "eionet.cr.web.action.CRTestActionBeanContext");
            filterParams.put("LocalePicker.Class", "eionet.cr.web.util.LocalePicker");
            filterParams.put("trimSpaces", "true");
            filterParams.put("FileUpload.MaximumPostSize", "50000000");
            filterParams.put("ExceptionHandler.Class", "eionet.cr.web.util.StripesExceptionHandler");
            //filterParams.put("ActionResolver.Packages", "postHarvest");
            //filterParams.put("Interceptor.Classes", "net.sourceforge.stripes.integration.spring.SpringInterceptor");
            // filterParams.put("LocalePicker.Locales", "en_US:UTF-8");
            context.addFilter(StripesFilter.class, "StripesFilter", filterParams);
        }
    }

    public static void clearFilters(MockServletContext context) {
        List<Filter> filters = context.getFilters();
        for (Filter filter : filters) {
            filter.destroy();
        }
    }

    //"eionet.web.action.di.ActionBeanDependencyInjectionInterceptor",
    //                "eionet.web.action.di.SpyActionBeanInterceptor"

    private static String getInterceptorClassesParameter() {
        String[] interceptors = new String[] {
                "net.sourceforge.stripes.integration.spring.SpringInterceptor",
                "eionet.cr.web.interceptor.ActionEventInterceptor"
        };

        return StringUtils.join(interceptors, ", ");
    }
}
