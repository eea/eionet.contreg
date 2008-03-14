package eionet.cr.harvest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.IndexReader;

import com.hp.hpl.jena.rdf.arp.ARP;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.harvest.util.RDFResource;
import eionet.cr.index.IndexException;
import eionet.cr.index.Indexer;
import eionet.cr.index.MainIndexer;
import eionet.cr.util.Identifiers;

/**
 * 
 * @author heinljab
 *
 */
public abstract class Harvest {
	
	/** */
	private static final String HARVEST_FILE_NAME_EXTENSION = ".xml";
	public static final String STATUS_STARTED = "started";
	public static final String TYPE_PULL = "pull";
	public static final String TYPE_PUSH = "push";
	
	/** */
	private static Log logger = LogFactory.getLog(Harvest.class);
	
	/** */
	protected String sourceUrlString = null;
	
	/** */
	protected Indexer indexer = new MainIndexer();

	/** */
	private int countTotalResources = 0;
	private int countEncodingSchemes = 0;
	private int countTotalStatements = 0;
	private int countLiteralStatements = 0;
	
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
	public Harvest(String sourceUrlString, HarvestDAOWriter daoWriter, HarvestNotificationSender notificationSender){
		
		if (sourceUrlString==null)
			throw new NullPointerException();
		
		this.sourceUrlString = sourceUrlString;
		this.daoWriter = daoWriter;
		this.notificationSender = notificationSender;
	}

	/**
	 * 
	 * @throws HarvestException
	 */
	public void execute() throws HarvestException{

		boolean noProblems = false;
		try{
			doHarvestStartedActions();
			doExecute();
			noProblems = true;
		}
		catch (HarvestException e){
			fatalError = e;
			throw e;
		}
		finally{
			try{
				if (noProblems)
					indexer.close();
				else
					indexer.abort();
			}
			catch (IOException e){
				errors.add(e);
				logger.error(e.toString(), e);
			}
			finally{
				doHarvestFinishedActions();
			}
		}
	}
	
	/**
	 * 
	 * @throws HarvestException
	 */
	protected abstract void doExecute() throws HarvestException;
	
	/**
	 * 
	 * @param file
	 * @throws HarvestException
	 */
	protected void harvestFile(File file) throws HarvestException{
		
		logger.debug("Parsing local file: " + file.getAbsolutePath());
		
		InputStreamReader reader = null;
		CopyOfRDFHandler rdfHandler = new CopyOfRDFHandler(sourceUrlString);
		try{			
	        reader = new InputStreamReader(new FileInputStream(file));	        	        
			ARP arp = new ARP();
	        arp.setStatementHandler(rdfHandler);
	        arp.setErrorHandler(rdfHandler);
	        arp.load(reader);
	        
	        logger.debug(rdfHandler.getCountResources() + "collected from local file: " + file.getAbsolutePath());
	        
	        errors.addAll(rdfHandler.getErrors());
	        warnings.addAll(rdfHandler.getWarnings());
	        
	        if (rdfHandler.isFatalError())
	        	throw rdfHandler.getFatalError();
	        else{
	        	updateFirstTimes(rdfHandler.getRdfResources());
	        	indexResources(rdfHandler.getRdfResources());
	        }
		}
		catch (Exception e){
			throw new HarvestException(e.toString(), e);
		}
		finally{
			try{
				logger.debug("Closing file reader: " + file.getAbsolutePath());
				if (reader!=null)
					reader.close();
			}
			catch (IOException e){
				errors.add(e);
				logger.error(e.toString(), e);
			}
		}
	}

	/**
	 * 
	 * @param resources
	 * @throws HarvestException 
	 */
	private void indexResources(Map<String,RDFResource> resources) throws HarvestException{
		
		if (resources==null || resources.size()==0)
			return;
		
		Iterator<RDFResource> iter = resources.values().iterator();
		while (iter.hasNext()){
			
			RDFResource resource = iter.next();
			
			countTotalStatements = countTotalStatements + resource.getCountTotalProperties();
			countLiteralStatements  = countLiteralStatements + resource.getCountLiteralProperties();
			
			try {
				indexer.indexRDFResource(resource);
			}
			catch (IndexException e) {
				throw new HarvestException(e.toString(), e);
			}
			
			countTotalResources++;
			if (resource.isEncodingScheme())
				countEncodingSchemes++;
		}
		
		logger.debug(countTotalResources + " resources indexed, among them " +
				countEncodingSchemes + " encoding schemes, sourceURL = " + sourceUrlString);
	}

	/**
	 * 
	 * @param resources
	 * @throws HarvestException
	 */
	private void updateFirstTimes(Map<String,RDFResource> resources) throws HarvestException{
		
		logger.debug("Updating the first-seen-times of the given resources");
		
		if (resources==null || resources.size()==0)
			return;
		
		IndexReader indexReader = null;
		try{
			indexReader = IndexReader.open(GeneralConfig.getProperty(GeneralConfig.LUCENE_INDEX_LOCATION));
			String[] fields = {Identifiers.DOC_ID, Identifiers.FIRST_SEEN_TIMESTAMP};
			FieldSelector fieldSelector = new MapFieldSelector(fields);
			int numDocs = indexReader.numDocs();
			int countUpdated = 0;
			for (int i=0; i<numDocs; i++){
				Document document = indexReader.document(i, fieldSelector);
				if (document!=null){
					String docID = document.get(Identifiers.DOC_ID);
					String firstTime = document.get(Identifiers.FIRST_SEEN_TIMESTAMP);
					if (docID!=null && firstTime!=null){
						RDFResource resource = resources.get(docID);
						if (resource!=null){
							resource.setFirstSeenTimestamp(firstTime);
							countUpdated++;
						}
					}
				}
			}
			
			logger.debug("First-seen-times updated for " + countUpdated + " resources, a total of " + numDocs + " documents was found by index reader");;
		}
		catch (Throwable t){
			throw new HarvestException("Failure when updating first times: " + t.toString(), t);
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
	 * @return the countTotalResources
	 */
	public int getCountTotalResources() {
		return countTotalResources;
	}

	/**
	 * @return the countEncodingSchemes
	 */
	public int getCountEncodingSchemes() {
		return countEncodingSchemes;
	}

	/**
	 * @return the countTotalStatements
	 */
	public int getCountTotalStatements() {
		return countTotalStatements;
	}

	/**
	 * @return the countLiteralStatements
	 */
	public int getCountLiteralStatements() {
		return countLiteralStatements;
	}
	
	/**
	 * @throws HarvestException 
	 */
	protected void doHarvestStartedActions() throws HarvestException{
		
		logger.debug("Harvested started, source URL = " + sourceUrlString);
		
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
		
		try{
			if (notificationSender!=null)
				notificationSender.notifyMessages(this);
		}
		catch (HarvestException e){
			errors.add(e);
			logger.error("Harvest notification sender threw an error: " + e.toString(), e);
		}
		
		try{
			if (daoWriter!=null){
				daoWriter.writeFinished(this);
				daoWriter.writeMessages(this);
			}
		}
		catch (DAOException e){
			errors.add(e);
			logger.error("Harvest DAO writer threw an exception: " + e.toString(), e);
			try{
				if (notificationSender!=null)
					notificationSender.notifyMessage("The following error occured after harevst finished: " + e.toString(), e, this);
			}
			catch (HarvestException ee){
				logger.error("Harvest notification sender threw an error: " + ee.toString(), ee);
			}
		}
		
		logger.debug("Harvest finished, source URL = " + sourceUrlString);
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
	
	protected java.util.Map<String,String[]>[] kala = null;
}
