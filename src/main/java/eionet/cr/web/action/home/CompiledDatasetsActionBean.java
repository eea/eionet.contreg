package eionet.cr.web.action.home;

import java.util.Collection;
import java.util.List;

import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.CompiledDatasetDAO;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.UploadDTO;
import eionet.cr.filestore.FileStore;
import eionet.cr.web.security.CRUser;

/**
 *
 * @author altnyris
 *
 */

@UrlBinding("/home/{username}/compiledDatasets")
public class CompiledDatasetsActionBean extends AbstractHomeActionBean {

    /** */
    private Collection<UploadDTO> compiledDatasets;
    /** URIs of subjects that were selected on submit event. */
    private List<String> subjectUris;

    /**
     *
     * @return Resolution
     */
    @DefaultHandler
    public Resolution view() {

        return new ForwardResolution("/pages/home/compiledDatasets.jsp");
    }

    /**
     *
     * @return Resolution
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

            DAOFactory.get().getDao(HarvestSourceDAO.class).removeHarvestSources(subjectUris);
        }

        return new ForwardResolution("/pages/home/compiledDatasets.jsp");
    }

    /**
     *
     * @return Resolution
     */
    public Resolution cancel() {

        return new RedirectResolution(StringUtils.replace(getUrlBinding(), "{username}", getUserName()));
    }

    /**
     * @throws DAOException
     *
     */
    @ValidationMethod(on = { "delete" })
    public void validatePostEvent() throws DAOException {

        // the below validation is relevant only when the event is requested through POST method
        if (!isPostRequest()) {
            return;
        }
        // for all the above POST events, user must be logged in
        if (getUser() == null) {
            addGlobalValidationError("User not logged in!");
            return;
        }
        // for all the above POST events, user must be authorized
        if (!isUserAuthorized()) {
            addGlobalValidationError("User is not authorised to make changes in this folder!");
        }
    }

    /**
     *
     */
    @Before(stages = LifecycleStage.CustomValidation)
    public void setEnvironmentParams() {
        setEnvironmentParams(getContext(), AbstractHomeActionBean.TYPE_COMPILED_DATASETS, true);
    }

    /**
     * @return the compiled datasets
     * @throws DAOException
     */
    public Collection<UploadDTO> getCompiledDatasets() throws DAOException {

        if (compiledDatasets == null || compiledDatasets.isEmpty()) {

            CRUser user = getUser();
            if (user == null || !isUserAuthorized()) {
                String attemptedUserName = getAttemptedUserName();
                if (!StringUtils.isBlank(attemptedUserName)) {
                    user = new CRUser(getAttemptedUserName());
                }
            }

            if (user != null) {
                compiledDatasets = DAOFactory.get().getDao(CompiledDatasetDAO.class).getUserCompiledDatasets(user);
            }
        }

        return compiledDatasets;
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
}
