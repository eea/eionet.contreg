/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.harvest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.arp.ARP;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.harvest.util.ARPSource;
import eionet.cr.harvest.util.HarvestLog;
import eionet.cr.harvest.util.InputStreamBasedARPSource;
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
//	public static final String FATAL = "ftl";
//	public static final String ERROR = "err";
//	public static final String WARNING = "wrn";
//	public static final String INFO = "inf";
	
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
	protected List<String> infos = new ArrayList<String>();
	
	/** */
	private boolean deriveExtraTriples = true;
	
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
		catch (Exception e){
			
			fatalError = e;
			try{ logger.error("Exception when harvesting [" + sourceUrlString + "]: " + e.toString(), e); }catch (Exception ee){}
			
			if (e instanceof HarvestException)
				throw (HarvestException)e;
			else
				throw new HarvestException(e.toString(), e);
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
	protected void harvest(File file, String responseContentType) throws HarvestException{
		
		InputStream inputStream = null;
		try{
			try{
				file = preProcess(file, sourceUrlString, responseContentType);
				if (file==null)
					return;
				
		        inputStream = new FileInputStream(file);
			}
			catch (Exception e){
				throw new HarvestException(e.toString(), e);
			}
			
	        harvest(new InputStreamBasedARPSource(inputStream));
		}
		finally{
			try{
				if (inputStream!=null) inputStream.close();
			}
			catch (IOException e){
				errors.add(e);
				logger.error("Failed to close file input stream: " + e.toString(), e);
			}
		}
	}

	/**
	 * Harvest the given ARPSource.
	 * The caller is responsible for closing the resources that the given ARPSource uses.
	 * 
	 * @param arpSource
	 * @throws HarvestException
	 */
	protected void harvest(ARPSource arpSource) throws HarvestException{
		
		long genTime = System.currentTimeMillis();
		
		RDFHandler rdfHandler = null;
		try{
			
			rdfHandler = new RDFHandler(sourceUrlString, genTime);
			rdfHandler.setDeriveExtraTriples(deriveExtraTriples);
			if (this instanceof PushHarvest)
				rdfHandler.setClearPreviousContent(false);

			ARP arp = new ARP();
	        arp.setStatementHandler(rdfHandler);
	        arp.setErrorHandler(rdfHandler);
	        arpSource.load(arp, sourceUrlString);
	        rdfHandler.endOfFile();
	        
	        if (rdfHandler.getSaxError()!=null)
	        	errors.add(rdfHandler.getSaxError());
	        if (rdfHandler.getSaxWarning()!=null)
	        	warnings.add(rdfHandler.getSaxWarning());

	        rdfHandler.commit();
	        
	        storedTriplesCount = rdfHandler.getStoredTriplesCount();
	        distinctSubjectsCount = rdfHandler.getDistinctSubjectsCount();
		}
		catch (Exception e){
			
			try{logger.error("Harvest error: " + e.toString());}catch (Exception ee){}
			
			if (rdfHandler!=null){
				try{
					rdfHandler.rollback();
				}
				catch (Exception ee){
					logger.fatal("Harvest rollback failed", ee);
					// TODO - handle rollback failure somehow
					// (e.g. send e-mail notification, store failure into database and retry rollback at later harvests)
				}
			}
			
			Throwable t = (e instanceof LoadException) && e.getCause()!=null ? e.getCause() : e;
			throw new HarvestException(t.toString(), t);
		}
		finally{
			if (rdfHandler!=null){
				try{
					rdfHandler.closeResources();
				}
				catch (Exception e){}
			}
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
		
		// call harvest finished functions of the DAO
		if (daoWriter!=null){
			try{
				daoWriter.writeFinished(this);
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

		// send notification of errors happened when executing harvest finished actions 
		try{
			if (notificationSender!=null && finishedActionsErrors.size()>0){
				notificationSender.notifyMessagesAfterHarvest(finishedActionsErrors, this);
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
	protected File preProcess(File file, String fromUrl, String responseContentType)
                                                                throws ParserConfigurationException, SAXException, IOException{

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
		
			/* get the URL of the conversion service method that returns the list of available conversions */
			
			String listConversionsUrl = GeneralConfig.getRequiredProperty(GeneralConfig.XMLCONV_LIST_CONVERSIONS_URL);
			listConversionsUrl = MessageFormat.format(listConversionsUrl, Util.toArray(URLEncoder.encode(schemaOrDtd)));

			/* open connection to the list-conversions URL */
			
			URL url = new URL(listConversionsUrl);
			URLConnection httpConn = url.openConnection();
			
			/* parse the returned input stream with eionet.cr.util.xml.ConversionsParser */
			
			InputStream inputStream = null;
			try{
				inputStream = httpConn.getInputStream();				
				ConversionsParser conversionsParser = new ConversionsParser();
				conversionsParser.parse(inputStream);
				
				/* see if ConversionsParser found any conversions available */
				
				String conversionId = conversionsParser.getRdfConversionId();
				if (conversionId!=null && conversionId.length()>0){
					
					/* prepare conversion URL */
					
					String convertUrl = GeneralConfig.getRequiredProperty(GeneralConfig.XMLCONV_CONVERT_URL);
					Object[] args = new String[2];
					args[0] = URLEncoder.encode(conversionId);
					args[1] = URLEncoder.encode(fromUrl);
					convertUrl = MessageFormat.format(convertUrl, args);
					
					/* run conversion and save the response to file */
					
					File convertedFile = new File(file.getAbsolutePath() + ".converted");
					FileUtil.downloadUrlToFile(convertUrl, convertedFile);
					return convertedFile;
				}
				else{
					if (responseContentType!=null){
						responseContentType = responseContentType.trim();
						if (!responseContentType.startsWith("text/xml")
								&& !responseContentType.startsWith("application/xml")
								&& !responseContentType.startsWith("application/rdf+xml")){
							
							logger.debug("Skipping, because no conversions found and cannot parse this media type: " + responseContentType);
							return null;
						}
					}
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

	/**
	 * @return the infos
	 */
	public List<String> getInfos() {
		return infos;
	}

	/**
	 * @param deriveExtraTriples the deriveExtraTriples to set
	 */
	public void setDeriveExtraTriples(boolean deriveExtraTriples) {
		this.deriveExtraTriples = deriveExtraTriples;
	}
}
