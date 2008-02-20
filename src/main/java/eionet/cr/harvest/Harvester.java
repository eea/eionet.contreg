package eionet.cr.harvest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.LockObtainFailedException;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.arp.ARP;

import eionet.cr.config.GeneralConfig;
import eionet.cr.util.Messages;
import eionet.cr.util.Util;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class Harvester {

	/** */
	private static Logger logger = Logger.getLogger(Harvester.class);
	
	/** */
	private IndexWriter allIndexWriter = null;
	private RDFSourceHandler handler = null;
	private HttpServletRequest servletRequest = null;
	private ServletContext servletContext = null; 
	
	/**
	 *
	 */
	public Harvester(){
	}

	/**
	 * 
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 */
	private void initIndexing() throws CorruptIndexException, LockObtainFailedException, IOException{
		
		if (allIndexWriter==null){
			Analyzer analyzer = new StandardAnalyzer();
			String indexLocation = GeneralConfig.getProperty(GeneralConfig.LUCENE_INDEX_LOCATION);
			allIndexWriter = new IndexWriter(indexLocation, analyzer);
		}
	}
	
	/**
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 * 
	 *
	 */
	private void closeIndexing() throws CorruptIndexException, IOException{
		
		if (allIndexWriter!=null){
			allIndexWriter.optimize();
			allIndexWriter.close();
		}
	}

	/**
	 * 
	 * @param url
	 * @param useCache
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SAXException
	 */
	public void harvest(String url, boolean useCache) throws CorruptIndexException, LockObtainFailedException, MalformedURLException, IOException, SAXException{
		harvest(new URL(url), useCache);
	}

	/**
	 * 
	 * @param url
	 * @param useCache
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 * @throws SAXException
	 */
	public void harvest(URL url, boolean useCache) throws CorruptIndexException, LockObtainFailedException, IOException, SAXException{
		
		File file = new File(GeneralConfig.getProperty(GeneralConfig.HARVESTER_FILES_LOCATION), toFileName(url) + ".xml");
		if (!useCache){
			if (file.exists())
				file.delete();
			download(url, file);
		}
		else if (!file.exists()){
			if (servletRequest!=null){
				Messages.addMessage(servletRequest, "messages", "This URL does not seem to be cached.");
				return;
			}
			else
				throw new IOException("No cached file found for " + url.toString());
		}
		
		harvest(file, url);
	}
	
	/**
	 * 
	 * @param url
	 * @param file
	 * @throws IOException 
	 */
	private void download(URL url, File file) throws IOException{

		InputStream istream = null;
		FileOutputStream fos = null;
		try{
			HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
			httpConn.setRequestProperty("Accept", "application/rdf+xml");
			
			istream = httpConn.getInputStream();
			fos = new FileOutputStream(file);
	        
	        int i = -1;
	        byte[] bytes = new byte[1024];
	
	        while ((i = istream.read(bytes, 0, bytes.length)) != -1)
	        	fos.write(bytes, 0, i);
		}
		finally{
			try{
		        if (fos!=null) fos.close();
		        if (istream!=null)istream.close();
			}
			catch (Exception e){}
		}
	}

	/**
	 * 
	 * @param file
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 * @throws SAXException 
	 */
	private void harvest(File file, URL sourceURL) throws CorruptIndexException, LockObtainFailedException, IOException, SAXException{
		
		InputStreamReader reader = null;
		
		try{
			initIndexing();
	        reader = new InputStreamReader(new FileInputStream(file));

			// set up the RDFSourceHandler
			handler = new RDFSourceHandler(allIndexWriter, sourceURL);
			handler.setServletContext(servletContext);
			
			// set up ARP and let it run
			ARP arp = new ARP();
	        arp.setStatementHandler(handler);
	        arp.setErrorHandler(new SAXErrorHandler());
	        arp.load(reader);
		}
		finally{
			// try to close index
			try{
				closeIndexing();
			}
			catch (Throwable t){}
			
			// try to close file input stream
			try{
				reader.close();
			}
			catch (Throwable t){}
		}
	}
	
	/**
	 * 
	 * @param req
	 * @throws Exception 
	 */
	public static void harvest(HttpServletRequest req, ServletContext servletContext) throws Exception{
		
		String url = req.getParameter("url");
		if (url==null || url.length()==0)
			throw new Exception("Missing request parameter: url");
		
		boolean useCache = req.getParameter("action").toLowerCase().endsWith("from cache");
		
		// set up the harvester
		Harvester harvester = new Harvester();
		harvester.setServletRequest(req);
		harvester.setServletContext(servletContext);
		
		// do the harvest
		harvester.harvest(url, useCache);
		
		RDFSourceHandler handler = harvester.getHandler();
		if (handler!=null)
			Messages.addMessage(req, "messages", handler.getCountDocumentsIndexed() + " documents harvested.");
	}

	/**
	 * 
	 * @return
	 */
	public RDFSourceHandler getHandler() {
		return handler;
	}

	/**
	 * 
	 * @return
	 */
	public HttpServletRequest getServletRequest() {
		return servletRequest;
	}

	/**
	 * 
	 * @param servletRequest
	 */
	public void setServletRequest(HttpServletRequest servletRequest) {
		this.servletRequest = servletRequest;
	}

	/**
	 * @return Returns the servletContext.
	 */
	public ServletContext getServletContext() {
		return servletContext;
	}

	/**
	 * @param servletContext The servletContext to set.
	 */
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	private static String toFileName(URL url){
		
		StringBuffer buf = new StringBuffer(url.getHost());
		int port = url.getPort();
		if (port>0)
			buf.append(".").append(port);
		String path = url.getPath();
		if (path!=null)
			buf.append("_").append(path);
		String qryStr = url.getQuery();
		if (qryStr!=null)
			buf.append("_").append(qryStr);

		String s = buf.toString();
		buf = new StringBuffer();
		for (int i=0; i<s.length(); i++){
			char c = s.charAt(i);
			if (!Character.isLetterOrDigit(c) && c!='-' && c!='_' && c!='.')
				c = '_';
			buf.append(c);
		}
		
		return buf.toString();
	}
}
