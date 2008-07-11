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

import eionet.cr.common.CRException;
import eionet.cr.common.Identifiers;
import eionet.cr.common.ResourceDTO;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.PushHarvest;
import eionet.cr.index.EncodingSchemes;
import eionet.cr.search.Searcher;
import eionet.cr.util.StringUtils;
import eionet.cr.util.Util;
import eionet.qawcommons.DataflowResultDto;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class XmlRpcServices implements Services{

	
	/** */
	private static Log logger = LogFactory.getLog(XmlRpcServices.class);

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.api.xmlrpc.Services#getResourcesSinceTimestamp(java.util.Date)
	 */
	public List getResourcesSinceTimestamp(Date timestamp) throws CRException {
		
		List result = null;
		if (timestamp!=null){
			// given timestamp must be less than current time (in seconds)
			long curTimeSeconds = Util.currentTimeSeconds();
			long givenTimeSeconds = Util.getSeconds(timestamp.getTime());
			if (givenTimeSeconds < curTimeSeconds){
				
				String s = Identifiers.FIRST_SEEN_TIMESTAMP.replaceAll(":", "\\:");
				
				StringBuffer qryBuf = new StringBuffer(Util.luceneEscape(Identifiers.FIRST_SEEN_TIMESTAMP));
				qryBuf.append(":[").append(givenTimeSeconds).append(" TO ").append(curTimeSeconds).append("]");
				try{
					result = eionet.cr.search.Searcher.luceneQuery(qryBuf.toString());
				}
				catch (Exception e){
					logger.error(e.toString(), e);
					throw new CRException(e.toString(), e);
				}				
			}
		}

		return result==null ? new ArrayList<java.util.Map<String,String[]>>() : result;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.api.xmlrpc.Services#dataflowSearch(java.util.Map)
	 */
	public List dataflowSearch(Map<String,String> criteria) throws CRException{
		
		if (criteria==null)
			criteria = new HashMap<String,String>();
		
		if (!criteria.containsKey(Identifiers.RDF_TYPE))
			criteria.put(Identifiers.RDF_TYPE, Identifiers.ROD_DELIVERY_CLASS);
		
		List<DataflowResultDto> result = new ArrayList<DataflowResultDto>();
		try{
			List<ResourceDTO> list = Searcher.customSearch(criteria, false);
			if (list!=null){
				for (int i=0; i<list.size(); i++){
					
					ResourceDTO resourceDTO = list.get(i);
					
					DataflowResultDto resultDTO = new DataflowResultDto();
					resultDTO.setTitle(resourceDTO.getTitle());
					resultDTO.setDataflow(
							StringUtils.toArray(resourceDTO.getDistinctLiteralValues(Identifiers.ROD_OBLIGATION_PROPERTY)));
					resultDTO.setLocality(
							StringUtils.toArray(resourceDTO.getDistinctLiteralValues(Identifiers.ROD_LOCALITY_PROPERTY)));
					resultDTO.setType(
							StringUtils.toArray(resourceDTO.getDistinctLiteralValues(Identifiers.RDF_TYPE)));
					resultDTO.setResource(resourceDTO.getUri());
					resultDTO.setDate(resourceDTO.getValue(Identifiers.DC_DATE));
					
					result.add(resultDTO);
				}
			}
		}
		catch (Throwable t){
			t.printStackTrace();
			throw new CRException(t.toString(), t);
		}
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.api.xmlrpc.Services#simpleAndSearch(java.util.Map)
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
						qryBuf.append(Util.luceneEscape(key)).append(":").append(Util.luceneEscape(value));
						
						try{
							result = eionet.cr.search.Searcher.luceneQuery(qryBuf.toString());
						}
						catch (Exception e){
							logger.error(e.toString(), e);
							throw new CRException(e.toString(), e);
						}
					}
				}
			}
		}
		
		return result==null ? new ArrayList<java.util.Map<String,String[]>>() : result;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.api.xmlrpc.Services#pushContent(java.lang.String)
	 */
	public Integer pushContent(String content, String sourceUri) throws CRException {
		
		int result = 0;
		if (content!=null && content.trim().length()>0){
			if (sourceUri==null || sourceUri.trim().length()==0)
				throw new CRException("Missing sourceUri");
			else{
				PushHarvest pushHarvest = new PushHarvest(content, sourceUri);
				try {
					pushHarvest.execute();
					result = pushHarvest.getCountTotalResources();
				}
				catch (HarvestException e) {
					throw new CRException(e.toString(), e);
				}
			}
		}
		
		return new Integer(result);
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		
		XmlRpcServices searcher = new XmlRpcServices();
		
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
