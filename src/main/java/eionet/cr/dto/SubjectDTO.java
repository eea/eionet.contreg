package eionet.cr.dto;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubjectDTO extends HashMap<PredicateDTO,Collection<ObjectDTO>>{
	
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
}
