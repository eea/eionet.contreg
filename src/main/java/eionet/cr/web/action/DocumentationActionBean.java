/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.web.action;

import java.io.ByteArrayInputStream;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.DocumentationDAO;
import eionet.cr.dto.DocumentationDTO;

/**
 * 
 * @author Risto Alt
 *
 */
@UrlBinding("/documentation/{pageId}")
public class DocumentationActionBean extends AbstractActionBean {

    /** */
    private String pageId;
    private String content;

    /**
     * Properties for documentation upload page
     */
    @Validate(required=true, on="uploadFile", label="Page ID")
    private String pid;
    @Validate(required=true, on="uploadFile")
    private FileBean file;
    @Validate(required=true, on="uploadFile")
    private String contentType;

    /**
     *
     * @return Resolution
     * @throws DAOException if query fails
     */
    @DefaultHandler
    public Resolution view() throws DAOException {

        if (StringUtils.isBlank(pageId)) {
            pageId = "index";
        }
        DocumentationDAO docDAO = DAOFactory.get().getDao(DocumentationDAO.class);
        DocumentationDTO doc = docDAO.getDocObject(pageId);
        if (doc == null) {
            addCautionMessage("Such page ID doesn't exist in database: " + pageId);
        } else {
            if (doc.getContentType().equals("text/html")) {
                content = new String(doc.getContent());
            } else {
                return new StreamingResolution(doc.getContentType(), new ByteArrayInputStream(doc.getContent()));
            }
        }
        return new ForwardResolution("/pages/documentation.jsp");
    }

    /**
     * Simply forwards to upload file page
     * @return Resolution
     * @throws Exception
     */
    public Resolution upload() throws Exception {
        return new ForwardResolution("/pages/documentationUpload.jsp");
    }

    /**
     * Adds uploaded file into documentation table
     * @return Resolution
     * @throws DAOException
     */
    public Resolution uploadFile() throws DAOException {

        if (isUserLoggedIn()) {
            DocumentationDAO dao = DAOFactory.get().getDao(DocumentationDAO.class);
            dao.insertFile(pid, contentType, file);
            String appUrl = GeneralConfig.getProperty(GeneralConfig.APPLICATION_HOME_URL);
            String url = appUrl + "/documentation/" + pid;
            addSystemMessage("File successfully uploaded! File URL is: <a href=\"" + url + "\">" + url + "</a>");
        } else {
            addWarningMessage(getBundle().getString("not.logged.in"));
        }
        return new ForwardResolution("/pages/documentationUpload.jsp");
    }

    @ValidationMethod(on = { "uploadFile" })
    public void validatePageId(ValidationErrors errors) throws Exception {
        if (!StringUtils.isBlank(pid)) {
            boolean exists = DAOFactory.get().getDao(DocumentationDAO.class).idExists(pid);
            if (exists) {
                errors.add("pid", new SimpleError("Such Page ID already exists!"));
                getContext().setValidationErrors(errors);
            }
        }
    }

    public String getPageId() {
        return pageId;
    }
    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public FileBean getFile() {
        return file;
    }

    public void setFile(FileBean file) {
        this.file = file;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
