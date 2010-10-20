package eionet.cr.web.action.home;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletRequest;

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
import eionet.cr.util.URIUtil;
import eionet.cr.web.security.CRUser;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

@UrlBinding("/home/{username}/uploads")
public class UploadsActionBean extends AbstractHomeActionBean implements Runnable{
	
	/** */
	public static final String FNAME_PARAM_PREFIX = "name_";
	
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
	private String uploadedFileSubjectUri;
	
	/** URIs of subjects that were selected on submit event */
	private List<String> subjectUris;
	
	/** The part of the uploaded file's URI which precedes the filename */
	private String uriPrefix;
	
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

				// if file content is empty (e.f. 0 KB file), no point in continuing
				if (uploadedFile.getSize()<=0){
					addWarningMessage("The file must not be empty!");
					return resolution;
				}
				
				// unless a replace requested, make sure file does not already exist
				if (replaceExisting==false){
					if (DAOFactory.get().getDao(HelperDAO.class).isExistingSubject(getUploadedFileSubjectUri())){
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
		
		// at this stage, the uploaded file must not be null
		if (uploadedFile==null){
			throw new CRRuntimeException("Uploaded file object must not be null");
		}
		
		String userName = getUserName();
		// prepare cr:hasFile predicate
		ObjectDTO objectDTO = new ObjectDTO(getUploadedFileSubjectUri(), false);
		objectDTO.setSourceUri(getUser().getHomeUri());
		SubjectDTO homeSubjectDTO = new SubjectDTO(getUser().getHomeUri(), false);
		homeSubjectDTO.addObject(Predicates.CR_HAS_FILE, objectDTO);
		
		// prepare the actual value of dc:title to store
		String titleToStore = title;
		if (StringUtils.isBlank(titleToStore)){
			titleToStore = URIUtil.extractURILabel(getUploadedFileSubjectUri(),SubjectDTO.NO_LABEL);
		}
		
		// prepare dc:title predicate
		objectDTO = new ObjectDTO(titleToStore, true);
		objectDTO.setSourceUri(getUser().getHomeUri());
		SubjectDTO fileSubjectDTO = new SubjectDTO(getUploadedFileSubjectUri(), false);
		fileSubjectDTO.addObject(Predicates.DC_TITLE, objectDTO);

		logger.debug("Creating the cr:hasFile predicate");
		try{
			// make sure cr:hasFile is present in RESOURCE
			DAOFactory.get().getDao(HelperDAO.class).addResource(
					Predicates.CR_HAS_FILE, getUser().getHomeUri());

			// make sure file subject URI is present in RESOURCE
			DAOFactory.get().getDao(HelperDAO.class).addResource(
					getUploadedFileSubjectUri(), getUser().getHomeUri());

			// persist the prepared cr:hasFile and dc:title predicates
			DAOFactory.get().getDao(HelperDAO.class).addTriples(homeSubjectDTO);
			DAOFactory.get().getDao(HelperDAO.class).addTriples(fileSubjectDTO);
		}
		catch (DAOException e){
			saveAndHarvestException = e;
			return;
		}

		// save the file's content into database
		try {
			saveContent(uploadedFile);
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

		// attempt to harvest the uploaded file
		harvestUploadedFile(getUploadedFileSubjectUri(), uploadedFile, null, userName);
	}

	/**
	 * 
	 * @param subjectUri
	 * @param fileBean
	 * @throws DAOException
	 * @throws IOException
	 */
	private void saveContent(FileBean fileBean) throws DAOException, IOException{
		
		logger.debug("Going to save the uploaded file's content into database");
		
		SpoBinaryDTO dto = new SpoBinaryDTO(
				Hashes.spoHash(getUploadedFileSubjectUri()), fileBean.getInputStream());
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
	 * @throws DAOException 
	 */
	public Resolution delete() throws DAOException{
		
		if (subjectUris!=null && !subjectUris.isEmpty()){
			
			DAOFactory.get().getDao(HelperDAO.class).deleteSubjects(subjectUris);
			DAOFactory.get().getDao(HarvestSourceDAO.class).queueSourcesForDeletion(subjectUris);
		}
		
		return new ForwardResolution("/pages/home/uploads.jsp");
	}

	/**
	 * 
	 * @return
	 * @throws DAOException 
	 */
	public Resolution rename() throws DAOException{

		Resolution resolution = new ForwardResolution("/pages/home/renameUploads.jsp");
		if (isPostRequest()){
			
			HashMap<Long,String> newUris = new HashMap<Long, String>();
			String uriPrefix = getUriPrefix();
			
			ServletRequest request = getContext().getRequest();
			Enumeration paramNames = request.getParameterNames();
			while (paramNames.hasMoreElements()){
				
				String paramName = (String)paramNames.nextElement();
				if (paramName.startsWith(FNAME_PARAM_PREFIX)){
					
					String newName = request.getParameter(paramName);
					if (StringUtils.isBlank(newName)==false){
						
						String oldHash = StringUtils.substringAfter(paramName, FNAME_PARAM_PREFIX);
						String newUri = uriPrefix + newName;
						newUris.put(Long.valueOf(oldHash), newUri);
					}
				}
			}
			
			if (!newUris.isEmpty()){
				
				DAOFactory.get().getDao(HelperDAO.class).renameSubjects(newUris);
				addSystemMessage("Files renamed successfully!");
				resolution = new RedirectResolution(
						StringUtils.replace(getUrlBinding(), "{username}", getUserName()));
			}
		}
		
		return resolution;
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
	@ValidationMethod(on={"add", "rename", "delete"})
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
		
		// if add event, make sure the file bean is not null
		String eventName = getContext().getEventName();
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
		setEnvironmentParams(getContext(), AbstractHomeActionBean.TYPE_UPLOADS, true);
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
			if (this.isUserAuthorized() && crUser!=null){
				uploads = DAOFactory.get().getDao(HelperDAO.class).getUserUploads(getUser());
			} else {
				uploads = DAOFactory.get().getDao(HelperDAO.class).getUserUploads(new CRUser(this.getAttemptedUserName()));
			}
		}
		
		return uploads;
	}


	/**
	 * @return the uploadedFileSubjectUri
	 */
	public String getUploadedFileSubjectUri() {
		
		if (StringUtils.isBlank(uploadedFileSubjectUri)){
			if (uploadedFile!=null && getUser()!=null){
				uploadedFileSubjectUri = getUriPrefix() + uploadedFile.getFileName();
			}
		}
		
		return uploadedFileSubjectUri;
	}

	/**
	 * @return the subjectUris
	 */
	public List<String> getSubjectUris() {
		return subjectUris;
	}

	/**
	 * @param subjectUris the subjectUris to set
	 */
	public void setSubjectUris(List<String> subjectUris) {
		this.subjectUris = subjectUris;
	}

	/**
	 * @return the uriPrefix
	 */
	public String getUriPrefix() {
		
		if (uriPrefix==null){
			uriPrefix = getUser().getHomeUri() + "/";
		}
		return uriPrefix;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getFileNameParamPrefix(){
		return FNAME_PARAM_PREFIX;
	}
}
