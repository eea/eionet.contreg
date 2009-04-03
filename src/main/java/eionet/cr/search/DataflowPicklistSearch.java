package eionet.cr.search;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.search.util.UriLabelPair;
import eionet.cr.util.Hashes;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.ResultSetBaseReader;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SQLValue;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class DataflowPicklistSearch extends ResultSetBaseReader{

	/** */
	private static final String sql = new StringBuffer().
		append("select distinct ").
			append("INSTRUMENT_TITLE.OBJECT as INSTRUMENT_TITLE, ").
			append("OBLIGATION_TITLE.OBJECT as OBLIGATION_TITLE, ").
			append("OBLIGATION_URI.URI as OBLIGATION_URI ").
		append("from ").
			append("SPO as OBLIGATION_TITLE ").
			append("left join RESOURCE as OBLIGATION_URI on OBLIGATION_TITLE.SUBJECT=OBLIGATION_URI.URI_HASH ").
			append("left join SPO as OBLIGATION_INSTR on OBLIGATION_TITLE.SUBJECT=OBLIGATION_INSTR.SUBJECT ").
			append("left join SPO as INSTRUMENT_TITLE on OBLIGATION_INSTR.OBJECT_HASH=INSTRUMENT_TITLE.SUBJECT ").			
		append("where ").
			append("OBLIGATION_TITLE.PREDICATE=").append(Hashes.spoHash(Predicates.DC_TITLE)).
			append(" and OBLIGATION_TITLE.LIT_OBJ='Y' and OBLIGATION_INSTR.PREDICATE=").
			append(Hashes.spoHash(Predicates.ROD_INSTRUMENT_PROPERTY)).
			append(" and OBLIGATION_INSTR.LIT_OBJ='N' and OBLIGATION_INSTR.ANON_OBJ='N'").
			append(" and INSTRUMENT_TITLE.PREDICATE=").append(Hashes.spoHash(Predicates.DCTERMS_ALTERNATIVE)).
			append(" and INSTRUMENT_TITLE.LIT_OBJ='Y' ").
		append("order by ").
			append("INSTRUMENT_TITLE.OBJECT, OBLIGATION_TITLE.OBJECT ").toString();

	/** */
	private LinkedHashMap<String,ArrayList<UriLabelPair>> resultMap = new LinkedHashMap<String,ArrayList<UriLabelPair>>();
	
	/** */
	private String currentInstrument = null;
	private ArrayList<UriLabelPair> currentObligations = null;

	/**
	 * @throws SearchException 
	 * 
	 */
	public void execute() throws SearchException{
		
		Connection conn = null;
		try{
			conn = ConnectionUtil.getConnection();
			SQLUtil.executeQuery(sql, this, conn);
		}
		catch (SQLException e){
			throw new SearchException(e.toString(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException {

		String instrument = rs.getString("INSTRUMENT_TITLE");
		if (currentInstrument==null || !currentInstrument.equals(instrument)){
			currentObligations = new ArrayList<UriLabelPair>();
			currentInstrument = instrument;
			resultMap.put(currentInstrument, currentObligations);
		}
		
		currentObligations.add(UriLabelPair.create(rs.getString("OBLIGATION_URI"), rs.getString("OBLIGATION_TITLE")));
	}

	/**
	 * @return the resultMap
	 */
	public HashMap<String,ArrayList<UriLabelPair>> getResultMap(){
		
		return resultMap;
	}
}
