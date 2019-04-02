package eionet.cr.config;

import net.sourceforge.stripes.controller.DispatcherServlet;
import net.sourceforge.stripes.mock.MockServletContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 *
 */
@Configuration
public class TestConfiguration {

    @Bean
    public MockServletContext getMock() {
        MockServletContext ctx = new MockServletContext("test");
        ctx.addInitParameter("contextConfigLocation", "classpath:spring-test-context.xml");
        ctx.setServlet(DispatcherServlet.class, "StripesDispatcher", null);
        return ctx;
    }

}
