/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import eionet.cr.common.Predicates;
import eionet.cr.util.Hashes;
import eionet.cr.util.URIUtil;
import eionet.cr.util.URLUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubjectDTO{
	
	/** */
	public static final String URI_ANONYMOUS = "anonymous";
	
	/** */
	private String uri;
	private long uriHash;
	private boolean anonymous;
	private Map<String,Collection<ObjectDTO>> predicates;
	private Date firstSeenTime;
	private Date lastModifiedTime;
	
	/** */
	private long hitSource;

	/**
	 * 
	 * @param uri
	 * @param anonymous
	 */
	public SubjectDTO(String uri, boolean anonymous){
		
		this.uri = anonymous==false ? uri : URI_ANONYMOUS;
		if (uri!=null){
			uriHash = Hashes.spoHash(uri);
		}
		this.anonymous = anonymous;
		predicates = new HashMap<String,Collection<ObjectDTO>>();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 * This toString is used in the Unit-tests.
	 */
	@Override
	public String toString() {
		return "SubjectDTO[uri=" + uri + ",predicates=" + predicates.toString() + "]";
	}
	
	/**
	 * 
	 * @param predicate
	 * @param object
	 */
	public void addObject(String predicate, ObjectDTO object){
		
		Collection<ObjectDTO> objects = predicates.get(predicate);
		if (objects==null){
			objects = new ArrayList<ObjectDTO>();
			predicates.put(predicate, objects);
		}
		objects.add(object);
	}
	
	/**
	 * 
	 * @param predicate
	 * @param object
	 */
	public void setObject(String predicate, ObjectDTO object){
		
		Collection<ObjectDTO> objects = new ArrayList<ObjectDTO>();
		objects.add(object);
		predicates.put(predicate, objects);
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
	 * @return
	 */
	public Set<String> getPredicateUris(){
		return predicates.keySet();
	}

	/**
	 * 
	 * @param predicate
	 * @return
	 */
	public Collection<ObjectDTO> getObjects(String predicate){
		
		return predicates.get(predicate);
	}

	/**
	 * 
	 * @param predicate
	 * @param objectType
	 * @return
	 */
	public Collection<ObjectDTO> getObjects(String predicate, ObjectDTO.Type objectType){
		
		ArrayList<ObjectDTO> result = new ArrayList<ObjectDTO>();
		
		Collection<ObjectDTO> coll = getObjects(predicate);
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
	public ObjectDTO getObject(String predicate){
		Collection<ObjectDTO> objects = getObjects(predicate);
		return objects==null || objects.isEmpty() ? null : objects.iterator().next();
	}

	/**
	 * 
	 * @param predicate
	 * @param objectType
	 * @return
	 */
	public ObjectDTO getObject(String predicate, ObjectDTO.Type objectType){
		Collection<ObjectDTO> objects = getObjects(predicate, objectType);
		return objects==null || objects.isEmpty() ? null : objects.iterator().next();
	}

	/**
	 * 
	 * @param predicate
	 * @return
	 */
	public String getObjectValue(String predicate){
		ObjectDTO objectDTO = getObject(predicate);
		return objectDTO==null ? null : objectDTO.getValue();
	}

	/**
	 * 
	 * @param predicate
	 * @return
	 */
	public String getObjectValue(String predicate, ObjectDTO.Type objectType){
		ObjectDTO objectDTO = getObject(predicate, objectType);
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
	 * @param predicate
	 * @param objectValue
	 * @return
	 */
	public boolean hasPredicateObject(String predicate, String objectValue){
		
		boolean result = false;
		Collection<ObjectDTO> objects = getObjects(predicate);
		if (objects!=null && !objects.isEmpty()){
			for (Iterator<ObjectDTO> i=objects.iterator(); i.hasNext();){
				if (objectValue.equals(i.next().getValue())){
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * @return the firstSeenTime
	 */
	public Date getFirstSeenTime() {
		return firstSeenTime;
	}

	/**
	 * @param firstSeenTime the firstSeenTime to set
	 */
	public void setFirstSeenTime(Date firstSeenTime) {
		this.firstSeenTime = firstSeenTime;
	}

	/**
	 * @return the uriHash
	 */
	public long getUriHash() {		
		return uriHash;
	}

	/**
	 * @param uriHash the uriHash to set
	 */
	public void setUriHash(long uriHash) {
		this.uriHash = uriHash;
	}
	
	/**
	 * 
	 * @param predicate
	 * @param objectValue
	 * @param sourceHash
	 * @return
	 */
	public boolean existsPredicateObjectSource(String predicate, String objectValue, String sourceUri){
		
		for (ObjectDTO objectDTO:getObjects(predicate)){
			if (objectDTO.getValue().equals(objectValue)){
				if (objectDTO.getSourceUri().equals(sourceUri)){
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * @return the hitSource
	 */
	public long getHitSource() {
		return hitSource;
	}

	/**
	 * @param hitSource the hitSource to set
	 */
	public void setHitSource(long hitSource) {
		this.hitSource = hitSource;
	}

	/**
	 * @return the lastModifiedTime
	 */
	public Date getLastModifiedTime() {
		return lastModifiedTime;
	}

	/**
	 * @param lastModifiedTime the lastModifiedTime to set
	 */
	public void setLastModifiedTime(Date lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getLabel(){
		
		ObjectDTO object = getObject(Predicates.RDFS_LABEL, ObjectDTO.Type.LITERAL);
		String label = object.getValue();
		if (label!=null && label.trim().length()>0)
			return label;
		else if (isAnonymous())
			return "Anonymous resource";
		else
			return URIUtil.deriveLabel(getUri());
	}
}
