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
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.ARP;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.persist.PersisterConfig;
import eionet.cr.harvest.util.HarvestLog;
import eionet.cr.harvest.util.arp.ARPSource;
import eionet.cr.util.Hashes;

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
	public static final String HARVESTER_URI = "http://cr.eionet.europa.eu/harvester";
	
	/** */
	protected String sourceUrlString = null;
	protected Log logger = null;
	
	/** */
	private int distinctSubjectsCount = 0;
	private int storedTriplesCount = 0;
	
	/** */
	protected HarvestDAOWriter daoWriter = null;
	private HarvestNotificationSender notificationSender = null;
	
	/** */
	protected Throwable fatalError = null;
	protected List<Throwable> errors = new ArrayList<Throwable>();
	protected List<Throwable> warnings = new ArrayList<Throwable>();
	protected List<String> infos = new ArrayList<String>();
	
	/** */
	private boolean deriveInferredTriples = true;
	
	/** */
	protected HarvestDTO previousHarvest = null;

	/** */
	protected SubjectDTO sourceMetadata;
	
	/** */
	protected boolean clearPreviousContent = true;
	
	/** */
	protected long sourceLastModified;
	
	/** */
	protected boolean rdfContentFound = false;
	
	/** */
	protected DAOFactory daoFactory = DAOFactory.get();
	
	/** */
	protected static SimpleDateFormat lastRefreshedDateFormat =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	/**
	 * 
	 * @param sourceUrlString
	 */
	protected Harvest(String sourceUrlString){
		
		if (sourceUrlString==null)
			throw new IllegalArgumentException("Harvest source URL cannot be null");
		else if (sourceUrlString.indexOf("#")>=0){
			throw new IllegalArgumentException("Harvest source URL must no contain a fragment part");
		}
		
		this.sourceUrlString = sourceUrlString;
		this.logger = new HarvestLog(sourceUrlString, LogFactory.getLog(this.getClass()));
		this.sourceMetadata = new SubjectDTO(sourceUrlString, false);
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
	 * 
	 * @param arpSource
	 * @throws HarvestException
	 */
	protected void harvest(ARPSource arpSource) throws HarvestException{
		harvest(arpSource, false);
	}

	/**
	 * Harvest the given ARPSource.
	 * The caller is responsible for closing the resources that the given ARPSource uses.
	 * 
	 * @param arpSource
	 * @param ignoreParsingError
	 * @throws HarvestException
	 */
	protected void harvest(ARPSource arpSource, boolean ignoreParsingError) throws HarvestException{
		
		RDFHandler rdfHandler = null;
		try{
			
			PersisterConfig config = new PersisterConfig(
					deriveInferredTriples,
					clearPreviousContent,
					sourceLastModified,
					sourceUrlString,
					System.currentTimeMillis(),
					Hashes.spoHash(sourceUrlString),
					null);
			rdfHandler = createRDFHandler(config);
			DefaultErrorHandler errorHandler = new DefaultErrorHandler();
			if (arpSource!=null){
				ARP arp = new ARP();
		        arp.setStatementHandler(rdfHandler);
		        arp.setErrorHandler(errorHandler);
		        
		        if (!ignoreParsingError){
		        	arpSource.load(arp, sourceUrlString);
		        }
		        else{
		        	try{
		        		arpSource.load(arp, sourceUrlString);
		        	}
		        	catch (SAXException e){
		        		logger.info("Following exception happened when parsing as RDF", e);
		        	}
		        	catch (RDFLoadingException e){
		        		Throwable cause = e.getCause();
		        		if (cause instanceof SAXParseException){
		        			logger.info("Following exception happened when parsing as RDF", e);
		        		}
		        		else{
		        			throw e;
		        		}
		        	}
		        }
			}
			
			rdfContentFound = rdfHandler.isRdfContentFound();
			if (rdfContentFound==false){
				logger.debug("No content found by RDF parser");
			}
			
			if (sourceMetadata.getPredicateCount()>0){
				logger.debug("Storing auto-generated triples for the source");
				rdfHandler.addSourceMetadata(sourceMetadata);
			}
			
	        rdfHandler.endOfFile();
	        
	        if (errorHandler.getSaxError()!=null)
	        	errors.add(errorHandler.getSaxError());
	        if (errorHandler.getSaxWarning()!=null)
	        	warnings.add(errorHandler.getSaxWarning());

	        rdfHandler.commit();

	        rdfContentFound = rdfHandler.isRdfContentFound();
	        storedTriplesCount = rdfHandler.getStoredTriplesCount();
	        distinctSubjectsCount = rdfHandler.getSubjectCount();
	        
	        logger.debug("Harvest committed. " + storedTriplesCount + " triples stored. "
	        		+ distinctSubjectsCount + " subjects found in source");
		}
		catch (Exception e){
			
			try{logger.error("Harvest error: " + e.toString());}catch (Exception ee){}
			
			if (e instanceof SQLException){
				logger.error("Next exception: ", ((SQLException)e).getNextException());
			}
			
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
			
			Throwable t = (e instanceof RDFLoadingException) && e.getCause()!=null ? e.getCause() : e;
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
	 */
	protected RDFHandler createRDFHandler(PersisterConfig config){
		return new RDFHandler(config);
	}

	/**
	 * 
	 * @param sourceUrl
	 * @return
	 */
	protected static File fullFilePathForSourceUrl(String sourceUrl){
		
		if (StringUtils.isBlank(sourceUrl)){
			return null;
		}
		
		String folder = GeneralConfig.getRequiredProperty(GeneralConfig.HARVESTER_FILES_LOCATION);
		String fileName = new StringBuilder(Hashes.md5(sourceUrl)).append("_").
		append(System.currentTimeMillis()).append(HARVEST_FILE_NAME_EXTENSION).toString();

		return new File(folder, fileName);
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
		
		try{
			if (daoWriter!=null){
				daoWriter.writeStarted(this);
			}
		}
		catch (DAOException e){
			throw new HarvestException(e.toString(), e);
		}
	}

	/**
	 * Saves the "harvest-finished" status and any harvest error/warning messages into the database,
	 * and sends e-mail notification of those messages.
	 */
	protected void doHarvestFinishedActions(){
		
		// send e-mail notification of messages that occurred DURING the harvest
		// (see below about messages that occurred AFTER the harvest)
		try{
			if (notificationSender!=null){
				notificationSender.notifyMessages(this);
			}
		}
		catch (HarvestException e){
			errors.add(e);
			logger.error("Harvest notification sender threw an error: " + e.toString(), e);
		}
		
		// let's be so ambitious and try to remember even the errors that will happen during the
		// finishing actions
		ArrayList<Throwable> finishingErrors  = new ArrayList<Throwable>();
		
		// log harvest finished event into the database
		if (daoWriter!=null){
			try{
				daoWriter.writeFinished(this);
			}
			catch (DAOException e){
				errors.add(e);
				finishingErrors.add(e);
				logger.error("Harvest DAO writer threw an exception: " + e.toString(), e);
			}
			try{
				daoWriter.writeMessages(this);
			}
			catch (DAOException e){
				errors.add(e);
				finishingErrors.add(e);
				logger.error("Harvest DAO writer threw an exception: " + e.toString(), e);
			}
		}

		// send e-mail notification of finishing errors
		try{
			if (notificationSender!=null && finishingErrors.size()>0){
				notificationSender.notifyMessagesAfterHarvest(finishingErrors, this);
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
	 * @param deriveInferredTriples the deriveInferredTriples to set
	 */
	public void setDeriveInferredTriples(boolean deriveInferredTriples) {
		this.deriveInferredTriples = deriveInferredTriples;
	}

	/**
	 * @param previousHarvest the previousHarvest to set
	 */
	public void setPreviousHarvest(HarvestDTO previousHarvest) {
		this.previousHarvest = previousHarvest;
	}

	/**
	 * @param distinctSubjectsCount the distinctSubjectsCount to set
	 */
	protected void setDistinctSubjectsCount(int distinctSubjectsCount) {
		this.distinctSubjectsCount = distinctSubjectsCount;
	}

	/**
	 * @param storedTriplesCount the storedTriplesCount to set
	 */
	protected void setStoredTriplesCount(int storedTriplesCount) {
		this.storedTriplesCount = storedTriplesCount;
	}

	/**
	 * @return the rdfContentFound
	 */
	public boolean isRdfContentFound() {
		return rdfContentFound;
	}
}
