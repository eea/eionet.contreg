/*
* The contents of this file are subject to the Mozilla Public
* 
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
* Agency. Portions created by Tieto Eesti are Copyright
* (C) European Environment Agency. All Rights Reserved.
* 
* Contributor(s):
* Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.harvest.persist.postgresql;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.common.LabelPredicates;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.persist.IDerivationEngine;
import eionet.cr.harvest.util.HarvestLog;
import eionet.cr.util.Hashes;
import eionet.cr.util.sql.SQLUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PostgreSQLDerivationEngine implements IDerivationEngine{

	/** */
	private Log logger;

	/** */
	private long sourceUrlHash;
	private long genTime;
	private Connection connection;
	
	/**
	 * 
	 * @param sourceUrlHash
	 * @param genTime
	 * @param connection
	 */
	public PostgreSQLDerivationEngine(String sourceUrlString, long sourceUrlHash, long genTime, Connection connection) {
		
		this.sourceUrlHash = sourceUrlHash;
		this.genTime = genTime;
		this.connection = connection;
		
		logger = new HarvestLog(sourceUrlString, genTime, LogFactory.getLog(this.getClass()));
	}
	
	/* (non-Javadoc)
	 * @see eionet.cr.harvest.persist.IDerivationEngine#deriveLabels()
	 */
	public void deriveLabels() throws SQLException {

		logger.debug("Deriving labels");
		
		// Derive labels FOR freshly harvested source
		
		StringBuffer buf = new StringBuffer().
		append("insert into SPO ").
		append("(SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, OBJECT_DOUBLE, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, ").
		append("OBJ_DERIV_SOURCE, OBJ_DERIV_SOURCE_GEN_TIME, OBJ_SOURCE_OBJECT, SOURCE, GEN_TIME) ").
		append("select distinct SPO_FRESH.SUBJECT, SPO_FRESH.PREDICATE, SPO_LABEL.OBJECT, SPO_LABEL.OBJECT_HASH, ").
		append("SPO_LABEL.OBJECT_DOUBLE, SPO_FRESH.ANON_SUBJ, ").
		append("cast('N' as ynboolean) as ANON_OBJ, cast('Y' as ynboolean) as LIT_OBJ, SPO_LABEL.OBJ_LANG, ").
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
		
		int i = SQLUtil.executeUpdate(buf.toString(), connection);

		// Derive labels FROM freshly harvested source
		
		buf = new StringBuffer().
		append("insert into SPO (").
		append(" SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, OBJECT_DOUBLE, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, ").
		append("OBJ_DERIV_SOURCE, OBJ_DERIV_SOURCE_GEN_TIME, OBJ_SOURCE_OBJECT, SOURCE, GEN_TIME) ").
		append("select distinct SPO_ALL.SUBJECT, SPO_ALL.PREDICATE, SPO_FRESH.OBJECT, SPO_FRESH.OBJECT_HASH, ").
		append("SPO_FRESH.OBJECT_DOUBLE, SPO_ALL.ANON_SUBJ, ").
		append("cast('N' as ynboolean) as ANON_OBJ, cast('Y' as ynboolean) as LIT_OBJ, SPO_FRESH.OBJ_LANG, ").
		append("SPO_FRESH.SOURCE as OBJ_DERIV_SOURCE, SPO_FRESH.GEN_TIME as OBJ_DERIV_SOURCE_GEN_TIME, ").
		append("SPO_ALL.OBJECT_HASH as OBJ_SOURCE_OBJECT, SPO_ALL.SOURCE, SPO_ALL.GEN_TIME ").
		append("from SPO as SPO_ALL, SPO as SPO_FRESH ").
		append("where SPO_ALL.LIT_OBJ='N' and SPO_ALL.OBJECT_HASH=SPO_FRESH.SUBJECT").
		append(" and SPO_FRESH.LIT_OBJ='Y' and SPO_FRESH.ANON_OBJ='N'").
		append(" and SPO_FRESH.PREDICATE in (").append(LabelPredicates.getCommaSeparatedHashes()).append(")").
		append(" and SPO_FRESH.SOURCE=").append(sourceUrlHash).
		append(" and SPO_FRESH.GEN_TIME=").append(genTime).
		append(" and SPO_ALL.SOURCE<>").append(sourceUrlHash).
		append(" and SPO_ALL.GEN_TIME<>").append(genTime);
		
		int j = SQLUtil.executeUpdate(buf.toString(), connection);
		
		logger.debug(i + " labels derived FOR and " + j + " labels derived FROM the current harvest");
	}

	/* (non-Javadoc)
	 * @see eionet.cr.harvest.persist.IDerivationEngine#deriveParentClasses()
	 */
	public void deriveParentClasses() throws SQLException {
		
		logger.debug("Deriving parent-classes");
		
		// Derive parent-classes FOR freshly harvested source
		
		StringBuffer buf = new StringBuffer().
		append("insert into SPO ").
		append("(SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, OBJECT_DOUBLE, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, ").
		append("OBJ_DERIV_SOURCE, OBJ_DERIV_SOURCE_GEN_TIME, SOURCE, GEN_TIME) ").
		append("select distinct SPO_FRESH.SUBJECT, SPO_FRESH.PREDICATE, SUBCLASS.OBJECT, ").
		append("SUBCLASS.OBJECT_HASH, SUBCLASS.OBJECT_DOUBLE, SPO_FRESH.ANON_SUBJ, cast('N' as ynboolean) as ANON_OBJ, cast('N' as ynboolean) as LIT_OBJ, SUBCLASS.OBJ_LANG, ").
		append("SUBCLASS.SOURCE as DERIV_SOURCE, SUBCLASS.GEN_TIME as DERIV_SOURCE_GEN_TIME, ").
		append("SPO_FRESH.SOURCE, SPO_FRESH.GEN_TIME ").
		append("from SPO as SPO_FRESH, SPO as SUBCLASS").
		append(" where SPO_FRESH.SOURCE=").append(sourceUrlHash).
		append(" and SPO_FRESH.GEN_TIME=").append(genTime).
		append(" and SPO_FRESH.PREDICATE=").append(Hashes.spoHash(Predicates.RDF_TYPE)).
		append(" and SPO_FRESH.OBJECT_HASH=SUBCLASS.SUBJECT").
		append(" and SUBCLASS.PREDICATE=").append(Hashes.spoHash(Predicates.RDFS_SUBCLASS_OF)). 
		append(" and SUBCLASS.LIT_OBJ='N' and SUBCLASS.ANON_OBJ='N'");
			
		int i = SQLUtil.executeUpdate(buf.toString(), connection);

		// Derive parent-classes FROM freshly harvested source
		
		buf = new StringBuffer().
		append("insert into SPO (SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, OBJECT_DOUBLE, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, ").
		append("OBJ_DERIV_SOURCE, OBJ_DERIV_SOURCE_GEN_TIME, SOURCE, GEN_TIME) ").
		append("select distinct SPO.SUBJECT, SPO.PREDICATE, SPO_FRESH.OBJECT, SPO_FRESH.OBJECT_HASH, SPO_FRESH.OBJECT_DOUBLE, ").
		append("SPO.ANON_SUBJ, cast('N' as ynboolean) as ANON_OBJ, cast('N' as ynboolean) as LIT_OBJ, SPO_FRESH.OBJ_LANG, ").
		append("SPO_FRESH.SOURCE as DERIV_SOURCE, SPO_FRESH.GEN_TIME as DERIV_SOURCE_GEN_TIME, ").
		append("SPO.SOURCE, SPO.GEN_TIME ").
		append("from SPO, SPO as SPO_FRESH").
		append(" where SPO.PREDICATE=").append(Hashes.spoHash(Predicates.RDF_TYPE)).
		append(" and SPO.OBJECT_HASH=SPO_FRESH.SUBJECT").
		append(" and SPO_FRESH.PREDICATE=").append(Hashes.spoHash(Predicates.RDFS_SUBCLASS_OF)). 
		append(" and SPO_FRESH.LIT_OBJ='N' and SPO_FRESH.ANON_OBJ='N'").
		append(" and SPO_FRESH.SOURCE=").append(sourceUrlHash).
		append(" and SPO_FRESH.GEN_TIME=").append(genTime).
		append(" and SPO.SOURCE<>").append(sourceUrlHash).
		append(" and SPO.GEN_TIME<>").append(genTime);
		
		int j = SQLUtil.executeUpdate(buf.toString(), connection);
		
		logger.debug(i + " parent-classes derived FOR and " + j + " parent-classes derived FROM the current harvest");

	}

	/* (non-Javadoc)
	 * @see eionet.cr.harvest.persist.IDerivationEngine#deriveParentProperties()
	 */
	public void deriveParentProperties() throws SQLException {
		
		logger.debug("Deriving parent-properties");
		
		// Derive parent-properties FOR freshly harvested source
		
		StringBuffer buf = new StringBuffer().
		append("insert into SPO ").
		append("(SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, OBJECT_DOUBLE, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, ").
		append("OBJ_DERIV_SOURCE, OBJ_DERIV_SOURCE_GEN_TIME, SOURCE, GEN_TIME) ").
		append("select distinct SPO_FRESH.SUBJECT, SUBPROP.OBJECT_HASH as PARENT_PREDICATE, ").
		append("SPO_FRESH.OBJECT, SPO_FRESH.OBJECT_HASH, SPO_FRESH.OBJECT_DOUBLE, SPO_FRESH.ANON_SUBJ, SPO_FRESH.ANON_OBJ, ").
		append("SPO_FRESH.LIT_OBJ, SPO_FRESH.OBJ_LANG, ").
		append("SUBPROP.SOURCE as DERIV_SOURCE, SUBPROP.GEN_TIME as DERIV_SOURCE_GEN_TIME, SPO_FRESH.SOURCE, SPO_FRESH.GEN_TIME ").
		append("from SPO as SPO_FRESH, SPO as SUBPROP ").
		append("where SPO_FRESH.SOURCE=").append(sourceUrlHash).
		append(" and SPO_FRESH.GEN_TIME=").append(genTime).
		append(" and SPO_FRESH.PREDICATE=SUBPROP.SUBJECT").
		append(" and SUBPROP.PREDICATE=").append(Hashes.spoHash(Predicates.RDFS_SUBPROPERTY_OF)). 
		append(" and SUBPROP.LIT_OBJ='N' and SUBPROP.ANON_OBJ='N'");
		
		int i = SQLUtil.executeUpdate(buf.toString(), connection);

		// Derive parent-properties FROM freshly harvested source
		
		buf = new StringBuffer().
		append("insert into SPO (SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, OBJECT_DOUBLE, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, ").
		append("OBJ_DERIV_SOURCE, OBJ_DERIV_SOURCE_GEN_TIME, SOURCE, GEN_TIME) ").		
		append("select distinct SPO.SUBJECT, SPO_FRESH.OBJECT_HASH as PARENT_PRED, SPO.OBJECT, SPO.OBJECT_HASH, SPO.OBJECT_DOUBLE, ").
		append("SPO.ANON_SUBJ, SPO.ANON_OBJ, SPO.LIT_OBJ, SPO.OBJ_LANG, ").
		append("SPO_FRESH.SOURCE as DERIV_SOURCE, SPO_FRESH.GEN_TIME as DERIV_SOURCE_GEN_TIME, SPO.SOURCE, SPO.GEN_TIME ").
		append("from SPO, SPO as SPO_FRESH").
		append(" where SPO_FRESH.PREDICATE=").append(Hashes.spoHash(Predicates.RDFS_SUBPROPERTY_OF)). 
		append(" and SPO_FRESH.LIT_OBJ='N' and SPO_FRESH.ANON_OBJ='N' and SPO_FRESH.SUBJECT=SPO.PREDICATE").
		append(" and SPO_FRESH.SOURCE=").append(sourceUrlHash).
		append(" and SPO_FRESH.GEN_TIME=").append(genTime);
//		append(" and SPO.SOURCE<>").append(sourceUrlHash).
//		append(" and SPO.GEN_TIME<>").append(genTime);
		
		int j = SQLUtil.executeUpdate(buf.toString(), connection);
		
		logger.debug(i + " parent-properties derived FOR and " + j + " parent-properties derived FROM the current harvest");
	}

	/* (non-Javadoc)
	 * @see eionet.cr.harvest.persist.IDerivationEngine#extractNewHarvestSources()
	 */
	public void extractNewHarvestSources() throws SQLException {
		
		logger.debug("Extracting tracked files");
		
		// harvest interval for tracked files		
		Integer interval = Integer.valueOf(
				GeneralConfig.getProperty(
						GeneralConfig.HARVESTER_REFERRALS_INTERVAL,
						String.valueOf(HarvestSourceDTO.DEFAULT_REFERRALS_INTERVAL)));

		StringBuffer buf = new StringBuffer().
		append("insert into HARVEST_SOURCE").
		append(" (URL, URL_HASH, TRACKED_FILE, TIME_CREATED, INTERVAL_MINUTES, SOURCE, GEN_TIME) ").		
		append("select (case when position('#' in URI)>1 then substring(URI from 1 for position('#' in URI)-1) else URI END),").
		append(" URI_HASH, cast('Y' as ynboolean), now(),").append(interval).append(",").
		append(sourceUrlHash).append(", ").append(genTime).
		append(" from SPO,RESOURCE where SPO.SOURCE=").append(sourceUrlHash).
		append(" and SPO.GEN_TIME=").append(genTime).append(" and SPO.PREDICATE=").
		append(Hashes.spoHash(Predicates.RDF_TYPE)).append(" and SPO.ANON_OBJ='N' and SPO.OBJECT_HASH=").
		append(Hashes.spoHash(Subjects.CR_FILE)).append(" and SPO.SUBJECT=RESOURCE.URI_HASH");
		
		int i = SQLUtil.executeUpdate(buf.toString(), connection);		
		logger.debug(i + " tracked files extracted and inserted as NEW harvest sources");

	}

}
