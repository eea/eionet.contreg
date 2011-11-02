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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */

package eionet.cr.web.action.admin;

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.PostHarvestScriptDAO;
import eionet.cr.dto.PostHarvestScriptDTO;
import eionet.cr.dto.PostHarvestScriptDTO.TargetType;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.util.ApplicationCache;

/**
 *
 * @author Jaanus Heinlaid
 */
@UrlBinding("/admin/postHarvestScript")
public class PostHarvestScriptActionBean extends AbstractActionBean {

    /** */
    private static final String SCRIPT_JSP = "/pages/admin/postHarvestScripts/script.jsp";

    /** */
    private int id;
    private String title;
    private String script;
    private String targetUrl;
    private TargetType targetType;
    private String testSourceUrl;
    private boolean active;
    private boolean ignoreMalformedSparql;

    @DefaultHandler
    public Resolution defaultHandler() throws DAOException {

        if (id > 0) {
            PostHarvestScriptDTO dto = DAOFactory.get().getDao(PostHarvestScriptDAO.class).fetch(id);
            if (dto != null) {
                title = dto.getTitle();
                script = dto.getScript();
                targetUrl = dto.getTargetUrl();
                targetType = dto.getTargetType();
                active = dto.isActive();
            }
        }

        return new ForwardResolution(SCRIPT_JSP);
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution save() throws DAOException {

        // // If malformed script should not be ignored (i.e. first time save button is pressed),
        // // perform script validation, and issue a warning message to the user if the script
        // // was found malformed. Otherwise (i.e. save button pressed second time), lower the ignore flag.
        // if (ignoreMalformedSparql == false) {
        // try {
        // new SPARQLParser().parseQuery(script, null);
        // } catch (MalformedQueryException e) {
        // ignoreMalformedSparql = true;
        // addCautionMessage("Script does not seem to be valid SPARQL: " + e.getMessage()
        // + ".<br/><strong>Save again to ignore this message!</strong>");
        // return new ForwardResolution(SCRIPT_JSP);
        // }
        // } else {
        // ignoreMalformedSparql = false;
        // }

        // If id given, do save by the given id, otherwise do addition of brand new script.
        if (id > 0) {
            DAOFactory.get().getDao(PostHarvestScriptDAO.class).save(id, title, script, active);
        } else {
            id = DAOFactory.get().getDao(PostHarvestScriptDAO.class).insert(targetType, targetUrl, title, script, active);
        }
        addSystemMessage("Script successfully saved!");

        // Depending on whether "Save" or "Save & close" was pressed, forward back to the same script's
        // view or to the list of all scripts.
        if (getContext().getRequestParameter("save").equalsIgnoreCase("Save & close")) {
            if (logger.isTraceEnabled()) {
                logger.trace("Redirecting after save & close");
            }
            return resolutionToScripts();
        } else {
            return new RedirectResolution(PostHarvestScriptActionBean.class).addParameter("id", id);
        }
    }

    /**
     *
     * @return
     */
    public Resolution test() {

        if (logger.isTraceEnabled()) {
            logger.trace("Handling test event");
        }
        return new ForwardResolution(SCRIPT_JSP);
    }

    /**
     *
     * @return
     */
    public Resolution cancel() {

        if (logger.isTraceEnabled()) {
            logger.trace("Handling cancel event");
        }
        return resolutionToScripts();
    }

    /**
     *
     * @return
     */
    private RedirectResolution resolutionToScripts() {

        RedirectResolution redirectResolution = new RedirectResolution(PostHarvestScriptsActionBean.class);
        if (targetType != null) {
            redirectResolution = redirectResolution.addParameter("targetType", targetType);
        }
        if (!StringUtils.isBlank(targetUrl)) {
            redirectResolution = redirectResolution.addParameter("targetUrl", targetUrl);
        }
        return redirectResolution;
    }

    /**
     *
     * @throws DAOException
     */
    @ValidationMethod(on = {"save"})
    public void validateSave() throws DAOException {

        if (getUser() == null || !getUser().isAdministrator()) {
            addGlobalValidationError("You are not authorized for this operation!");
            return;
        }

        if (StringUtils.isBlank(title)) {
            addGlobalValidationError("Title must not be blank!");
        } else if (title.length() > 255) {
            addGlobalValidationError("Title must be no longer than 255 characters!");
        }

        if (StringUtils.isBlank(script)) {
            addGlobalValidationError("Script must not be blank!");
        }

        getContext().setSourcePageResolution(new ForwardResolution(SCRIPT_JSP));
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
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
     * @return the script
     */
    public String getScript() {
        return script;
    }

    /**
     * @param script
     *            the script to set
     */
    public void setScript(String script) {
        this.script = script;
    }

    /**
     * @return the targetUrl
     */
    public String getTargetUrl() {
        return targetUrl;
    }

    /**
     * @param targetUrl
     *            the targetUrl to set
     */
    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    /**
     * @return the targetType
     */
    public TargetType getTargetType() {
        return targetType;
    }

    /**
     * @param targetType
     *            the targetType to set
     */
    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }

    /**
     * @return the testSourceUrl
     */
    public String getTestSourceUrl() {
        return testSourceUrl;
    }

    /**
     * @param testSourceUrl
     *            the testSourceUrl to set
     */
    public void setTestSourceUrl(String testSourceUrl) {
        this.testSourceUrl = testSourceUrl;
    }

    /**
     *
     * @return
     */
    public List<String> getTypeUris() {
        return ApplicationCache.getTypeUris();
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active
     *            the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the ignoreMalformedSparql
     */
    public boolean isIgnoreMalformedSparql() {
        return ignoreMalformedSparql;
    }

    /**
     * @param ignoreMalformedSparql
     *            the ignoreMalformedSparql to set
     */
    public void setIgnoreMalformedSparql(boolean ignoreMalformedSparql) {
        this.ignoreMalformedSparql = ignoreMalformedSparql;
    }
}
