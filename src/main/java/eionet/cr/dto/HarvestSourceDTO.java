/**
 *
 */
package eionet.cr.dto;

import java.io.Serializable;
import java.util.Date;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;

/**
 * @author altnyris
 *
 */
public class HarvestSourceDTO implements Serializable {
	private Integer sourceId;
	private String identifier;
	private String pullUrl;
	private String type;
	private String emails;
	private Date dateCreated;
	private String creator;
	private Integer statements;
	private HarvestScheduleDTO harvestSchedule;
	
	/**
	 * 
	 */
	public HarvestSourceDTO(){
	}
	
	/** Gets the harvest source with the corresponding ID, or null if it does not exist. */
    public HarvestSourceDTO getHarvestSource(int id) throws DAOException {
        return DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(id);
    }
	
	public Integer getSourceId() {
		return sourceId;
	}
	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getPullUrl() {
		return pullUrl;
	}
	public void setPullUrl(String pullUrl) {
		this.pullUrl = pullUrl;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getEmails() {
		return emails;
	}
	public void setEmails(String emails) {
		this.emails = emails;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public Integer getStatements() {
		return statements;
	}
	public void setStatements(Integer statements) {
		this.statements = statements;
	}
	public HarvestScheduleDTO getHarvestSchedule() {
		return harvestSchedule;
	}
	public void setHarvestSchedule(HarvestScheduleDTO harvestSchedule) {
		this.harvestSchedule = harvestSchedule;
	}
}
