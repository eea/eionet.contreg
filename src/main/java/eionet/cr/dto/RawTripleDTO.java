package eionet.cr.dto;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class RawTripleDTO {

	/** */
	private String subject;
	private String predicate;
	private String object;
	private String objectDerivSource;

	/**
	 * 
	 */
	public RawTripleDTO(){
	}
	
	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}
	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}
	/**
	 * @return the predicate
	 */
	public String getPredicate() {
		return predicate;
	}
	/**
	 * @param predicate the predicate to set
	 */
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	/**
	 * @return the object
	 */
	public String getObject() {
		return object;
	}
	/**
	 * @param object the object to set
	 */
	public void setObject(String object) {
		this.object = object;
	}

	/**
	 * @return the objectDerivSource
	 */
	public String getObjectDerivSource() {
		return objectDerivSource;
	}

	/**
	 * @param objectDerivSource the objectDerivSource to set
	 */
	public void setObjectDerivSource(String objectDerivSource) {
		this.objectDerivSource = objectDerivSource;
	}
}
