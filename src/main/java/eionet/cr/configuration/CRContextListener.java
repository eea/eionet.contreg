package eionet.cr.configuration;

import eionet.cr.config.GeneralConfig;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class CRContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        servletContextEvent.getServletContext().setInitParameter("appDispName", GeneralConfig.getProperty("config.application.displayName"));
        servletContextEvent.getServletContext().setInitParameter("templateCacheFolder", GeneralConfig.getProperty("application.eea.template.folder"));
        servletContextEvent.getServletContext().setInitParameter("useCentralAuthenticationService", GeneralConfig.getProperty("useCentralAuthenticationService"));
        servletContextEvent.getServletContext().setInitParameter("enableEEAFunctionality", GeneralConfig.getProperty("enableEEAFunctionality"));
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
