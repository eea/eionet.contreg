package eionet.cr.harvest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.arp.ARP;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.harvest.util.DedicatedHarvestSourceTypes;
import eionet.cr.harvest.util.HarvestLog;
import eionet.cr.search.Searcher;
import eionet.cr.util.FileUtil;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;
import eionet.cr.util.xml.ConversionsParser;
import eionet.cr.util.xml.XmlAnalysis;

/**
 * 
 * @author heinljab
 *
 */
public abstract class Harvest {
	
	/** */
	private static final String HARVEST_FILE_NAME_EXTENSION = ".xml";
	public static final String STATUS_STARTED = "started";
	public static final String STATUS_FINISHED = "finished";
	public static final String TYPE_PULL = "pull";
	public static final String TYPE_PUSH = "push";
	
	/** */
	public static final String FATAL = "ftl";
	public static final String ERROR = "err";
	public static final String WARNING = "wrn";
	
	/** */
	protected String sourceUrlString = null;
	protected Log logger = null;
	
	/** */
	private int distinctSubjectsCount = 0;
	private int storedTriplesCount = 0;
	
	/** */
	private HarvestDAOWriter daoWriter = null;
	private HarvestNotificationSender notificationSender = null;
	
	/** */
	protected Throwable fatalError = null;
	protected List<Throwable> errors = new ArrayList<Throwable>();
	protected List<Throwable> warnings = new ArrayList<Throwable>();
	
	/**
	 * 
	 * @param sourceUrlString
	 */
	protected Harvest(String sourceUrlString){
		
		if (sourceUrlString==null)
			throw new NullPointerException();
		
		this.sourceUrlString = sourceUrlString;
		this.logger = new HarvestLog(sourceUrlString, LogFactory.getLog(this.getClass()));
	}

	/**
	 * 
	 * @throws HarvestException
	 */
	public void execute() throws HarvestException{

		try{
			doHarvestStartedActions();
			doExecute();
		}
		catch (Throwable t){
			fatalError = t;
			if (t instanceof HarvestException)
				throw (HarvestException)t;
			else
				throw new HarvestException("Exception when harvesting [" + sourceUrlString + "]: " + t.toString(), t);
		}
		finally{
			doHarvestFinishedActions();
		}
	}
	
	/**
	 * 
	 * @throws HarvestException
	 */
	protected abstract void doExecute() throws HarvestException;

	/**
	 * Harvest the given file.
	 * 
	 * @param file
	 * @throws HarvestException
	 */
	protected void harvest(File file) throws HarvestException{
		
		InputStreamReader reader = null;
		try{
			file = preProcess(file, sourceUrlString);
			if (file==null)
				return;
			
	        reader = new InputStreamReader(new FileInputStream(file));
	        harvest(reader);
		}
		catch (Exception e){
			throw new HarvestException("Exception when harvesting [" + sourceUrlString + "]: " + e.toString(), e);
		}
		finally{
			try{
				if (reader!=null) reader.close();
			}
			catch (IOException e){
				errors.add(e);
				logger.error("Failed to close file input stream reader: " + e.toString(), e);
			}
		}
	}

	/**
	 * Harvest the given reader.
	 * The caller is responsible for closing the reader.
	 * 
	 * @param reader
	 * @throws HarvestException
	 */
	protected void harvest(Reader reader) throws HarvestException{
		
		long genTime = System.currentTimeMillis();
		
		RDFHandler rdfHandler = new RDFHandler(sourceUrlString, genTime);
		if (this instanceof PushHarvest)
			rdfHandler.setClearPreviousContent(false);
		
		try{
			ARP arp = new ARP();
	        arp.setStatementHandler(rdfHandler);
	        arp.setErrorHandler(rdfHandler);
	        arp.load(reader, sourceUrlString);
	        
	        errors.addAll(rdfHandler.getSaxErrors());
	        warnings.addAll(rdfHandler.getSaxWarnings());

	        rdfHandler.commit();
	        
	        storedTriplesCount = rdfHandler.getStoredTriplesCount();
	        distinctSubjectsCount = rdfHandler.getDistinctSubjectsCount();
	        
	        logger.debug(rdfHandler.getStoredTriplesCount() + " triples stored");
		}
		catch (Exception e){
			try{
				rdfHandler.rollback();
			}
			catch (Exception ee){
				logger.fatal("Harvest rollback failed", ee);
				// TODO - handle rollback failure somehow
				// (e.g. send e-mail notification, store failure into database and retry rollback at later harvests)
			}
			
			Throwable t = (e instanceof LoadException) && e.getCause()!=null ? e.getCause() : e;
			throw new HarvestException("Exception when harvesting [" + sourceUrlString + "]: " + t.toString(), t);
		}
		finally{
			try{
				rdfHandler.closeResources();
			}
			catch (Exception e){}
		}
	}

	/**
	 * 
	 * @param sourceUrl
	 * @return
	 */
	protected static File fullFilePathForSourceUrl(String sourceUrl){

		char replacerForIllegal = '_';
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<sourceUrl.length(); i++){
			char c = sourceUrl.charAt(i);			
			// if not (latin upper case or latin lower case or numbers 0-9 or '-' or '.' or '_') then replace with replacer
			if (!(c>=65 && c<=90) && !(c>=97 && c<=122) && !(c>=48 && c<=57) && c!=45 && c!=46 && c!=95){
				c = replacerForIllegal;
			}			
			buf.append(c);
		}
		
		return new File(GeneralConfig.getProperty(GeneralConfig.HARVESTER_FILES_LOCATION), buf.append(HARVEST_FILE_NAME_EXTENSION).toString());
	}
	
	/**
	 * @return the distinctSubjectsCount
	 */
	public int getDistinctSubjectsCount() {
		return distinctSubjectsCount;
	}

	/**
	 * @return the storedTriplesCount
	 */
	public int getStoredTriplesCount() {
		return storedTriplesCount;
	}

	/**
	 * @throws HarvestException 
	 */
	protected void doHarvestStartedActions() throws HarvestException{
		
		if (this instanceof PullHarvest)
			logger.debug("Pull harvest started");
		else if (this instanceof PushHarvest)
			logger.debug("Push harvest started");
		else
			logger.debug("Harvest started");
		
		try{
			if (daoWriter!=null)
				daoWriter.writeStarted(this);
		}
		catch (DAOException e){
			throw new HarvestException(e.toString(), e);
		}
	}
	
	/**
	 * @throws HarvestException 
	 */
	protected void doHarvestFinishedActions(){
		
		// send notification of messages that occurred during the harvest
		try{
			if (notificationSender!=null)
				notificationSender.notifyMessages(this);
		}
		catch (HarvestException e){
			errors.add(e);
			logger.error("Harvest notification sender threw an error: " + e.toString(), e);
		}
		
		ArrayList<Throwable> finishedActionsErrors  = new ArrayList<Throwable>();
		
		// get the number of harvested resources now in the given source
		Integer numDocsInSource = null;
		try {
			numDocsInSource = new Integer(Searcher.getNumDocsBySourceUrl(sourceUrlString));
		}
		catch (IOException e){
			numDocsInSource = null;
			errors.add(e);
			finishedActionsErrors.add(e);
			logger.error("Error when getting the count of documents in the given source: " + e.toString(), e);
		}
		
		// call harvest finished functions of the DAO
		if (daoWriter!=null){
			try{
				daoWriter.writeFinished(this, numDocsInSource);
			}
			catch (DAOException e){
				errors.add(e);
				finishedActionsErrors.add(e);
				logger.error("Harvest DAO writer threw an exception: " + e.toString(), e);
			}
			try{
				daoWriter.writeMessages(this);
			}
			catch (DAOException e){
				errors.add(e);
				finishedActionsErrors.add(e);
				logger.error("Harvest DAO writer threw an exception: " + e.toString(), e);
			}
		}

		// send notification of errors happened when executing harvest actions 
		try{
			if (notificationSender!=null && finishedActionsErrors.size()>0){
				for (int i=0; i<finishedActionsErrors.size(); i++){
					Throwable t = finishedActionsErrors.get(i);
					notificationSender.notifyMessage("The following error occured after harvest finished: " + t.toString(), t, this);
				}
			}
		}
		catch (HarvestException ee){
			logger.error("Harvest notification sender threw an error: " + ee.toString(), ee);
		}
		
		logger.debug("Harvest finished");
	}
	
	/**
	 * @return the errors
	 */
	public List<Throwable> getErrors() {
		return errors;
	}

	/**
	 * @return the warnings
	 */
	public List<Throwable> getWarnings() {
		return warnings;
	}

	/**
	 * @return the fatalError
	 */
	public Throwable getFatalError() {
		return fatalError;
	}

	/**
	 * @return the sourceUrlString
	 */
	public String getSourceUrlString() {
		return sourceUrlString;
	}
	
	/**
	 * 
	 * @param file
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	protected File preProcess(File file, String fromUrl) throws ParserConfigurationException, SAXException, IOException{

		XmlAnalysis xmlAnalysis = new XmlAnalysis();
		xmlAnalysis.parse(file);
		
		// get schema uri, if it's not found then fall back to dtd 
		String schemaOrDtd = xmlAnalysis.getSchemaLocation();
		if (schemaOrDtd==null || schemaOrDtd.length()==0){
			schemaOrDtd = xmlAnalysis.getSystemDtd();
			if (schemaOrDtd==null || !URLUtil.isURL(schemaOrDtd)){
				schemaOrDtd = xmlAnalysis.getPublicDtd();
			}
		}
		
		// if this file has a conversion to RDF, run it and return the reference to the resulting file
		if (schemaOrDtd!=null && schemaOrDtd.length()>0){
			
			String listConversionsUrl = GeneralConfig.getRequiredProperty(GeneralConfig.XMLCONV_LIST_CONVERSIONS_URL);
			listConversionsUrl = MessageFormat.format(listConversionsUrl, Util.toArray(URLEncoder.encode(schemaOrDtd)));
			
			URL url = new URL(listConversionsUrl);
			URLConnection httpConn = url.openConnection();
			
			InputStream inputStream = null;
			try{
				inputStream = httpConn.getInputStream();
				ConversionsParser conversionsParser = new ConversionsParser();
				conversionsParser.parse(inputStream);
				String conversionId = conversionsParser.getRdfConversionId();
				if (conversionId!=null && conversionId.length()>0){
					
					String convertUrl = GeneralConfig.getRequiredProperty(GeneralConfig.XMLCONV_CONVERT_URL);
					Object[] args = new String[2];
					args[0] = URLEncoder.encode(conversionId);
					args[1] = URLEncoder.encode(fromUrl);
					convertUrl = MessageFormat.format(convertUrl, args);
					
					File convertedFile = new File(file.getAbsolutePath() + ".converted");
					FileUtil.downloadUrlToFile(convertUrl, convertedFile);
					return convertedFile;
				}
			}
			finally{
				try{
					if (inputStream!=null) inputStream.close();
				}
				catch (IOException e){}
			}
		}
		
		return file;
	}
	
	/**
	 * @param daoWriter the daoWriter to set
	 */
	public void setDaoWriter(HarvestDAOWriter daoWriter) {
		this.daoWriter = daoWriter;
	}

	/**
	 * @param notificationSender the notificationSender to set
	 */
	public void setNotificationSender(HarvestNotificationSender notificationSender) {
		this.notificationSender = notificationSender;
	}
}
