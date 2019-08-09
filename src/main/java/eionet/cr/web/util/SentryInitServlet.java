package eionet.cr.web.util;

import eionet.cr.spring.SpringApplicationContext;
import eionet.propertyplaceholderresolver.CircularReferenceException;
import eionet.propertyplaceholderresolver.ConfigurationPropertyResolver;
import eionet.propertyplaceholderresolver.UnresolvedPropertyException;
import io.sentry.Sentry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class SentryInitServlet extends HttpServlet {

    /**
     * Sentry initialization
     *
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ConfigurationPropertyResolver propertyResolver = SpringApplicationContext.getBean("configurationPropertyResolver");

        String dsn = null;

        try {
            dsn = propertyResolver.resolveValue("config.sentry.dsn");
        } catch (UnresolvedPropertyException e) {
            e.printStackTrace();
        } catch (CircularReferenceException e) {
            e.printStackTrace();
        }
        if (dsn != null && dsn.length() > 2) {
            Sentry.init(dsn);
        } else {
            Sentry.init();
        }

    }
}