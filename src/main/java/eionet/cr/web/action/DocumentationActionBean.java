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

import eionet.doc.extensions.stripes.DocumentationValidator;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;
import eionet.doc.DocumentationService;
import eionet.doc.dto.DocPageDTO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Action bean for handling events related to the online editing of CR documentation.
 *
 * @author Risto Alt
 */
@UrlBinding("/documentation/{pageId}/{event}")
public class DocumentationActionBean extends AbstractActionBean {

    private String pageId;
    private String event;
    private DocPageDTO pageObject;

    @SpringBean
    private DocumentationService documentationService;

    @SpringBean
    private DocumentationValidator docValidator;

    /**
     *
     * @return Resolution
     * @throws Exception
     */
    @DefaultHandler
    public Resolution view() throws Exception {

        String forward = "/pages/documentation.jsp";
        pageObject = documentationService.view(pageId, event);
        if (pageObject != null && pageObject.getFis() != null) {
            return new StreamingResolution(pageObject.getContentType(), pageObject.getFis());
        }

        return new ForwardResolution(forward);
    }

    /**
     * Edit page
     *
     * @return Resolution
     * @throws Exception
     */
    public Resolution editContent() throws Exception {

        if (isUserLoggedIn()) {
            if (isPostRequest()) {
                documentationService.editContent(pageObject);
                addSystemMessage("Successfully saved!");
            }
        } else {
            addWarningMessage(getBundle().getString("not.logged.in"));
        }
        return new RedirectResolution("/documentation/" + pageObject.getPid() + "/edit");
    }

    /**
     * Adds content into documentation table.
     *
     * @return Resolution
     * @throws Exception
     */
    public Resolution addContent() throws Exception {
        if (isUserLoggedIn()) {
            if (isPostRequest()) {
                documentationService.addContent(pageObject);
            }
        } else {
            addWarningMessage(getBundle().getString("not.logged.in"));
        }
        return new RedirectResolution("/documentation/" + pageObject.getPid() + "/edit");
    }

    /**
     * Validate page id.
     *
     * @param errors the errors
     * @throws Exception the exception
     */
    @ValidationMethod(on = {"addContent"})
    public void validatePageId(ValidationErrors errors) throws Exception {
        // Expects that first parameter is named "pageObject" in this actionBean class
        // Does two validations:
        // - If pageObject.overwrite = false, then checks if page ID already exists
        // - If no file is chosen, then Page ID is mandatory
        docValidator.getStripesValidationErrors(pageObject, errors);
        if (errors != null && errors.size() > 0) {
            getContext().setValidationErrors(errors);
        }
        event = "add";
    }

    /**
     * Deletes content
     *
     * @return Resolution
     * @throws Exception
     */
    public Resolution delete() throws Exception {

        if (isUserLoggedIn()) {
            // The page title is not mandatory. If it is not filled in, then it takes the value of the page_id.
            if (pageObject != null && pageObject.getDocIds() != null && pageObject.getDocIds().size() > 0) {
                documentationService.delete(pageObject);
            } else {
                addWarningMessage("No objects selected!");
            }
        } else {
            addWarningMessage(getBundle().getString("not.logged.in"));
        }

        return new RedirectResolution("/documentation/contents");
    }

    /**
     * Gets the page id.
     *
     * @return the page id
     */
    public String getPageId() {
        return pageId;
    }

    /**
     * Sets the page id.
     *
     * @param pageId the new page id
     */
    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    /**
     * Gets the event.
     *
     * @return the event
     */
    public String getEvent() {
        return event;
    }

    /**
     * Sets the event.
     *
     * @param event the new event
     */
    public void setEvent(String event) {
        this.event = event;
    }

    /**
     * Gets the page object.
     *
     * @return the page object
     */
    public DocPageDTO getPageObject() {
        return pageObject;
    }

    /**
     * Sets the page object.
     *
     * @param pageObject the new page object
     */
    public void setPageObject(DocPageDTO pageObject) {
        this.pageObject = pageObject;
    }

}
