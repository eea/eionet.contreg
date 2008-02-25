package eionet.cr.harvest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.arp.ARP;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.web.security.CRUser;

public class Harvester {
	
	/** */
	private static final String HARVEST_FILE_NAME_EXTENSION = ".xml";
	
	/** */
	private static Log logger = LogFactory.getLog(Harvester.class);

	/**
	 * 
	 * @param harvestSourceDTO
	 * @throws HarvestException 
	 */
	public static void pull(DefaultHarvestListener harvestListener) throws HarvestException{
		
		harvestListener.harvestStarted();
		
		File toFile = fullFilePathForSourceUrl(harvestListener.getHarvestSourceDTO().getPullUrl());
		if (toFile.exists())
			toFile.delete();
		download(harvestListener.getHarvestSourceDTO().getPullUrl(), toFile);
		harvest(harvestListener, toFile);
		
		harvestListener.harvestFinished();
	}
	
	/**
	 * 
	 * @param harvestSourceDTO
	 * @param file
	 * @throws HarvestException
	 */
	private static void harvest(DefaultHarvestListener harvestListener, File file) throws HarvestException{
		
		logger.debug("Parsing local file: " + file.getAbsolutePath());
		
		boolean exceptionCatched = false;
		InputStreamReader reader = null;
		try{
	        reader = new InputStreamReader(new FileInputStream(file));	        
	        RDFHandler handler = new RDFHandler(harvestListener.getHarvestSourceDTO().getPullUrl(), (HarvestListener)harvestListener);
			ARP arp = new ARP();
	        arp.setStatementHandler(handler);
	        arp.setErrorHandler(new SAXErrorHandler());
	        arp.load(reader);
		}
		catch (Exception e){
			exceptionCatched = true;
			HarvestException hrve = new HarvestException(e.toString(), e);
			harvestListener.setFatalException(hrve);
			throw hrve;
		}
		finally{
			try{
				logger.debug("Closing file reader: " + file.getAbsolutePath());
				if (reader!=null) reader.close();
			}
			catch (IOException e){
				logger.error("Failure when trying to close the file's InputStreamReader: " + e.toString(), e);
			}
			finally{
		        if (!exceptionCatched && harvestListener.hasFatalException())
		        	throw harvestListener.getFatalException();
			}
		}
	}
	
	/**
	 * 
	 * @param urlString
	 * @param toFile
	 * @throws HarvestException
	 */
	private static void download(String urlString, File toFile) throws HarvestException{
		
		logger.debug("Downloading from URL: " + urlString);

		InputStream istream = null;
		FileOutputStream fos = null;
		try{
			URL url = new URL(urlString);
			HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
			httpConn.setRequestProperty("Accept", "application/rdf+xml");
			
			istream = httpConn.getInputStream();
			fos = new FileOutputStream(toFile);
	        
	        int i = -1;
	        byte[] bytes = new byte[1024];
	
	        while ((i = istream.read(bytes, 0, bytes.length)) != -1)
	        	fos.write(bytes, 0, i);
		}
		catch (IOException e){
			throw new HarvestException(e.toString(), e);
		}
		finally{
			try{
				logger.debug("Closing URL input stream: " + urlString);
		        if (istream!=null) istream.close();
			}
			catch (IOException e){
				logger.error("Failed to close URL input stream: " + e.toString(), e);
			}
			try{
				logger.debug("Closing file output stream: " + toFile.getAbsolutePath());
				if (fos!=null) fos.close();
			}
			catch (IOException e){
				logger.error("Failed to close file output stream: " + e.toString(), e);
			}
		}
	}

	/**
	 * 
	 * @param url
	 * @return
	 */
	private static File fullFilePathForSourceUrl(String sourceUrl){

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
	 * 
	 * @param args
	 */
	public static void main(String[] args){

		HarvestSourceDTO harvestSourceDTO = new HarvestSourceDTO();
		harvestSourceDTO.setPullUrl("http://cdr.eionet.europa.eu/envelopes.rdf");
		
		DefaultHarvestListener harvestListener = new DefaultHarvestListener(harvestSourceDTO, "pull", null);
		try{
			Harvester.pull(harvestListener);
		}
		catch (Throwable t){			
			t.printStackTrace();
			System.out.println();
			System.out.println("=================================================");
		}
		
		System.out.println("ResourcesCalled = " + harvestListener.getCountResourcesCalled());
		System.out.println("ResourcesIndexed = " + harvestListener.getCountResourcesIndexed());
	}
}
