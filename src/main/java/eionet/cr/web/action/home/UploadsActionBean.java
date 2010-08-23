package eionet.cr.web.action.home;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.dao.DAOException;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.UploadHarvest;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.ValidationMethod;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

@UrlBinding("/home/{username}/uploads")
public class UploadsActionBean extends AbstractHomeActionBean {
	
	/** */
	private String title;
	/** */
	private FileBean uploadedFile;
	/** */
	private boolean replaceExisting;

	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
	@DefaultHandler
	public Resolution view() throws DAOException {
		return new ForwardResolution("/pages/home/uploads.jsp");
	}
	
	
	
	/**
	 * 
	 * @return
	 */
	public Resolution add(){
		
		Resolution resolution = new ForwardResolution("/pages/home/addUpload.jsp");
		if (isPostRequest()){

			logger.debug("Uploaded file: " + uploadedFile);
			
			if (uploadedFile!=null){
				
				String sourceUri = getUser().getHomeUri() + uploadedFile.getFileName();
				logger.debug("Uploaded file's pseudo-source: " + sourceUri);
				
				try {
					UploadHarvest uploadHarvest = new UploadHarvest(sourceUri, uploadedFile);
					uploadHarvest.execute();
				}
				catch (HarvestException e) {
					logger.info("Exception when trying to harvest uploaded file content", e);
				}

				// delete uploaded file now that the parsing has been done
				deleteUploadedFile();
			}
			resolution = new ForwardResolution("/pages/home/uploads.jsp");
		}
		
		return resolution;
	}
	
	/**
	 * 
	 */
	private void deleteUploadedFile(){
		
		// double check for null, even though this method should only be called
		// when the file object IS NOT null
		if (uploadedFile!=null){
			try{
				uploadedFile.delete();
			}
			catch (IOException ioe){
				logger.error("Failed to delete the temporarily saved uploaded file", ioe);
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public Resolution edit(){
		
		// TODO
		return new ForwardResolution("/pages/home/uploads.jsp");
	}

	/**
	 * 
	 * @return
	 */
	public Resolution delete(){
		
		// TODO
		return new ForwardResolution("/pages/home/uploads.jsp");
	}

	/**
	 * 
	 */
	@ValidationMethod(on={"add", "edit", "delete"})
	public void validatePostEvent(){
		
		// the below validation is relevant only when the event is requested through POST method
		if (!isPostRequest()){
			return;
		}
		
		// for all the above POST events, user must be authorized
		if (!isUserAuthorized() || getUser()==null){
			addGlobalValidationError("User not logged in!");
			return;
		}
		
		String eventName = getContext().getEventName();
		if (eventName.equals("add") || eventName.equals("edit")){
			if (StringUtils.isBlank(title)){
				addGlobalValidationError("Title is missing!");
			}
		}
		
		if (eventName.equals("add")){
			if (uploadedFile==null){
				addGlobalValidationError("No file specified!");
			}
		}
		
		if (hasValidationErrors()){
			
			Resolution resolution = new ForwardResolution("/pages/home/uploads.jsp");;
			if (eventName.equals("add")){
				resolution = new ForwardResolution("/pages/home/addUpload.jsp");
			}
			
			getContext().setSourcePageResolution(resolution);
		}
	}
	
	/**
	 * 
	 */
	@Before(stages=LifecycleStage.CustomValidation)
	public void setEnvironmentParams(){
		setEnvironmentParams(getContext(), AbstractHomeActionBean.TYPE_UPLOADS);
	}



	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}



	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}



	/**
	 * @return the uploadedFile
	 */
	public FileBean getUploadedFile() {
		return uploadedFile;
	}



	/**
	 * @param uploadedFile the uploadedFile to set
	 */
	public void setUploadedFile(FileBean uploadedFile) {
		this.uploadedFile = uploadedFile;
	}



	/**
	 * @return the replaceExisting
	 */
	public boolean isReplaceExisting() {
		return replaceExisting;
	}



	/**
	 * @param replaceExisting the replaceExisting to set
	 */
	public void setReplaceExisting(boolean replaceExisting) {
		this.replaceExisting = replaceExisting;
	}
}
