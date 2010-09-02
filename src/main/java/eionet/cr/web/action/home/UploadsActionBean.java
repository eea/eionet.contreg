package eionet.cr.web.action.home;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.SpoBinaryDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SpoBinaryDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.dto.UploadDTO;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.UploadHarvest;
import eionet.cr.util.Hashes;
import eionet.cr.web.security.CRUser;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

@UrlBinding("/home/{username}/uploads")
public class UploadsActionBean extends AbstractHomeActionBean implements Runnable{
	
	/** */
	private String title;
	/** */
	private FileBean uploadedFile;
	/** */
	private boolean replaceExisting;
	/** */
	private Collection<UploadDTO> uploads;
	/** */
	private boolean contentSaved;
	/** */
	private Exception saveAndHarvestException;
	
	/** URI assigned to the uploaded file as a subject. */
	private String subjectUri;

	/**
	 * 
	 * @return
	 */
	@DefaultHandler
	public Resolution view(){
		
		return new ForwardResolution("/pages/home/uploads.jsp");
	}
	
	/**
	 * 
	 * @return
	 * @throws DAOException 
	 * @throws IOException 
	 */
	public Resolution add() throws DAOException, IOException{
		
		try{
			return doAdd();
		}
		finally{
			deleteUploadedFile(uploadedFile);
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws DAOException
	 * @throws IOException
	 */
	private Resolution doAdd() throws DAOException, IOException{
		
		Thread thread = null;
		Resolution resolution = new ForwardResolution("/pages/home/addUpload.jsp");
		if (isPostRequest()){

			logger.debug("Uploaded file: " + uploadedFile);
			
			if (uploadedFile!=null){
				
				// unless a replace requested, make sure file does not already exist
				if (replaceExisting==false){
					if (DAOFactory.get().getDao(HelperDAO.class).isExistingSubject(getSubjectUri())){
						addWarningMessage("A file with such a name already exists!" +
								" Use \"replace existing\" checkbox to overwrite.");
						return resolution;
					}
				}
				
				// save the file's content and try to harvest it
				saveAndHarvest();

				// redirect to the uploads list
				String urlBinding = getUrlBinding();
				resolution = new RedirectResolution(StringUtils.replace(
						urlBinding, "{username}", getUserName()));
			}
		}
		
		return resolution;
	}
	
	/**
	 * @throws IOException 
	 * @throws DAOException 
	 * 
	 */
	private void saveAndHarvest() throws IOException, DAOException{
		
		// start the thread that saves the file's content and attempts to harvest it
		Thread thread = new Thread(this);
		thread.start();

		// check the thread after every second, exit loop if it hasn't finished in 15 seconds 
		for (int loopCount = 0; thread.isAlive() && loopCount<15; loopCount++){
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				throw new CRRuntimeException(e.toString(), e);
			}
		}
		
		// if the the thread reported an exception, throw it
		if (saveAndHarvestException!=null){
			if (saveAndHarvestException instanceof DAOException){
				throw (DAOException)saveAndHarvestException;
			}
			else if (saveAndHarvestException instanceof IOException){
				throw (IOException)saveAndHarvestException;
			}
			else if (saveAndHarvestException instanceof RuntimeException){
				throw (RuntimeException)saveAndHarvestException;
			}
			else{
				throw new CRRuntimeException(
						saveAndHarvestException.getMessage(), saveAndHarvestException);
			}
		}
		
		// add feedback message to the bean's context
		if (!thread.isAlive()){
			addSystemMessage("File saved and harvested!");
		}
		else{
			if (!contentSaved){
				addSystemMessage("Saving and harvesting the file continues in the background!");
			}
			else{
				addSystemMessage("File content saved, but harvest continues in the background!");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		
		if (uploadedFile==null){
			throw new CRRuntimeException("Uploaded file object must not be null");
		}
		
		// save the file's content into database
		try {
			saveContent(getSubjectUri(), uploadedFile);
			contentSaved = true;
		}
		catch (DAOException e) {
			saveAndHarvestException = e;
			return;
		}
		catch (IOException e) {
			saveAndHarvestException = e;
			return;
		}

		// add cr:hasFile predicate to the user's home URI in SPO
		
		logger.debug("Creating the cr:hasFile predicate");
		
		SubjectDTO subjectDTO = new SubjectDTO(getUser().getHomeUri(), false);
		ObjectDTO objectDTO = new ObjectDTO(getSubjectUri(), false);
		objectDTO.setSourceUri(getUser().getHomeUri());
		subjectDTO.addObject(Predicates.CR_HAS_FILE, objectDTO);
		
		try{
			DAOFactory.get().getDao(HelperDAO.class).addTriples(subjectDTO);

			// make sure cr:hasFile is present in RESOURCE
			DAOFactory.get().getDao(HelperDAO.class).addResource(
					Predicates.CR_HAS_FILE, getUser().getHomeUri());
		}
		catch (DAOException e){
			saveAndHarvestException = e;
			return;
		}
		
		// attempt to harvest the uploaded file
		harvestUploadedFile(getSubjectUri(), uploadedFile, title);
	}

	
	/**
	 * 
	 * @param subjectUri
	 * @param fileBean
	 * @throws DAOException
	 * @throws IOException
	 */
	private void saveContent(String subjectUri, FileBean fileBean) throws DAOException, IOException{
		
		logger.debug("Going to save the uploaded file's content into database");
		
		SpoBinaryDTO dto = new SpoBinaryDTO(Hashes.spoHash(subjectUri), fileBean.getInputStream());
		dto.setContentType(uploadedFile.getContentType());
		dto.setLanguage("");
		dto.setMustEmbed(false);
		InputStream contentStream = null;
		try{
			contentStream = uploadedFile.getInputStream();
			DAOFactory.get().getDao(SpoBinaryDAO.class).add(dto, uploadedFile.getSize());
		}
		finally{
			IOUtils.closeQuietly(contentStream);
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
	 * @return
	 */
	public Resolution rename(){
		
		// TODO
		return new ForwardResolution("/pages/home/uploads.jsp");
	}

	/**
	 * 
	 * @return
	 */
	public Resolution cancel(){
		
		return new RedirectResolution(StringUtils.replace(
				getUrlBinding(), "{username}", getUserName()));
	}

	/**
	 * @throws DAOException 
	 * 
	 */
	@ValidationMethod(on={"add", "edit", "delete"})
	public void validatePostEvent() throws DAOException{
		
		// the below validation is relevant only when the event is requested through POST method
		if (!isPostRequest()){
			return;
		}
		
		// for all the above POST events, user must be authorized
		if (!isUserAuthorized() || getUser()==null){
			addGlobalValidationError("User not logged in!");
			return;
		}
		
		// for edit and add events, the Title is mandatory
		String eventName = getContext().getEventName();
		if (eventName.equals("add") || eventName.equals("edit")){
			if (StringUtils.isBlank(title)){
				addGlobalValidationError("Title is missing!");
			}
		}
		
		// if add event, make sure the file bean is not null,
		if (eventName.equals("add")){
			
			if (uploadedFile==null){
				addGlobalValidationError("No file specified!");				
			}
		}
		
		// if any validation errors were set above, make sure the right resolution is returned
		if (hasValidationErrors()){
			
			Resolution resolution = new ForwardResolution("/pages/home/uploads.jsp");
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

	/**
	 * @return the uploads
	 * @throws DAOException 
	 */
	public Collection<UploadDTO> getUploads() throws DAOException {

		if (uploads==null || uploads.isEmpty()){
			
			CRUser crUser = getUser();
			if (crUser!=null){
				uploads = DAOFactory.get().getDao(HelperDAO.class).getUserUploads(getUser());
			}
		}
		
		return uploads;
	}


	/**
	 * @return the subjectUri
	 */
	public String getSubjectUri() {
		
		if (StringUtils.isBlank(subjectUri)){
			if (uploadedFile!=null && getUser()!=null){
				subjectUri = getUser().getHomeUri() + "/" + uploadedFile.getFileName();
			}
		}
		
		return subjectUri;
	}
}
