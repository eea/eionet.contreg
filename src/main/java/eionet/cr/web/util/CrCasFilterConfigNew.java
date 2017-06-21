package eionet.cr.web.util;

import eionet.cr.spring.SpringApplicationContext;
import eionet.propertyplaceholderresolver.CircularReferenceException;
import eionet.propertyplaceholderresolver.ConfigurationPropertyResolver;
import eionet.propertyplaceholderresolver.UnresolvedPropertyException;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

/**
 *
 * @author Thanos Tourikas
 *
 */
public class CrCasFilterConfigNew implements FilterConfig {

    @Override
    public String getInitParameter(String s) {
        ConfigurationPropertyResolver resolver = (ConfigurationPropertyResolver) SpringApplicationContext.getBean("configurationPropertyResolver");
        String value = null;
        try {
            value = resolver.resolveValue(s);
        } catch (UnresolvedPropertyException e) {
//            e.printStackTrace();
        } catch (CircularReferenceException e) {
//            e.printStackTrace();
        }

        return value;
    }

    @Override
    public String getFilterName() {
        return null;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public Enumeration getInitParameterNames() {
        return null;
    }
}
