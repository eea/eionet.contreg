package eionet.cr.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import eionet.cr.index.walk.AllDocsWalker;

/**
 * 
 * @author heinljab
 *
 */
public class ServletContextListenerImpl implements ServletContextListener{

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent event) {
		
		AllDocsWalker.startupWalk();
	}
}
