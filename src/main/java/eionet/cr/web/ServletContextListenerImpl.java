package eionet.cr.web;

import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import eionet.cr.index.EncodingSchemes;
import eionet.cr.index.Searcher;

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
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent arg0) {
		
		EncodingSchemes.load();
	}
}
