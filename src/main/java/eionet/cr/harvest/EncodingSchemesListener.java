package eionet.cr.harvest;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import eionet.cr.index.Searcher;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class EncodingSchemesListener implements ServletContextListener{

	/** */
	private static Logger logger = Logger.getLogger(EncodingSchemesListener.class);

	/** */
	private ServletContext servletContext;

	/*
	 *  (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent contextEvent) {

		try{
			servletContext = contextEvent.getServletContext();
			Searcher searcher = new Searcher();
			List hits = searcher.search("IS_ENCODING_SCHEME:true");
			if (hits!=null && hits.size()>0){
				EncodingSchemes encSchemes = new EncodingSchemes();
				for (int i=0; i<hits.size(); i++){
					Hashtable hash = (Hashtable)hits.get(i);
					String[] ids = (String[])hash.get("ID");
					if (ids!=null && ids.length>0)
						encSchemes.update(ids[0], (String[])hash.get("http://www.w3.org/2000/01/rdf-schema#label"));
				}
				
				contextEvent.getServletContext().setAttribute(encSchemes.getClass().getName(), encSchemes);
			}
		}
		catch (Exception e){
			logger.error("Failed to initialize encoding schemes in servlet context", e);
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent contextEvent) {
	}

}
