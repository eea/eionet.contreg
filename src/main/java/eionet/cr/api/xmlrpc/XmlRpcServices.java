package eionet.cr.api.xmlrpc;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.common.CRException;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.scheduled.HarvestQueue;
import eionet.cr.search.LuceneBasedSearcher;
import eionet.cr.search.util.EntriesCollector;
import eionet.cr.search.util.SubjectDTOCollector;
import eionet.cr.util.Util;
import eionet.cr.web.util.JstlFunctions;
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
				
				String s = Predicates.FIRST_SEEN_TIMESTAMP.replaceAll(":", "\\:");
				
				StringBuffer qryBuf = new StringBuffer(Util.luceneEscape(Predicates.FIRST_SEEN_TIMESTAMP));
				qryBuf.append(":[").append(givenTimeSeconds).append(" TO ").append(curTimeSeconds).append("]");
				try{
					result = eionet.cr.search.LuceneBasedSearcher.luceneQuery(qryBuf.toString());
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
		
		if (!criteria.containsKey(Predicates.RDF_TYPE))
			criteria.put(Predicates.RDF_TYPE, Subjects.ROD_DELIVERY_CLASS);
		
		List<DataflowResultDto> result = new ArrayList<DataflowResultDto>();
		try{
			
			SubjectDTOCollector collector = new SubjectDTOCollector();
			LuceneBasedSearcher.customSearch(criteria, false, collector);
			List<SubjectDTO> list = collector.getResultList();
			
			if (list!=null){
				for (int i=0; i<list.size(); i++){
					
					SubjectDTO subjectDTO = list.get(i);
					DataflowResultDto resultDTO = new DataflowResultDto();
					
					resultDTO.setResource(subjectDTO.getUri());
					resultDTO.setTitle(subjectDTO.getTitle());
					
					resultDTO.setDate(subjectDTO.getObjectValue(Predicates.DC_DATE));
					resultDTO.setDataflow(getDistinctLiteralObjects(subjectDTO, Predicates.ROD_OBLIGATION_PROPERTY));
					resultDTO.setLocality(getDistinctLiteralObjects(subjectDTO, Predicates.ROD_LOCALITY_PROPERTY));
					resultDTO.setType(getDistinctLiteralObjects(subjectDTO, Predicates.RDF_TYPE));
					
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
							result = eionet.cr.search.LuceneBasedSearcher.luceneQuery(qryBuf.toString());
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
	public String pushContent(String content, String sourceUri) throws CRException {
		
		if (content!=null && content.trim().length()>0){
			if (sourceUri==null || sourceUri.trim().length()==0)
				throw new CRException( "Missing source uri");
			else
				HarvestQueue.addPushHarvest(content, sourceUri, HarvestQueue.PRIORITY_NORMAL);
		}
		
		return OK_RETURN_STRING;
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
		LuceneBasedSearcher.customSearch((Map<String,String>)attributes, true, collector);
		Vector result = collector.getResultVector();
		if (result==null)
			result = new Vector();
		
		return result;
	}

	/**
	 * 
	 * @param subjectDTO
	 * @param predicateUri
	 * @return
	 */
	private static String[] getDistinctLiteralObjects(SubjectDTO subjectDTO, String predicateUri){
		
		HashSet<String> result = new HashSet<String>();
		
		Collection<ObjectDTO> objects = subjectDTO.getObjects(predicateUri, ObjectDTO.Type.LITERAL);
		if (objects!=null && !objects.isEmpty()){
			for (Iterator<ObjectDTO> iter = objects.iterator(); iter.hasNext();){
				result.add(iter.next().getValue());
			}
		}
		
		return result.toArray(new String[result.size()]);
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
