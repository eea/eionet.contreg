package eionet.cr.web.action;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dto.BookmarkFormDTO;
import eionet.cr.harvest.HarvestException;
import eionet.cr.util.URLUtil;
import eionet.cr.web.util.RegisterUrl;

/**
 * Bookmarklet backend action bean.
 *
 * @author Aleksandr Ivanov <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a> (modified for CR)
 */
@UrlBinding("/quickAddBookmark.action")
public class QuickAddBookmarkActionBean extends AbstractActionBean {

    private boolean saveToBookmarks;
    private BookmarkFormDTO resource;
    private String originalPageUrl;
    private boolean loggedIn = false;

    @DefaultHandler
    @HandlesEvent("addBookmark")
    public Resolution init() {
        if (getUser() != null) {
            loggedIn = true;
        }
        return new ForwardResolution("/pages/bookmarklet/quickAddBookmark.jsp");
    }

    @HandlesEvent("processForm")
    public Resolution addBookmark() throws DAOException, HarvestException {
        String url = resource.getSource();
        RegisterUrl.register(url, getUser(), saveToBookmarks, null);
        return new ForwardResolution("/pages/bookmarklet/bookmarkAdded.jsp");
    }

    /**
     *
     */
    @ValidationMethod(on = "processForm")
    public void validateSave() {

        if (StringUtils.isBlank(resource.getSource()) || !URLUtil.isURL(resource.getSource())) {
            addGlobalValidationError(new SimpleError("Not a valid URL!"));
        }
        if (getUser() == null) {
            addGlobalValidationError(new SimpleError("You are not logged in!"));
        }
    }

    @HandlesEvent("installation")
    public Resolution installation() {
        return new ForwardResolution("/pages/bookmarklet/bookmarkletInstaller.jsp");
    }

    /**
     * @return bookmarklet script
     */
    public String getBookmarklet() {

        String appDispName = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_DISPLAY_NAME);

        return "javascript: function go() {" + "u=location.href;" + "a=false;" + "x=window;" + "e=x.encodeURIComponent;"
        + "d=document;" + "if((s=d.selection)" + "?t=s.createRange().text" + ":t=x.getSelection()+'')"
        + "(r=/^http:\\/\\/\\S+$/.exec(t))" + "?u=t" + ":a=true;" + "a"
        + "?alert('Please highlight a full URL, or deselect text to add this page.')" + ":window.open(" + "'"
        + getBaseUrl(this.getContext()) + "/quickAddBookmark.action?"
        + "resource.source='+e(u)+'&amp;resource.title='+e(d.title)+'&amp;originalPageUrl='+e(location.href)"
        + ", 'Add bookmark to " + appDispName + "', 'left=20,top=20,width=700,height=500')}; go();";
    }

    /**
     * @return the resource
     */
    public BookmarkFormDTO getResource() {
        return resource;
    }

    /**
     * @param resource the resource to set
     */
    public void setResource(BookmarkFormDTO resource) {
        this.resource = resource;
    }

    /**
     * @return the originalPageUrl
     */
    public String getOriginalPageUrl() {
        return originalPageUrl;
    }

    /**
     * @param originalPageUrl the originalPageUrl to set
     */
    public void setOriginalPageUrl(String originalPageUrl) {
        this.originalPageUrl = originalPageUrl;
    }

    public boolean isSaveToBookmarks() {
        return saveToBookmarks;
    }

    public void setSaveToBookmarks(boolean saveToBookmarks) {
        this.saveToBookmarks = saveToBookmarks;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

}
