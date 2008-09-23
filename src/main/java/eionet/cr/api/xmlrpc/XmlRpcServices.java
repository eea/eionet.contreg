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
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.Harvest;
import eionet.cr.harvest.HarvestDAOWriter;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.PushHarvest;
import eionet.cr.index.EncodingSchemes;
import eionet.cr.search.Searcher;
import eionet.cr.search.util.EntriesCollector;
import eionet.cr.search.util.HitsCollector;
import eionet.cr.search.util.ResourceDTOCollector;
import eionet.cr.util.StringUtils;
import eionet.cr.util.Util;
import eionet.cr.web.security.CRUser;
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
			
			ResourceDTOCollector collector = new ResourceDTOCollector();
			Searcher.customSearch(criteria, false, collector);
			List<ResourceDTO> list = collector.getResultList();
			
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
				throw new CRException( "Missing source uri");
			else{
				PushHarvest pushHarvest = new PushHarvest(content, sourceUri);
				HarvestSourceDTO sourceDTO = new HarvestSourceDTO();
				sourceDTO.setUrl(sourceUri);
				sourceDTO.setName(sourceUri);
				sourceDTO.setType("data");
				try {
					Integer sourceId = DAOFactory.getDAOFactory().getHarvestSourceDAO().addSourceIgnoreDuplicate(
							sourceDTO, CRUser.application.getUserName());
					if (sourceId!=null && sourceId.intValue()>0){
						pushHarvest.setDaoWriter(
								new HarvestDAOWriter(sourceId.intValue(), Harvest.TYPE_PUSH, CRUser.application.getUserName()));
					}
					
					pushHarvest.execute();
					result = pushHarvest.getCountTotalResources();
				}
				catch (Exception e) {
					throw new CRException(e.toString(), e);
				}
			}
		}
		
		return new Integer(result);
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.api.xmlrpc.Services#getEntries(java.util.Hashtable)
	 * 
	 * This method implements what getEntries did in the old Content Registry.
	 * It is called by ROD, though it can be used by any other application as well.
	 * 
	 * The purpose is to return all metadata of all resources that match the given
	 * criteria. The criteria is given as a <code>java.util.Hashtable</code>, where keys represent metadata
	 * attribute names and values represent their values. Data type of both keys and
	 * values is <code>java.lang.String</code>.
	 * 
	 * The method returns a <code>java.util.Vector</code> of type <code>java.util.Hashtable</code>. Every such hashtable represents one
	 * resource that contains exactly 1 key that is a String that represents the resource's URI. The value is
	 * another <code>java.lang.Hashtable</code> where the data type of keys is <code>java.lang.String</code> and the data type of values
	 * is <code>java.util.Vector</code>. They keys represent URIs of the resource's attributes and the value-vectors represent values
	 * of attributes. These values are of type <code>java.lang.String</code>.
	 * 
	 */
	public Vector getEntries(Hashtable attributes) throws CRException {

		EntriesCollector collector = new EntriesCollector();
		Searcher.customSearch((Map<String,String>)attributes, true, collector);
		Vector result = collector.getResultVector();
		if (result==null)
			result = new Vector();
		
		return result;
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
