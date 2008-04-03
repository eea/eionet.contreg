package eionet.cr.api.xmlrpc;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.index.EncodingSchemes;
import eionet.cr.util.CRException;
import eionet.cr.util.Identifiers;
import eionet.cr.util.Util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SearcherXmlRpcImpl{
	
	/** */
	private static Log logger = LogFactory.getLog(SearcherXmlRpcImpl.class);

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.api.Searcher#getResourcesSinceTimestamp(java.util.Date)
	 */
	public List getResourcesSinceTimestamp(Date timestamp) throws CRException {
		
		List result = null;
		if (timestamp!=null){
			// given timestamp must be less than current time (in seconds)
			long curTimeSeconds = Util.currentTimeSeconds();
			long givenTimeSeconds = Util.getSeconds(timestamp.getTime());
			if (givenTimeSeconds < curTimeSeconds){
				
				String s = Identifiers.FIRST_SEEN_TIMESTAMP.replaceAll(":", "\\:");
				
				StringBuffer qryBuf = new StringBuffer(Util.escapeForLuceneQuery(Identifiers.FIRST_SEEN_TIMESTAMP));
				qryBuf.append(":[").append(givenTimeSeconds).append(" TO ").append(curTimeSeconds).append("]");
				try{
					result = eionet.cr.index.Searcher.search(qryBuf.toString());
				}
				catch (Exception e){
					logger.error(e.toString(), e);
					throw new CRException(e.toString(), e);
				}				
			}
		}

		if (result==null)
			return new ArrayList<java.util.Map<String,String[]>>();
		else
			return result.size()>300 ? result.subList(0, 300) : result;
	}
	
	/**
	 * 
	 * @param criteria
	 * @return
	 * @throws CRException 
	 */
	public List simpleAndSearch(Map<String,String> criteria) throws CRException{
		
		List result = null;
		if (criteria!=null && !criteria.isEmpty()){
			StringBuffer qryBuf = new StringBuffer();
			for (Iterator<String> iter = criteria.keySet().iterator(); iter.hasNext();){
				String key = iter.next();
				if (key!=null && key.length()>0){
					String value = criteria.get(key);
					if (value!=null && value.length()>0){
						if (Util.hasWhiteSpace(value))
							value = "\"" + value + "\"";
						if (qryBuf.length()>0)
							qryBuf.append(" AND ");
						qryBuf.append(Util.escapeForLuceneQuery(key)).append(":").append(Util.escapeForLuceneQuery(value));
						
						try{
							result = eionet.cr.index.Searcher.search(qryBuf.toString());
						}
						catch (Exception e){
							logger.error(e.toString(), e);
							throw new CRException(e.toString(), e);
						}
					}
				}
			}
		}
		
		if (result==null)
			return new ArrayList<java.util.Map<String,String[]>>();
		else
			return result.size()>300 ? result.subList(0, 300) : result;
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		
		SearcherXmlRpcImpl searcher = new SearcherXmlRpcImpl();
		
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			System.out.println(searcher.getResourcesSinceTimestamp(formatter.parse("2007-01-01 10:30:00")));
		}
		catch (CRException e) {
			e.printStackTrace();
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
