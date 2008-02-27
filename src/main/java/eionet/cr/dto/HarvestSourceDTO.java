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
	private String name;
	private String url;
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
	
	public Integer getSourceId() {
		return sourceId;
	}
	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
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
