package eionet.cr.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import eionet.cr.common.Predicates;
import eionet.cr.util.URLUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubjectDTO{
	
	/** */
	private String uri;
	private boolean anonymous;
	private Map<String,Collection<ObjectDTO>> predicates;
	
	/**
	 * 
	 * @param uri
	 * @param anonymous
	 */
	public SubjectDTO(String uri, boolean anonymous){
		
		this.uri = uri;
		this.anonymous = anonymous;
		predicates = new HashMap<String,Collection<ObjectDTO>>();
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<String,Collection<ObjectDTO>> getPredicates(){
		return predicates;
	}
	
	/**
	 * 
	 * @param predicateUri
	 * @return
	 */
	public Collection<ObjectDTO> getObjects(String predicateUri){
		
		Collection<ObjectDTO> result = predicates.get(predicateUri);
		if (result==null || result.isEmpty()){
			if (predicateUri!=null){
				if (predicateUri.equals(Predicates.RDFS_LABEL)) // FIXME right now a workaround if no label available
					result = predicates.get(Predicates.DC_TITLE);
			}
		}
		return result;
	}

	/**
	 * 
	 * @param predicateUri
	 * @param objectType
	 * @return
	 */
	public Collection<ObjectDTO> getObjects(String predicateUri, ObjectDTO.Type objectType){
		
		ArrayList<ObjectDTO> result = new ArrayList<ObjectDTO>();
		
		Collection<ObjectDTO> coll = getObjects(predicateUri);
		if (coll!=null && !coll.isEmpty()){
			for (Iterator<ObjectDTO> iter=coll.iterator(); iter.hasNext();){
				ObjectDTO objectDTO = iter.next();
				if (objectType.equals(ObjectDTO.Type.LITERAL) && objectDTO.isLiteral())
					result.add(objectDTO);
				else if (objectType.equals(ObjectDTO.Type.RESOURCE) && !objectDTO.isLiteral())
					result.add(objectDTO);;
			}
		}
		
		return result;
	}

	/**
	 * 
	 * @param predicate
	 * @return
	 */
	public ObjectDTO getObject(String predicateUri){
		Collection<ObjectDTO> objects = getObjects(predicateUri);
		return objects==null || objects.isEmpty() ? null : objects.iterator().next();
	}

	/**
	 * 
	 * @param predicateUri
	 * @param objectType
	 * @return
	 */
	public ObjectDTO getObject(String predicateUri, ObjectDTO.Type objectType){
		Collection<ObjectDTO> objects = getObjects(predicateUri, objectType);
		return objects==null || objects.isEmpty() ? null : objects.iterator().next();
	}

	/**
	 * 
	 * @param predicateUri
	 * @return
	 */
	public String getObjectValue(String predicateUri){
		ObjectDTO objectDTO = getObject(predicateUri);
		return objectDTO==null ? null : objectDTO.getValue();
	}

	/**
	 * 
	 * @param predicateUri
	 * @return
	 */
	public String getObjectValue(String predicateUri, ObjectDTO.Type objectType){
		ObjectDTO objectDTO = getObject(predicateUri, objectType);
		return objectDTO==null ? null : objectDTO.getValue();
	}

	/**
	 * 
	 * @return
	 */
	public int getPredicateCount(){
		return predicates.size();
	}

	/**
	 * 
	 * @return
	 */
	public String getUri(){
		return uri;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isAnonymous(){
		return anonymous;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other){
		
		if (this==other)
			return true;
		
		if (!(other instanceof SubjectDTO))
			return false;
		
		
		String otherUri = ((SubjectDTO)other).getUri();
		return getUri()==null ? otherUri==null : getUri().equals(otherUri);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode(){

		return getUri()==null ? 0 : getUri().hashCode();
	}

	/**
	 * 
	 * @return
	 */
	public String getUrl(){
		
		if (uri!=null && URLUtil.isURL(uri))
			return uri;
		else{
			ObjectDTO o = getObject(Predicates.DC_IDENTIFIER);
			return o==null || !URLUtil.isURL(o.getValue()) ? null : o.getValue();
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public String getTitle(){
		return getObjectValue(Predicates.RDFS_LABEL);
	}
}
