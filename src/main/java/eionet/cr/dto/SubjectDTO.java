package eionet.cr.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import eionet.cr.common.Md5Map;
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
		return predicates.get(predicateUri);
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
	 * @param key
	 * @return
	 */
	public String[] getDistinctLiteralObjects(String predicateUri){
		
		ArrayList<String> result = new ArrayList<String>();		
		
		Collection<ObjectDTO> objects = getObjects(predicateUri);
		if (objects!=null && !objects.isEmpty()){
		
			LinkedHashSet<ObjectDTO> distinctObjects = new LinkedHashSet<ObjectDTO>(objects);
			for (Iterator<ObjectDTO> iter = distinctObjects.iterator(); iter.hasNext();){			
				ObjectDTO objectDTO = iter.next();
				if (objectDTO.isLiteral())
					result.add(objectDTO.toString());
			}
		}
		
		return result.toArray(new String[result.size()]);
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
		ObjectDTO o = getObject(Predicates.RDFS_LABEL);
		return o==null ? null : o.getValue();
	}
}
