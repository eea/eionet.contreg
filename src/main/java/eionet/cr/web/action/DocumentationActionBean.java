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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.DocumentationDAO;
import eionet.cr.dto.DocumentationDTO;
import eionet.cr.filestore.FileStore;

/**
 * 
 * @author Risto Alt
 * 
 */
@UrlBinding("/documentation/{pageId}/{event}")
public class DocumentationActionBean extends AbstractActionBean {

    /** */
    private String pageId;
    private String event;
    private String content;

    /** */
    public static final String PATH = GeneralConfig.getRequiredProperty(GeneralConfig.FILESTORE_PATH);

    /** */
    public static final String APP_URL = GeneralConfig.getProperty(GeneralConfig.APPLICATION_HOME_URL);

    /**
     * Properties for documentation add page
     */
    private String pid;
    private FileBean file;
    private String contentType;
    private String title;
    private boolean overwrite;

    private boolean editableContent;

    /**
     * 
     * @return Resolution
     * @throws DAOException
     *             if query fails
     */
    @DefaultHandler
    public Resolution view() throws Exception {

        String forward = "/pages/documentation.jsp";
        DocumentationDAO docDAO = DAOFactory.get().getDao(DocumentationDAO.class);
        if (StringUtils.isBlank(pageId) || (pageId != null && pageId.equals("allobjects"))) {
            List<DocumentationDTO> docs = null;
            if (pageId != null && pageId.equals("allobjects")) {
                docs = docDAO.getDocObjects(false);
            } else {
                docs = docDAO.getDocObjects(true);
            }
            if (docs != null) {
                StringBuffer buf = new StringBuffer();
                buf.append("<ul>");
                for (DocumentationDTO doc : docs) {
                    String url = APP_URL + "/documentation/" + doc.getPageId();
                    String title = (StringUtils.isBlank(doc.getTitle())) ? doc.getPageId() : doc.getTitle();
                    buf.append("<li><a href=\"").append(url).append("\">").append(title).append("</a></li>");
                }
                buf.append("</ul>");
                content = buf.toString();
                title = "Documentation";
            }
        } else {
            DocumentationDTO doc = docDAO.getDocObject(pageId);
            if (doc == null) {
                addCautionMessage("Such page ID doesn't exist in database: " + pageId);
            } else {
                if (!StringUtils.isBlank(event) && event.equals("edit")) {
                    contentType = doc.getContentType();
                    title = doc.getTitle();
                    if (doc.getContentType().startsWith("text/")) {
                        File f = FileStore.getInstance("documentation").get(pageId);
                        if (f != null) {
                            content = FileUtils.readFileToString(f, "UTF-8");
                            editableContent = true;
                        } else {
                            addCautionMessage("File does not exist: " + pageId);
                        }
                    }
                    forward = "/pages/documentationEdit.jsp";
                } else {
                    if (doc.getContentType().startsWith("text/")) {
                        File f = FileStore.getInstance("documentation").get(pageId);
                        if (f != null) {
                            content = FileUtils.readFileToString(f, "UTF-8");
                        } else {
                            addCautionMessage("File does not exist: " + pageId);
                        }
                        title = doc.getTitle();
                    } else {
                        File f = FileStore.getInstance("documentation").get(pageId);
                        return new StreamingResolution(doc.getContentType(), new FileInputStream(f));
                    }
                }
            }
        }
        return new ForwardResolution(forward);
    }

    /**
     * Simply forwards to add documentation page
     * 
     * @return Resolution
     * @throws Exception
     */
    public Resolution add() throws Exception {
        return new ForwardResolution("/pages/documentationAdd.jsp");
    }

    /**
     * Edit page
     * 
     * @return Resolution
     * @throws Exception
     */
    public Resolution editContent() throws Exception {
        if (title == null) {
            title = "";
        }
        insertContent();
        addSystemMessage("Successfully saved!");
        return new RedirectResolution("/documentation/" + pid + "/edit");
    }

    /**
     * Adds content into documentation table
     * 
     * @return Resolution
     * @throws DAOException
     */
    public Resolution addContent() throws Exception {

        // The page title is not mandatory. If it is not filled in, then it takes the value of the page_id.
        if (StringUtils.isBlank(title)) {
            title = pid;
        }
        insertContent();

        return new RedirectResolution("/documentation/" + pid + "/edit");
    }

    /**
     * Insert content into database
     * 
     * @throws Exception
     */
    private void insertContent() throws Exception {

        if (isUserLoggedIn()) {
            if (isPostRequest()) {
                // Extract file name.
                String fileName = pid;
                if (file != null && file.getFileName() != null) {
                    if (StringUtils.isBlank(pid)) {
                        fileName = file.getFileName();
                        pid = fileName;
                        // If title is still empty, then set it to file name
                        if (StringUtils.isBlank(title)) {
                            title = fileName;
                        }
                    }
                }

                // If content type is not filled in, then it takes the content-type of the file.
                // If that's not available, then it is application/octet-stream.
                // If file is null the the content-type is "text/html"
                if (StringUtils.isBlank(contentType)) {
                    if (file != null) {
                        contentType = file.getContentType();
                        if (StringUtils.isBlank(contentType)) {
                            contentType = "application/octet-stream";
                        }
                    } else {
                        contentType = "text/html";
                    }
                }

                InputStream is = null;
                if (file != null) {
                    is = file.getInputStream();
                } else if (content != null) {
                    is = new ByteArrayInputStream(content.getBytes("UTF-8"));
                } else {
                    is = new ByteArrayInputStream("".getBytes());
                }
                FileStore.getInstance("documentation").add(fileName, true, is);
                DocumentationDAO dao = DAOFactory.get().getDao(DocumentationDAO.class);
                dao.insertContent(pid, contentType, title);
            }
        } else {
            addWarningMessage(getBundle().getString("not.logged.in"));
        }
    }

    @ValidationMethod(on = {"addContent"})
    public void validatePageId(ValidationErrors errors) throws Exception {
        // If overwrite = false, then check if page id already exists
        if (!StringUtils.isBlank(pid) && !overwrite) {
            boolean exists = DAOFactory.get().getDao(DocumentationDAO.class).idExists(pid);
            if (exists) {
                errors.add("pid", new SimpleError("Such Page ID already exists!"));
                getContext().setValidationErrors(errors);
            }
        }
        if (file == null && StringUtils.isBlank(pid)) {
            errors.add("pid", new SimpleError("If no file is chosen, then Page ID is mandatory!"));
            getContext().setValidationErrors(errors);
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public boolean isEditableContent() {
        return editableContent;
    }

    public void setEditableContent(boolean editableContent) {
        this.editableContent = editableContent;
    }

}
