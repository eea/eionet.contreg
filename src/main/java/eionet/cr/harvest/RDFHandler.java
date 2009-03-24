package eionet.cr.harvest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.ALiteral;
import com.hp.hpl.jena.rdf.arp.AResource;
import com.hp.hpl.jena.rdf.arp.StatementHandler;

import eionet.cr.common.LabelPredicates;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.UnfinishedHarvestDTO;
import eionet.cr.harvest.util.DedicatedHarvestSourceTypes;
import eionet.cr.harvest.util.HarvestLog;
import eionet.cr.util.Hashes;
import eionet.cr.util.UnicodeUtils;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.web.security.CRUser;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class RDFHandler implements StatementHandler, ErrorHandler{
	
	/** */
	private List<SAXParseException> saxErrors = new ArrayList<SAXParseException>();
	private List<SAXParseException> saxWarnings = new ArrayList<SAXParseException>();
	
	/** */
	private long anonIdSeed;
	
	/** */
	private HashSet<String> usedNamespaces = new HashSet();
	
	/** */
	private String sourceUrl;
	private long sourceUrlHash;
	private long genTime;
	private Log logger;
	
	/** */
	private PreparedStatement preparedStatementForTriples;
	private PreparedStatement preparedStatementForResources;
	
	/** */
	private Connection connection;

	/** */
	private int storedTriplesCount = 0;
	private int distinctSubjectsCount = 0;
	
	/** */
	private Long currentSubjectHash;
	private HashSet distinctSubjects = new HashSet();
	
	/** */
	private boolean clearPreviousContent = true;
	
	/** */
	private boolean parsingStarted = false;

	/**
	 * 
	 */
	public RDFHandler(String sourceUrl, long genTime){
		
		if (sourceUrl==null || sourceUrl.length()==0 || genTime<=0)
			throw new IllegalArgumentException();
		
		this.sourceUrl = sourceUrl;
		this.sourceUrlHash = Hashes.spoHash(sourceUrl);
		this.genTime = genTime;
		this.logger = new HarvestLog(sourceUrl, genTime, LogFactory.getLog(this.getClass()));
		
		// set the hash-seed for anonymous ids
		anonIdSeed = Hashes.spoHash(sourceUrl + String.valueOf(genTime));
	}

	/*
	 *  (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.StatementHandler#statement(com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.AResource)
	 */
	public void statement(AResource subject, AResource predicate, AResource object){
		
		statement(subject, predicate, object.isAnonymous() ? object.getAnonymousID() : object.getURI(), "", false, object.isAnonymous());
	}

	/*
	 *  (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.StatementHandler#statement(com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.AResource, com.hp.hpl.jena.rdf.arp.ALiteral)
	 */
	public void statement(AResource subject, AResource predicate, ALiteral object){
		
		statement(subject, predicate, object.toString(), object.getLang(), true, false);
	}

	/**
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 * @param objectLang
	 * @param litObject
	 * @param anonObject
	 */
	private void statement(AResource subject, AResource predicate,
							String object, String objectLang, boolean litObject, boolean anonObject){

		try{
			if (parsingStarted==false){
				onParsingStarted();
				parsingStarted = true;
			}
			
			// we ignore statements with anonymous predicates
			if (predicate.isAnonymous())
				return;

			// we ignore literal objects with length=0
			if (litObject && object.length()==0)
				return;

			parseForUsedNamespaces(predicate);
			
			long subjectHash = spoHash(subject.isAnonymous() ? subject.getAnonymousID() : subject.getURI(), subject.isAnonymous());
			long predicateHash = spoHash(predicate.getURI(), false);
			if (litObject)
				object = UnicodeUtils.replaceEntityReferences(object);
			
			int i = storeTriple(subjectHash, subject.isAnonymous(), predicateHash, object, objectLang, litObject, anonObject);
			storeResource(predicate.getURI(), predicateHash);
			if (!subject.isAnonymous()){
				storeResource(subject.getURI(), subjectHash);
			}
		}
		catch (Exception e){
			throw new LoadException(e.toString(), e);
		}
	}
	
	/**
	 * @throws SQLException 
	 * 
	 */
	private void onParsingStarted() throws SQLException{
		
		// make sure SPO_TEMP and RESOURCE_TEMP are empty, because we do only one harvest at a time
		// and so any possible leftovers from previous harvest must be deleted)
		clearTemporaries();
		
		// create unfinished harvest flag for the current harvest
		raiseUnfinishedHarvestFlag();
		
		// store the hash of the source itself
		storeResource(sourceUrl, sourceUrlHash);
	}
	
	/**
	 * @throws SQLException 
	 * 
	 */
	private void raiseUnfinishedHarvestFlag() throws SQLException{
		
		StringBuffer buf = new StringBuffer();
		buf.append("insert into UNFINISHED_HARVEST (SOURCE, GEN_TIME) values (").
		append(sourceUrlHash).append(", ").append(genTime).append(")");
		
		SQLUtil.executeUpdate(buf.toString(), getConnection());
	}
	
	/**
	 * 
	 * @throws SQLException
	 */
	private void deleteUnfinishedHarvestFlag() throws SQLException{
		RDFHandler.deleteUnfinishedHarvestFlag(this.sourceUrlHash, this.genTime, this.getConnection());
	}

	/**
	 * 
	 * @throws SQLException
	 */
	private static void deleteUnfinishedHarvestFlag(long sourceUrlHash, long genTime, Connection conn) throws SQLException{
		
		StringBuffer buf = new StringBuffer();
		buf.append("delete from UNFINISHED_HARVEST where SOURCE=").append(sourceUrlHash).append(" and GEN_TIME=").append(genTime);
		
		SQLUtil.executeUpdate(buf.toString(), conn);
	}
	
	/**
	 * 
	 * @param subject
	 * @param anonSubject
	 * @param predicate
	 * @param anonPredicate
	 * @param object
	 * @param objectLang
	 * @param litObject
	 * @param anonObject
	 * @throws SQLException 
	 */
	private int storeTriple(long subjectHash, boolean anonSubject, long predicateHash,
							String object, String objectLang, boolean litObject, boolean anonObject) throws SQLException{
		
		if (preparedStatementForTriples==null){
			prepareStatementForTriples();
			logger.debug("Started storing triples");
		}
		
		preparedStatementForTriples.setLong  ( 1, subjectHash);
		preparedStatementForTriples.setLong  ( 2, predicateHash);
		preparedStatementForTriples.setString( 3, object);
		preparedStatementForTriples.setLong  ( 4, spoHash(object, anonObject));
		preparedStatementForTriples.setString( 5, YesNoBoolean.format(anonSubject));
		preparedStatementForTriples.setString( 6, YesNoBoolean.format(anonObject));
		preparedStatementForTriples.setString( 7, YesNoBoolean.format(litObject));
		preparedStatementForTriples.setString( 8, objectLang==null ? "" : objectLang);
		
		int i = preparedStatementForTriples.executeUpdate();
		if (currentSubjectHash==null || currentSubjectHash.longValue()!=subjectHash){
			currentSubjectHash = subjectHash;
			distinctSubjects.add(Long.valueOf(subjectHash));
		}
		
		return i;
	}

	/**
	 * 
	 * @param uri
	 * @param uriHash
	 * @param type
	 * @throws SQLException 
	 */
	private int storeResource(String uri, long uriHash) throws SQLException{

		if (preparedStatementForResources==null){
			prepareStatementForResources();
			logger.debug("Started storing resources");
		}
		
		preparedStatementForResources.setString(1, uri);
		preparedStatementForResources.setLong(2, uriHash);
		
		return preparedStatementForResources.executeUpdate();
	}
	
	/**
	 * 
	 * @param s
	 * @param isAnonymous
	 * @return
	 */
	private long spoHash(String s, boolean isAnonymous){
		return isAnonymous ? Hashes.spoHash(s, anonIdSeed) : Hashes.spoHash(s);
	}

	/**
	 * 
	 * @param predicate
	 */
	private void parseForUsedNamespaces(AResource predicate){
		
		if (!predicate.isAnonymous()){
			String predicateUri = predicate.getURI();
			int i = predicateUri.lastIndexOf("#");
            if (i<0)
            	i = predicateUri.lastIndexOf("/");
            if (i>0){
                if (predicateUri.charAt(i)=='/')
                	i++;
                usedNamespaces.add(predicateUri.substring(0, i));
            }
		}
	}

	/**
	 * 
	 * @throws SQLException
	 */
	private void prepareStatementForTriples() throws SQLException{
		
		StringBuffer buf = new StringBuffer();
        buf.append("insert into SPO_TEMP (SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ").
        append("ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        preparedStatementForTriples = getConnection().prepareStatement(buf.toString());
	}

	/**
	 * 
	 * @throws SQLException 
	 */
	private void prepareStatementForResources() throws SQLException{

        preparedStatementForResources = getConnection().prepareStatement("insert ignore into RESOURCE_TEMP (URI, URI_HASH) VALUES (?, ?)");
	}

	/**
	 * @return the connection
	 * @throws SQLException 
	 */
	private Connection getConnection() throws SQLException {
		
		if (connection==null){
			connection = ConnectionUtil.getConnection();
		}
		return connection;
	}
	
	/**
	 * 
	 */
	protected void closeResources(){
		
		SQLUtil.close(preparedStatementForTriples);
		SQLUtil.close(preparedStatementForResources);
		SQLUtil.close(connection);
	}

	/**
	 * @throws SQLException 
	 * 
	 */
	protected void commit() throws SQLException{
		
		commitTriples();
		commitResources();
		
		deriveParentProperties();
		deriveParentClasses();
		
		deriveLabels();
		extractNewHarvestSources();
		
		try{
			clearTemporaries();
		}
		catch (Exception e){}
		
		deleteUnfinishedHarvestFlag();
	}
	
	/**
	 * @throws SQLException 
	 * 
	 */
	private void commitTriples() throws SQLException{

		/* copy triples from SPO_TEMP into SPO */

		logger.debug("Copying triples from SPO_TEMP into SPO");
		
		StringBuffer buf = new StringBuffer();
		buf.append("insert high_priority into SPO (").
		append("SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, SOURCE, GEN_TIME").
		append(") select distinct SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, ").
		append(sourceUrlHash).append(", ").append(genTime).append(" from SPO_TEMP");
		
		storedTriplesCount = SQLUtil.executeUpdate(buf.toString(), getConnection());
		distinctSubjectsCount = distinctSubjects.size();
		
		logger.debug(storedTriplesCount + " triples inserted into SPO");

		if (clearPreviousContent){
			
			/* delete SPO records from previous harvests of this source */
	
			logger.debug("Deleting SPO rows of previous harvests");
			
			buf = new StringBuffer("delete from SPO where SOURCE=");
			buf.append(sourceUrlHash).append(" and GEN_TIME<").append(genTime);
			
			SQLUtil.executeUpdate(buf.toString(), getConnection());
		}
	}

	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	private void commitResources() throws SQLException{
		
		logger.debug("Copying resources from RESOURCE_TEMP into RESOURCE");
		
		StringBuffer buf = new StringBuffer();
		buf.append("insert high_priority ignore into RESOURCE (URI, URI_HASH, FIRSTSEEN_SOURCE, FIRSTSEEN_TIME) ").
		append("select URI, URI_HASH, ").append(sourceUrlHash).append(", ").append(genTime).append(" from RESOURCE_TEMP");
		
		int i = SQLUtil.executeUpdate(buf.toString(), getConnection());
		logger.debug(i + " resources inserted into RESOURCE");
	}
	
	/**
	 * @throws SQLException 
	 * 
	 */
	private void deriveLabels() throws SQLException{
		
		logger.debug("Deriving labels");
		
		/* Derive labels FOR freshly harvested source. */
		
		StringBuffer buf = new StringBuffer().
		append("insert ignore into SPO ").
		append("(SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, ").
		append("OBJ_DERIV_SOURCE, OBJ_DERIV_SOURCE_GEN_TIME, OBJ_SOURCE_OBJECT, SOURCE, GEN_TIME) ").
		append("select distinct SPO_FRESH.SUBJECT, SPO_FRESH.PREDICATE, SPO_LABEL.OBJECT, SPO_LABEL.OBJECT_HASH, SPO_FRESH.ANON_SUBJ, ").
		append("'N' as ANON_OBJ, 'Y' as LIT_OBJ, SPO_LABEL.OBJ_LANG, ").
		append("SPO_LABEL.SOURCE as OBJ_DERIV_SOURCE, SPO_LABEL.GEN_TIME as OBJ_DERIV_SOURCE_GEN_TIME, ").
		append("SPO_FRESH.OBJECT_HASH as OBJ_SOURCE_OBJECT, SPO_FRESH.SOURCE, SPO_FRESH.GEN_TIME ").
		append("from SPO as SPO_FRESH, SPO as SPO_LABEL ").
		append("where SPO_FRESH.SOURCE=").append(sourceUrlHash).
		append(" and SPO_FRESH.GEN_TIME=").append(genTime).
		append(" and SPO_FRESH.ANON_OBJ='N' and SPO_FRESH.LIT_OBJ='N' and SPO_FRESH.OBJECT_HASH=SPO_LABEL.SUBJECT and ").
		append("SPO_LABEL.LIT_OBJ='Y' and SPO_LABEL.ANON_OBJ='N' and ").
		append("SPO_LABEL.PREDICATE in (").
		append(LabelPredicates.getCommaSeparatedHashes()).
		append(")");
		
		int i = SQLUtil.executeUpdate(buf.toString(), getConnection());

		/* Derive labels FROM freshly harvested source. */
		
		buf = new StringBuffer().
		append("insert ignore into SPO (SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, ").
		append("OBJ_DERIV_SOURCE, OBJ_DERIV_SOURCE_GEN_TIME, OBJ_SOURCE_OBJECT, SOURCE, GEN_TIME) ").
		append("select distinct SPO_ALL.SUBJECT, SPO_ALL.PREDICATE, SPO_FRESH.OBJECT, SPO_FRESH.OBJECT_HASH, SPO_ALL.ANON_SUBJ, ").
		append("'N' as ANON_OBJ, 'Y' as LIT_OBJ, SPO_FRESH.OBJ_LANG, ").
		append("SPO_FRESH.SOURCE as OBJ_DERIV_SOURCE, SPO_FRESH.GEN_TIME as OBJ_DERIV_SOURCE_GEN_TIME, ").
		append("SPO_ALL.OBJECT_HASH as OBJ_SOURCE_OBJECT, SPO_ALL.SOURCE, SPO_ALL.GEN_TIME ").
		append("from SPO as SPO_ALL, SPO as SPO_FRESH ").
		append("where SPO_ALL.LIT_OBJ='N' and SPO_ALL.OBJECT_HASH=SPO_FRESH.SUBJECT and ").
		append("SPO_FRESH.SOURCE=").append(sourceUrlHash).
		append(" and SPO_FRESH.GEN_TIME=").append(genTime).
		append(" and SPO_FRESH.LIT_OBJ='Y' and SPO_FRESH.ANON_OBJ='N' and ").
		append("SPO_FRESH.PREDICATE in (").
		append(LabelPredicates.getCommaSeparatedHashes()).
		append(")");
		
		int j = SQLUtil.executeUpdate(buf.toString(), getConnection());
		
		logger.debug(i + " labels derived FOR and " + j + " labels derived FROM the current harvest");
	}
	
	/**
	 * 
	 * @throws SQLException
	 */
	private void deriveParentProperties() throws SQLException{
		
		logger.debug("Deriving parent-properties");
		
		/* Derive parent-properties FOR freshly harvested source. */
		
		StringBuffer buf = new StringBuffer().
		append("insert ignore into SPO ").
		append("(SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, ").
		append("OBJ_DERIV_SOURCE, OBJ_DERIV_SOURCE_GEN_TIME, SOURCE, GEN_TIME) ").
		append("select distinct SPO_FRESH.SUBJECT, SUBPROP.OBJECT_HASH as PARENT_PREDICATE, ").
		append("SPO_FRESH.OBJECT, SPO_FRESH.OBJECT_HASH, SPO_FRESH.ANON_SUBJ, SPO_FRESH.ANON_OBJ, ").
		append("SPO_FRESH.LIT_OBJ, SPO_FRESH.OBJ_LANG, ").
		append("SUBPROP.SOURCE as DERIV_SOURCE, SUBPROP.GEN_TIME as DERIV_SOURCE_GEN_TIME, SPO_FRESH.SOURCE, SPO_FRESH.GEN_TIME ").
		append("from SPO as SPO_FRESH, SPO as SUBPROP ").
		append("where SPO_FRESH.SOURCE=").append(sourceUrlHash).
		append(" and SPO_FRESH.GEN_TIME=").append(genTime).
		append(" and SPO_FRESH.PREDICATE=SUBPROP.SUBJECT").
		append(" and SUBPROP.PREDICATE=").append(Hashes.spoHash(Predicates.RDFS_SUBPROPERTY_OF)). 
		append(" and SUBPROP.LIT_OBJ='N' and SUBPROP.ANON_OBJ='N'");
		
		int i = SQLUtil.executeUpdate(buf.toString(), getConnection());

		/* Derive parent-properties FROM freshly harvested source. */
		
		buf = new StringBuffer().
		append("insert ignore into SPO (SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, ").
		append("OBJ_DERIV_SOURCE, OBJ_DERIV_SOURCE_GEN_TIME, SOURCE, GEN_TIME) ").		
		append("select distinct SPO.SUBJECT, SPO_FRESH.OBJECT_HASH as PARENT_PRED, SPO.OBJECT, SPO.OBJECT_HASH, ").
		append("SPO.ANON_SUBJ, SPO.ANON_OBJ, SPO.LIT_OBJ, SPO.OBJ_LANG, ").
		append("SPO_FRESH.SOURCE as DERIV_SOURCE, SPO_FRESH.GEN_TIME as DERIV_SOURCE_GEN_TIME, SPO.SOURCE, SPO.GEN_TIME ").
		append("from SPO, SPO as SPO_FRESH").
		append(" where SPO_FRESH.SOURCE=").append(sourceUrlHash).
		append(" and SPO_FRESH.GEN_TIME=").append(genTime).
		append(" and SPO_FRESH.PREDICATE=").append(Hashes.spoHash(Predicates.RDFS_SUBPROPERTY_OF)). 
		append(" and SPO_FRESH.LIT_OBJ='N' and SPO_FRESH.ANON_OBJ='N' and SPO_FRESH.SUBJECT=SPO.PREDICATE");
		
		int j = SQLUtil.executeUpdate(buf.toString(), getConnection());
		
		logger.debug(i + " parent-properties derived FOR and " + j + " parent-properties derived FROM the current harvest");
	}

	/**
	 * 
	 * @throws SQLException
	 */
	private void deriveParentClasses() throws SQLException{
		
		logger.debug("Deriving parent-classes");
		
		/* Derive parent-classes FOR freshly harvested source. */
		
		StringBuffer buf = new StringBuffer().
		append("insert ignore into SPO ").
		append("(SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, ").
		append("OBJ_DERIV_SOURCE, OBJ_DERIV_SOURCE_GEN_TIME, SOURCE, GEN_TIME) ").
		append("select distinct SPO_FRESH.SUBJECT, SPO_FRESH.PREDICATE, SUBCLASS.OBJECT, ").
		append("SUBCLASS.OBJECT_HASH, SPO_FRESH.ANON_SUBJ, 'N' as ANON_OBJ, 'N' as LIT_OBJ, SUBCLASS.OBJ_LANG, ").
		append("SUBCLASS.SOURCE as DERIV_SOURCE, SUBCLASS.GEN_TIME as DERIV_SOURCE_GEN_TIME, ").
		append("SPO_FRESH.SOURCE, SPO_FRESH.GEN_TIME ").
		append("from SPO as SPO_FRESH, SPO as SUBCLASS").
		append(" where SPO_FRESH.SOURCE=").append(sourceUrlHash).
		append(" and SPO_FRESH.GEN_TIME=").append(genTime).
		append(" and SPO_FRESH.PREDICATE=").append(Hashes.spoHash(Predicates.RDF_TYPE)).
		append(" and SPO_FRESH.OBJECT_HASH=SUBCLASS.SUBJECT").
		append(" and SUBCLASS.PREDICATE=").append(Hashes.spoHash(Predicates.RDFS_SUBCLASS_OF)). 
		append(" and SUBCLASS.LIT_OBJ='N' and SUBCLASS.ANON_OBJ='N'");
			
		int i = SQLUtil.executeUpdate(buf.toString(), getConnection());

		/* Derive parent-classes FROM freshly harvested source. */
		
		buf = new StringBuffer().
		append("insert ignore into SPO (SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, ").
		append("OBJ_DERIV_SOURCE, OBJ_DERIV_SOURCE_GEN_TIME, SOURCE, GEN_TIME) ").
		append("select distinct SPO.SUBJECT, SPO.PREDICATE, SPO_FRESH.OBJECT, SPO_FRESH.OBJECT_HASH, ").
		append("SPO.ANON_SUBJ, 'N' as ANON_OBJ, 'N' as LIT_OBJ, SPO_FRESH.OBJ_LANG, ").
		append("SPO_FRESH.SOURCE as DERIV_SOURCE, SPO_FRESH.GEN_TIME as DERIV_SOURCE_GEN_TIME, ").
		append("SPO.SOURCE, SPO.GEN_TIME ").
		append("from SPO, SPO as SPO_FRESH").
		append(" where SPO.PREDICATE=").append(Hashes.spoHash(Predicates.RDF_TYPE)).
		append(" and SPO.OBJECT_HASH=SPO_FRESH.SUBJECT").
		append(" and SPO_FRESH.SOURCE=").append(sourceUrlHash).
		append(" and SPO_FRESH.GEN_TIME=").append(genTime).
		append(" and SPO_FRESH.PREDICATE=").append(Hashes.spoHash(Predicates.RDFS_SUBCLASS_OF)). 
		append(" and SPO_FRESH.LIT_OBJ='N' and SPO_FRESH.ANON_OBJ='N'");
		
		int j = SQLUtil.executeUpdate(buf.toString(), getConnection());
		
		logger.debug(i + " parent-classes derived FOR and " + j + " parent-classes derived FROM the current harvest");
	}

	/**
	 * @throws SQLException 
	 * 
	 */
	private void extractNewHarvestSources() throws SQLException{

		logger.debug("Extracting new harvest sources");
		
		/* handle qaw sources */
		
		String cronExpr = GeneralConfig.getProperty(GeneralConfig.HARVESTER_DEDICATED_SOURCES_CRON_EXPRESSION,
				HarvestSourceDTO.DEDICATED_HARVEST_SOURCE_DEFAULT_CRON);
		
		StringBuffer buf = new StringBuffer().
		append("insert ignore into HARVEST_SOURCE (NAME, URL, TYPE, DATE_CREATED, CREATOR, SCHEDULE_CRON, SOURCE, GEN_TIME) ").
		append("select SPO_TEMP_SOURCE.OBJECT, SPO_TEMP_SOURCE.OBJECT, '").append(DedicatedHarvestSourceTypes.qawSource).
		append("', now(), '").append(CRUser.application.getUserName()).
		append("', '").append(cronExpr).append("', ").append(sourceUrlHash).append(", ").append(genTime).
		append(" from SPO_TEMP as SPO_TEMP_SOURCE, SPO_TEMP where SPO_TEMP_SOURCE.ANON_OBJ='N' and SPO_TEMP_SOURCE.LIT_OBJ='N' and ").
		append("SPO_TEMP_SOURCE.PREDICATE=").append(Hashes.spoHash(Predicates.DC_SOURCE)).
		append(" and SPO_TEMP_SOURCE.SUBJECT=SPO_TEMP.SUBJECT and SPO_TEMP.PREDICATE=").append(Hashes.spoHash(Predicates.RDF_TYPE)).
		append(" and SPO_TEMP.ANON_OBJ='N' and SPO_TEMP.LIT_OBJ='N' and SPO_TEMP.OBJECT_HASH in (").
		append(Hashes.spoHash(Subjects.QA_REPORT_CLASS)).append(", ").append(Hashes.spoHash(Subjects.QAW_RESOURCE_CLASS)).append(")");
		
		int i = SQLUtil.executeUpdate(buf.toString(), getConnection());

		/* handle delivered files */
		
		buf = new StringBuffer().
		append("insert ignore into HARVEST_SOURCE (NAME, URL, TYPE, DATE_CREATED, CREATOR, SCHEDULE_CRON, SOURCE, GEN_TIME) ").
		append("select URI, URI, '").append(DedicatedHarvestSourceTypes.deliveredFile).
		append("', now(), '").append(CRUser.application.getUserName()).
		append("', '").append(cronExpr).append("', ").append(sourceUrlHash).append(", ").append(genTime).
		append(" from RESOURCE_TEMP, SPO_TEMP where RESOURCE_TEMP.URI_HASH=SPO_TEMP.SUBJECT and ").
		append("SPO_TEMP.PREDICATE=").append(Hashes.spoHash(Predicates.RDF_TYPE)).
		append(" and SPO_TEMP.ANON_OBJ='N' and SPO_TEMP.LIT_OBJ='N' and SPO_TEMP.OBJECT_HASH in (").
		append(Hashes.spoHash(Subjects.ROD_DELIVERY_CLASS)).append(", ").append(Hashes.spoHash(Subjects.DCTYPE_DATASET_CLASS)).append(")");
		
		i = i + SQLUtil.executeUpdate(buf.toString(), getConnection());
		
		logger.debug(i + " new harvest sources extracted and inserted");
	}
	
	/**
	 * 
	 * @throws SQLException
	 */
	private void clearTemporaries() throws SQLException{
		
		logger.debug("Cleaning SPO_TEMP and RESOURCE_TEMP");
		RDFHandler.clearTemporaries(getConnection());
	}

	/**
	 * @throws SQLException 
	 * @throws SQLException 
	 * 
	 */
	private static void clearTemporaries(Connection conn) throws SQLException{
		
		SQLUtil.executeUpdate("delete from SPO_TEMP", conn);
		SQLUtil.executeUpdate("delete from RESOURCE_TEMP", conn);
	}
	
	/**
	 * 
	 * @throws SQLException
	 */
	public static void rollbackUnfinishedHarvests() throws SQLException{

		ResultSet rs = null;
		Statement stmt = null;
		Connection conn = null;
		ArrayList<UnfinishedHarvestDTO> list = new ArrayList<UnfinishedHarvestDTO>();
		try{
			conn = ConnectionUtil.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select * from UNFINISHED_HARVEST");
			while (rs!=null && rs.next()){
				list.add(UnfinishedHarvestDTO.create(rs.getLong("SOURCE"), rs.getLong("GEN_TIME")));
			}

			if (!list.isEmpty()){
				
				LogFactory.getLog(RDFHandler.class).debug("Deleting leftovers from unfinished harvests");
				
				for (Iterator<UnfinishedHarvestDTO> i = list.iterator(); i.hasNext();){
					UnfinishedHarvestDTO unfinishedHarvestDTO = i.next();
					RDFHandler.rollback(unfinishedHarvestDTO.getSource(), unfinishedHarvestDTO.getGenTime(), conn);
				}
			}
		}
		finally{
			SQLUtil.close(rs);
			SQLUtil.close(stmt);
			SQLUtil.close(conn);
		}
	}
	
	/**
	 * 
	 * @throws SQLException
	 */
	protected void rollback() throws SQLException{

		logger.debug("Doing harvest rollback");
		RDFHandler.rollback(this.sourceUrlHash, this.genTime, this.getConnection());
	}

	/**
	 * 
	 * @param rollbackScope
	 * @throws SQLException
	 */
	private static void rollback(long sourceUrlHash, long genTime, Connection conn) throws SQLException{

		// delete rows of current harvest from SPO
		StringBuffer buf = new StringBuffer("delete from SPO where (SOURCE=");
		buf.append(sourceUrlHash).append(" and GEN_TIME=").append(genTime).
		append(") or (OBJ_DERIV_SOURCE=").append(sourceUrlHash).
		append(" and OBJ_DERIV_SOURCE_GEN_TIME=").append(genTime).append(")");
		
		SQLUtil.executeUpdate(buf.toString(), conn);

		// delete rows of current harvest from RESOURCE
		buf = new StringBuffer("delete from RESOURCE where FIRSTSEEN_SOURCE=");
		buf.append(sourceUrlHash).append(" and FIRSTSEEN_TIME=").append(genTime);
		SQLUtil.executeUpdate(buf.toString(), conn);
		
		// delete new extracted harvest sources
		buf = new StringBuffer("delete from HARVEST_SOURCE where SOURCE=").append(sourceUrlHash).
		append(" and GEN_TIME=").append(genTime);
		
		// 
		RDFHandler.deleteUnfinishedHarvestFlag(sourceUrlHash, genTime, conn);

		// delete all rows from SPO_TEMP and RESOURCE_TEMP
		try{
			RDFHandler.clearTemporaries(conn);
		}
		catch (Exception e){}
	}

    /*
     * (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
	public void error(SAXParseException e) throws SAXException {
		saxErrors.add(e);
		logger.error("SAX error encountered: " + e.toString(), e);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	public void fatalError(SAXParseException e) throws SAXException {
		throw new LoadException(e.toString(), e);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	public void warning(SAXParseException e) throws SAXException {
		saxWarnings.add(e);
		logger.warn("SAX warning encountered: " + e.toString(), e);
	}

	/**
	 * @return the saxErrors
	 */
	public List<SAXParseException> getSaxErrors() {
		return saxErrors;
	}

	/**
	 * @return the saxWarnings
	 */
	public List<SAXParseException> getSaxWarnings() {
		return saxWarnings;
	}

	/**
	 * @return the storedTriplesCount
	 */
	public int getStoredTriplesCount() {
		return storedTriplesCount;
	}

	/**
	 * @param clearPreviousContent the clearPreviousContent to set
	 */
	public void setClearPreviousContent(boolean clearPreviousContent) {
		this.clearPreviousContent = clearPreviousContent;
	}

	/**
	 * @return the distinctSubjectsCount
	 */
	public int getDistinctSubjectsCount() {
		return distinctSubjectsCount;
	}
}