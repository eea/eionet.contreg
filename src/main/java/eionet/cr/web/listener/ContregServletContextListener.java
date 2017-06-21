package eionet.cr.web.listener;

import eionet.cr.spring.SpringApplicationContext;
import eionet.propertyplaceholderresolver.CircularReferenceException;
import eionet.propertyplaceholderresolver.ConfigurationPropertyResolver;
import eionet.propertyplaceholderresolver.UnresolvedPropertyException;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author Thanos Tourikas
 */
public class ContregServletContextListener implements ApplicationListener {

//    @Override
//    public void contextInitialized(ServletContextEvent servletContextEvent) {
//
////        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
////        ResourceLoader resourceLoader = new DefaultResourceLoader();
////
////        Resource resource = resourceLoader.getResource("classpath:"+System.getenv("SERVER_TYPE")+"local.properties");
////        configurer.setLocation(resource);
//////        configurer.postProcessBeanFactory(beanFactory);
//
//        ConfigurationPropertyResolver configurationService = (ConfigurationPropertyResolver) SpringApplicationContext.getBean("configurationPropertyResolver");
//        try {
//            configurationService.resolveValue("virtuoso.db.url");
//        } catch (UnresolvedPropertyException e) {
//            e.printStackTrace();
//        } catch (CircularReferenceException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void contextDestroyed(ServletContextEvent servletContextEvent) {
//
//    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {

//        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
//        ResourceLoader resourceLoader = new DefaultResourceLoader();
//
//        Resource resource = resourceLoader.getResource("classpath:"+System.getenv("SERVER_TYPE")+"local.properties");
//        configurer.setLocation(resource);

        ConfigurationPropertyResolver configurationService = (ConfigurationPropertyResolver) SpringApplicationContext.getBean("configurationPropertyResolver");

        try {
            String url = configurationService.resolveValue("virtuoso.db.url");
            System.out.println(url);
        } catch (UnresolvedPropertyException e) {
            e.printStackTrace();
        } catch (CircularReferenceException e) {
            e.printStackTrace();
        }
    }
}
