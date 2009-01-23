package eionet.cr.dto;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class RDFSubject extends HashMap<RDFPredicate,Collection<RDFObject>>{
	
	/** */
	private String uri;
	private boolean anonymous;
	
	/**
	 * 
	 * @param uri
	 * @param anonymous
	 */
	public RDFSubject(String uri, boolean anonymous){
		super();
		this.uri = uri;
		this.anonymous = anonymous;
	}

	/**
	 * 
	 * @param s
	 * @return
	 */
	public Collection<RDFObject> get(String s){
		return super.get(new RDFPredicate(s));
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
		
		if (!(other instanceof RDFSubject))
			return false;
		
		
		String otherUri = ((RDFSubject)other).getUri();
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
