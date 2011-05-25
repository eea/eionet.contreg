package eionet.cr.web.action.home;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletRequest;

import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.common.Predicates;
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
import eionet.cr.filestore.FileStore;
import eionet.cr.util.Hashes;
import eionet.cr.util.URIUtil;
import eionet.cr.web.security.CRUser;

/**
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

@UrlBinding("/home/{username}/uploads")
public class UploadsActionBean extends AbstractHomeActionBean implements Runnable {

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

    /** URIs of subjects that were selected on submit event. */
    private List<String> subjectUris;

    /** The part of the uploaded file's URI which precedes the filename. */
    private String uriPrefix;

    /** */
    private boolean fileExists;

    /**
     *
     * @return
     */
    @DefaultHandler
    public Resolution view() {

        return new ForwardResolution("/pages/home/uploads.jsp");
    }

    /**
     *
     * @return
     * @throws DAOException
     * @throws IOException
     */
    public Resolution add() throws DAOException, IOException {

        try {
            return doAdd();
        } finally {
            deleteUploadedFile(uploadedFile);
        }
    }

    /**
     *
     * @return
     * @throws DAOException
     * @throws IOException
     */
    private Resolution doAdd() throws DAOException, IOException {

        Resolution resolution = new ForwardResolution("/pages/home/addUpload.jsp");
        if (isPostRequest()) {

            logger.debug("Uploaded file: " + uploadedFile);

            if (uploadedFile != null) {

                // if file content is empty (e.f. 0 KB file), no point in continuing
                if (uploadedFile.getSize() <= 0) {
                    addWarningMessage("The file must not be empty!");
                    return resolution;
                }

                // check if the file exists
                fileExists = DAOFactory.get().getDao(HelperDAO.class).isExistingSubject(getUploadedFileSubjectUri());

                // if file exists and replace not requested, report a warning
                if (!replaceExisting && fileExists) {
                    addWarningMessage("A file with such a name already exists!"
                            + " Use \"replace existing\" checkbox to overwrite.");
                    return resolution;
                }

                // save the file's content and try to harvest it
                saveAndHarvest();

                // redirect to the uploads list
                String urlBinding = getUrlBinding();
                resolution = new RedirectResolution(StringUtils.replace(urlBinding, "{username}", getUserName()));
            }
        }

        return resolution;
    }

    /**
     * @throws IOException
     * @throws DAOException
     *
     */
    private void saveAndHarvest() throws IOException, DAOException {

        // start the thread that saves the file's content and attempts to harvest it
        Thread thread = new Thread(this);
        thread.start();

        // check the thread after every second, exit loop if it hasn't finished in 15 seconds
        for (int loopCount = 0; thread.isAlive() && loopCount < 15; loopCount++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new CRRuntimeException(e.toString(), e);
            }
        }

        // if the the thread reported an exception, throw it
        if (saveAndHarvestException != null) {
            if (saveAndHarvestException instanceof DAOException) {
                throw (DAOException) saveAndHarvestException;
            } else if (saveAndHarvestException instanceof IOException) {
                throw (IOException) saveAndHarvestException;
            } else if (saveAndHarvestException instanceof RuntimeException) {
                throw (RuntimeException) saveAndHarvestException;
            } else {
                throw new CRRuntimeException(saveAndHarvestException.getMessage(), saveAndHarvestException);
            }
        }

        // add feedback message to the bean's context
        if (!thread.isAlive()) {
            addSystemMessage("File saved and harvested!");
        } else {
            if (!contentSaved) {
                addSystemMessage("Saving and harvesting the file continues in the background!");
            } else {
                addSystemMessage("File content saved, but harvest continues in the background!");
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {

        try {
            runBody();
        } catch (RuntimeException e) {
            if (saveAndHarvestException == null) {
                saveAndHarvestException = e;
            }
            throw e;
        } catch (Error e) {
            if (saveAndHarvestException == null) {
                saveAndHarvestException = new CRRuntimeException(e);
            }
            throw e;
        }
    }

    /**
     *
     */
    private void runBody() {

        // at this stage, the uploaded file must not be null
        if (uploadedFile == null) {
            throw new CRRuntimeException("Uploaded file object must not be null");
        }

        String userName = getUserName();

        // prepare cr:hasFile predicate
        ObjectDTO objectDTO = new ObjectDTO(getUploadedFileSubjectUri(), false);
        objectDTO.setSourceUri(getUser().getHomeUri());
        SubjectDTO homeSubjectDTO = new SubjectDTO(getUser().getHomeUri(), false);
        homeSubjectDTO.addObject(Predicates.CR_HAS_FILE, objectDTO);

        // declare file subject DTO, set it to null for starters
        SubjectDTO fileSubjectDTO = null;

        // if title needs to be stored, add it to file subject DTO
        if (!fileExists || !StringUtils.isBlank(title)) {

            String titleToStore = title;
            if (StringUtils.isBlank(titleToStore)) {
                titleToStore = URIUtil.extractURILabel(getUploadedFileSubjectUri(), SubjectDTO.NO_LABEL);
                titleToStore = StringUtils.replace(titleToStore, "%20", " ");
            }

            objectDTO = new ObjectDTO(titleToStore, true);
            objectDTO.setSourceUri(getUser().getHomeUri());
            fileSubjectDTO = new SubjectDTO(getUploadedFileSubjectUri(), false);
            fileSubjectDTO.addObject(Predicates.DC_TITLE, objectDTO);
        }

        logger.debug("Creating the cr:hasFile predicate");
        try {
            // persist the prepared cr:hasFile and dc:title predicates
            DAOFactory.get().getDao(HelperDAO.class).addTriples(homeSubjectDTO);

            // store file subject DTO if it has been initialized
            if (fileSubjectDTO != null) {

                // delete previous value of dc:title if new one set
                if (fileExists && fileSubjectDTO.hasPredicate(Predicates.DC_TITLE)) {
                    DAOFactory
                            .get()
                            .getDao(HelperDAO.class)
                            .deleteTriples(fileSubjectDTO.getUri(), Collections.singletonList(Predicates.DC_TITLE),
                                    getUser().getHomeUri());
                }
                DAOFactory.get().getDao(HelperDAO.class).addTriples(fileSubjectDTO);
            }

            // since user's home URI was used above as triple source, add it to HARVEST_SOURCE too
            // (but set interval minutes to 0, to avoid it being background-harvested)
            DAOFactory.get().getDao(HarvestSourceDAO.class)
                    .addSourceIgnoreDuplicate(HarvestSourceDTO.create(getUser().getHomeUri(), true, 0, getUserName()));

        } catch (DAOException e) {
            saveAndHarvestException = e;
            return;
        }

        // save the file's content into database
        try {
            saveContent();
            contentSaved = true;
        } catch (DAOException e) {
            saveAndHarvestException = e;
            return;
        } catch (IOException e) {
            saveAndHarvestException = e;
            return;
        }

        // attempt to harvest the uploaded file
        harvestUploadedFile(getUploadedFileSubjectUri(), uploadedFile, null, userName);
    }

    /**
     *
     * @throws DAOException
     * @throws IOException
     */
    private void saveContent() throws DAOException, IOException {

        logger.debug("Going to save the uploaded file's content into database");

        SpoBinaryDTO dto = new SpoBinaryDTO(Hashes.spoHash(getUploadedFileSubjectUri()));
        dto.setContentType(uploadedFile.getContentType());
        dto.setLanguage("");
        dto.setMustEmbed(false);

        InputStream contentStream = null;
        try {
            DAOFactory.get().getDao(SpoBinaryDAO.class).add(dto, uploadedFile.getSize());
            contentStream = uploadedFile.getInputStream();
            FileStore.getInstance(getUserName()).add(uploadedFile.getFileName(), replaceExisting, contentStream);
        } finally {
            IOUtils.closeQuietly(contentStream);
        }
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution delete() throws DAOException {

        if (subjectUris != null && !subjectUris.isEmpty()) {

            DAOFactory.get().getDao(HelperDAO.class).deleteUserUploads(getUserName(), subjectUris);

            FileStore fileStore = FileStore.getInstance(getUserName());
            for (String subjectUri : subjectUris) {
                String fileName = StringUtils.substringAfterLast(subjectUri, "/");
                fileName = StringUtils.replace(fileName, "%20", " ");
                fileStore.delete(fileName);
            }

            DAOFactory.get().getDao(HarvestSourceDAO.class).queueSourcesForDeletion(subjectUris);
        }

        return new ForwardResolution("/pages/home/uploads.jsp");
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution rename() throws DAOException {

        Resolution resolution = new ForwardResolution("/pages/home/renameUploads.jsp");
        if (isPostRequest()) {

            HashMap<String, String> fileRenamings = new HashMap<String, String>();
            HashMap<String, String> uriRenamings = new HashMap<String, String>();

            if (subjectUris != null && !subjectUris.isEmpty()) {

                ServletRequest request = getContext().getRequest();
                for (String curSubjectUri : subjectUris) {

                    long subjectHash = Hashes.spoHash(curSubjectUri);
                    String newFileName = request.getParameter(FNAME_PARAM_PREFIX + subjectHash);
                    if (!StringUtils.isBlank(newFileName)) {

                        String newSubjectUri = getUriPrefix() + StringUtils.replace(newFileName, " ", "%20");
                        uriRenamings.put(curSubjectUri, newSubjectUri);

                        String curFileName = StringUtils.substringAfterLast(curSubjectUri, "/");
                        curFileName = StringUtils.replace(curFileName, "%20", " ");
                        fileRenamings.put(curFileName, newFileName);

                        logger.debug("<" + curSubjectUri + "> will be renamed to <" + newSubjectUri + ">");
                        logger.debug("<" + curFileName + "> will be renamed to <" + newFileName + ">");
                    }
                }
            }

            if (uriRenamings.size() > 0 && uriRenamings.size() == fileRenamings.size()) {

                logger.debug("Calling renamings on DAO");
                DAOFactory.get().getDao(HelperDAO.class).renameUserUploads(uriRenamings);

                logger.debug("Calling renamings on file store");
                FileStore.getInstance(getUserName()).rename(fileRenamings);

                addSystemMessage("Files renamed successfully!");
                resolution = new RedirectResolution(StringUtils.replace(getUrlBinding(), "{username}", getUserName()));
            }
        }

        return resolution;
    }

    /**
     *
     * @return
     */
    public Resolution cancel() {

        return new RedirectResolution(StringUtils.replace(getUrlBinding(), "{username}", getUserName()));
    }

    /**
     * @throws DAOException
     *
     */
    @ValidationMethod(on = { "add", "rename", "delete" })
    public void validatePostEvent() throws DAOException {

        // the below validation is relevant only when the event is requested through POST method
        if (!isPostRequest()) {
            return;
        }

        // for all the above POST events, user must be authorized
        if (!isUserAuthorized() || getUser() == null) {
            addGlobalValidationError("User not logged in!");
            return;
        }

        // if add event, make sure the file bean is not null
        String eventName = getContext().getEventName();
        if (eventName.equals("add")) {

            if (uploadedFile == null) {
                addGlobalValidationError("No file specified!");
            }
        }

        // if any validation errors were set above, make sure the right resolution is returned
        if (hasValidationErrors()) {

            Resolution resolution = new ForwardResolution("/pages/home/uploads.jsp");
            if (eventName.equals("add")) {
                resolution = new ForwardResolution("/pages/home/addUpload.jsp");
            }

            getContext().setSourcePageResolution(resolution);
        }
    }

    /**
     *
     */
    @Before(stages = LifecycleStage.CustomValidation)
    public void setEnvironmentParams() {
        setEnvironmentParams(getContext(), AbstractHomeActionBean.TYPE_UPLOADS, true);
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
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
     * @param uploadedFile
     *            the uploadedFile to set
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
     * @param replaceExisting
     *            the replaceExisting to set
     */
    public void setReplaceExisting(boolean replaceExisting) {
        this.replaceExisting = replaceExisting;
    }

    /**
     * @return the uploads
     * @throws DAOException
     */
    public Collection<UploadDTO> getUploads() throws DAOException {

        if (uploads == null || uploads.isEmpty()) {

            CRUser user = getUser();
            if (user == null || !isUserAuthorized()) {
                String attemptedUserName = getAttemptedUserName();
                if (StringUtils.isBlank(attemptedUserName)) {
                    user = new CRUser(getAttemptedUserName());
                }
            }

            if (user != null) {
                uploads = DAOFactory.get().getDao(HelperDAO.class).getUserUploads(user);
            }
        }

        return uploads;
    }

    /**
     * @return the uploadedFileSubjectUri
     */
    public String getUploadedFileSubjectUri() {

        if (StringUtils.isBlank(uploadedFileSubjectUri)) {
            if (uploadedFile != null && getUser() != null) {
                uploadedFileSubjectUri = getUriPrefix() + StringUtils.replace(uploadedFile.getFileName(), " ", "%20");
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
     * @param subjectUris
     *            the subjectUris to set
     */
    public void setSubjectUris(List<String> subjectUris) {
        this.subjectUris = subjectUris;
    }

    /**
     * @return the uriPrefix
     */
    public String getUriPrefix() {

        if (uriPrefix == null) {
            uriPrefix = getUser().getHomeUri() + "/";
        }
        return uriPrefix;
    }

    /**
     *
     * @return
     */
    public String getFileNameParamPrefix() {
        return FNAME_PARAM_PREFIX;
    }
}
