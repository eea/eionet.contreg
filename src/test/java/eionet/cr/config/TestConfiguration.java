package eionet.cr.config;

import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.mock.MockServletContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
@Configuration
public class TestConfiguration {

    @Bean
    public MockServletContext getMock() {
        MockServletContext ctx = new MockServletContext("test");
        Map filterParams = new HashMap();
        // filterParams.put("ActionResolver.Packages", "postHarvest");
        filterParams.put("ActionResolver.Packages", "eionet.cr.web.action");
        filterParams.put("Interceptor.Classes", "eionet.cr.web.interceptor.ActionEventInterceptor");
        filterParams.put("ActionBeanContext.Class", "eionet.cr.web.action.CRTestActionBeanContext");
        // filterParams.put("LocalePicker.Locales", "en_US:UTF-8");
        ctx.addFilter(StripesFilter.class, "StripesFilter", filterParams);
        ctx.setServlet(DispatcherServlet.class, "StripesDispatcher", null);
        ctx.addInitParameter("contextConfigLocation", "classpath:spring-test-context.xml");

/*        ContextLoaderListener springContextLoader = new ContextLoaderListener();
        springContextLoader.contextInitialized(new ServletContextEvent(ctx));*/
        return ctx;
    }

}
