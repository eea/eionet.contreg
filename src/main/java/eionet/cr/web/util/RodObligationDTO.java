package eionet.cr.web.util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class RodObligationDTO implements Comparable{

	/** */
	private String id = null;
	private String label = null;

	/**
	 * 
	 */
	public RodObligationDTO(String id, String label){
		this.id = id;
		this.label = label;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		return this.label.compareTo(((RodObligationDTO)o).getLabel());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o){
		return compareTo(o)==0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuffer buf = new StringBuffer("id=");
		buf.append(id).append(", label=").append(label);
		return buf.toString();
	}
}
