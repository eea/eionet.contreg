package eionet.cr.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class RodInstrumentDTO implements Comparable{
	
	/** */
	private String id = null;
	private String label = null;
	private List<RodObligationDTO> obligations = null;

	/**
	 * 
	 */
	public RodInstrumentDTO(String id, String label){
		this.id = id;
		this.label = label;
	}
	
	/**
	 * 
	 * @param obligationDTO
	 */
	public void addObligation(RodObligationDTO obligationDTO){
		
		if (obligationDTO==null)
			return;
		
		if (obligations==null)
			obligations = new ArrayList<RodObligationDTO>();
		obligations.add(obligationDTO);
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

	/**
	 * @return the obligations
	 */
	public List<RodObligationDTO> getObligations() {
		
		if (obligations!=null && obligations.size()>0){
			Collections.sort(obligations);
		}
		return obligations;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		return label.compareTo(((RodInstrumentDTO)o).getLabel());
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
		buf.append(id).append(", label=").append(label).append(", obligations=").append(obligations);
		return buf.toString();
	}
}
