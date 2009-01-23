package eionet.cr.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import eionet.cr.util.URLUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubjectDTO extends HashMap<PredicateDTO,Collection<ObjectDTO>>{
	
	/**
	 */
	public static interface SpecialKeys{
		
		public static final String RESOURCE_URI = "resourceUri";
		public static final String RESOURCE_URL = "resourceUrl";
		public static final String RESOURCE_TITLE = "resourceTitle";
	}

	
	/** */
	private String uri;
	private boolean anonymous;
	
	/**
	 * 
	 * @param uri
	 * @param anonymous
	 */
	public SubjectDTO(String uri, boolean anonymous){
		super();
		this.uri = uri;
		this.anonymous = anonymous;
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public Collection<ObjectDTO> get(String s){
		return super.get(new PredicateDTO(s));
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
	public List<String> getDistinctLiteralValues(String key){
		
		Collection<ObjectDTO> coll = get(key);
		if (coll==null)
			return null;
		else
			return asDistinctLiteralValues(new ArrayList<ObjectDTO>(coll));
	}

	/**
	 * 
	 * @param list
	 * @return
	 */
	public static List<String> asDistinctLiteralValues(List list){
		
		if (list==null || list.isEmpty())
			return list;
		
		// first we pick out only literals (using HashSet ensures we get only distinct ones)
		HashSet set = new HashSet();
		for (int i=0; i<list.size(); i++){
			String s = list.get(i).toString();
			if (!URLUtil.isURL(s))
				set.add(s);
		}
		
		// if no distinct literals were found at all, return the list as it was given
		if (set.isEmpty())
			return list;
		
		return new ArrayList<String>(set);
	}

	/**
	 * 
	 * @return
	 */
	public String getUrl(){
		return getValue(SpecialKeys.RESOURCE_URL);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getTitle(){
		return getValue(SpecialKeys.RESOURCE_TITLE);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public String getValue(String key){
		
		Collection<ObjectDTO> values = get(key);
		return values!=null && values.size()>0 ? values.iterator().next().toString() : null;
	}
}
