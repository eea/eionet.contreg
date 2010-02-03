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
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.harvest.persist.mysql;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.cr.config.GeneralConfig;
import eionet.cr.harvest.persist.IHarvestPersister;
import eionet.cr.harvest.persist.PersisterConfig;
import eionet.cr.harvest.persist.PersisterException;
import eionet.cr.util.Hashes;
import eionet.cr.util.Util;
import eionet.cr.util.YesNoBoolean;

/**
 * MySQLOptimizedPersister, which uses LOAD DATA INFILE to
 * store triples and resources. 
 * TODO not fully implemented.
 * 
 * 
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class MySQLOptimizedPersister implements IHarvestPersister {
	
	private Logger logger = Logger.getLogger(MySQLOptimizedPersister.class);
	
	private static final String TEMP_FILE_NAME = "temp_harvest.csv";
	private static final String SOURCE_TEMP_FILE = "sources_harvest.csv";
	
	private FileWriter triplesWriter;
	private FileWriter sourcesWriter;
	
	private File tripleFile;
	private File sourcesFile;
	
	private PersisterConfig config;
	
	/**
	 * @param config
	 */
	public MySQLOptimizedPersister(PersisterConfig config) {
		this.config = config;
	}

	/** 
	 * @see eionet.cr.harvest.persist.IHarvestPersister#addResource(java.lang.String, long)
	 * {@inheritDoc}
	 */
	public void addResource(String uri, long uriHash) throws PersisterException {
		StringBuffer sb = new StringBuffer()
				.append(uri)
				.append(',')
				.append(uriHash)
				.append('\n');
		try {
			sourcesWriter.write(sb.toString());
			sourcesWriter.flush();
		} catch (IOException e) {
			throw new PersisterException(e.getMessage(), e);
		}
	}

	/** 
	 * @see eionet.cr.harvest.persist.IHarvestPersister#addTriple(long, boolean, long, java.lang.String, java.lang.String, boolean, boolean, long)
	 * {@inheritDoc}
	 */
	public void addTriple(long subjectHash, boolean anonSubject, long predicateHash, String object, long objectHash, String objectLang,
			boolean litObject, boolean anonObject, long objSourceObject) throws PersisterException {
		StringBuffer sb = new StringBuffer()
				.append(escapeCSV(subjectHash)).append(',')
				.append(escapeCSV(predicateHash)).append(',')
				.append(escapeCSV(object)).append(',')
				.append(escapeCSV(Hashes.spoHash(object))).append(',')
				.append(escapeCSV(Util.toDouble(object))).append(',')
				.append(escapeCSV(YesNoBoolean.format(anonSubject))).append(',')
				.append(escapeCSV(YesNoBoolean.format(anonObject))).append(',')
				.append(escapeCSV(YesNoBoolean.format(litObject))).append(',')
				.append(escapeCSV(objectLang == null ? "" : objectLang)).append(',')
				.append(escapeCSV(objSourceObject == 0 ? 0 : config.getSourceUrlHash())).append(',')
				.append(escapeCSV(objSourceObject == 0 ? 0 : config.getGenTime())).append(',')
				.append(escapeCSV(objSourceObject)).append('\n');
		try {
			triplesWriter.write(sb.toString());
			triplesWriter.flush();
		} catch (IOException e) {
			throw new PersisterException(e.getMessage(), e);
		}
	}
	private String escapeCSV(String value) {
		if (value == null) {
			return "null";
		} else if (StringUtils.isBlank(value)) {
			return "\"\"";
		} else {
			return StringEscapeUtils.escapeCsv(
					StringUtils.trim(value));
		}
	}
	
	private String escapeCSV(Long value) {
		if (value == null) {
			return "null";
		}
		return value.toString();
	}
	
	private String escapeCSV(Double value) {
		if (value == null) {
			return "null";
		}
		return value.toString();
	}

	/** 
	 * @see eionet.cr.harvest.persist.IHarvestPersister#closeResources()
	 * {@inheritDoc}
	 */
	public void closeResources() throws PersisterException {
		try {
			triplesWriter.close();
			sourcesWriter.close();
			deleteTempFiles();
		} catch (Exception ignored) {
			logger.warn("exception was raised while closing resources ", ignored);
		}
	}

	/** 
	 * @see eionet.cr.harvest.persist.IHarvestPersister#commit()
	 * {@inheritDoc}
	 */
	public void commit() throws PersisterException {
		// TODO Auto-generated method stub
	}

	/** 
	 * @see eionet.cr.harvest.persist.IHarvestPersister#endOfFile()
	 * {@inheritDoc}
	 */
	public void endOfFile() throws PersisterException {
		// TODO Auto-generated method stub
	}

	/** 
	 * @see eionet.cr.harvest.persist.IHarvestPersister#openResources()
	 * {@inheritDoc}
	 */
	public void openResources() throws PersisterException {
		logger.debug("allocating writers");
		String tempFolder = GeneralConfig.getRequiredProperty(GeneralConfig.HARVESTER_FILES_LOCATION);
		try {
			tripleFile = new File(tempFolder + File.separator+ TEMP_FILE_NAME);
			tripleFile.createNewFile();
			sourcesFile = new File(tempFolder + File.separator + SOURCE_TEMP_FILE);
			sourcesFile.createNewFile();
			triplesWriter = new FileWriter(tripleFile);
			sourcesWriter = new FileWriter(sourcesFile, false);
		} catch (IOException e) {
			throw new PersisterException(e.getMessage(), e);
		}
	}

	/** 
	 * @see eionet.cr.harvest.persist.IHarvestPersister#rollback()
	 * {@inheritDoc}
	 */
	public void rollback() throws PersisterException {
		// TODO Auto-generated method stub
	}

	/** 
	 * @see eionet.cr.harvest.persist.IHarvestPersister#rollbackUnfinishedHarvests()
	 * {@inheritDoc}
	 */
	public void rollbackUnfinishedHarvests() throws PersisterException {
		deleteTempFiles();
	}

	/** 
	 * @see eionet.cr.harvest.persist.IHarvestPersister#tempCommit()
	 * {@inheritDoc}
	 */
	public void tempCommit() throws PersisterException {
		//not needed
	}
	
	private void deleteTempFiles() {
//		new File(TEMP_FILE_NAME).delete();
//		new File(SOURCE_TEMP_FILE).delete();
	}

	/** 
	 * @see eionet.cr.harvest.persist.IHarvestPersister#getStoredTripleCount()
	 * {@inheritDoc}
	 */
	public int getStoredTripleCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/** 
	 * @see eionet.cr.harvest.persist.IHarvestPersister#getDistinctSubjectCount()
	 * {@inheritDoc}
	 */
	public int getDistinctSubjectCount() {
		// TODO Auto-generated method stub
		return 0;
	}

}
