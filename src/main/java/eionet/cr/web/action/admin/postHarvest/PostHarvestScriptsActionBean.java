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

package eionet.cr.web.action.admin.postHarvest;

import java.util.HashSet;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.PostHarvestScriptDAO;
import eionet.cr.dto.PostHarvestScriptDTO;
import eionet.cr.dto.PostHarvestScriptDTO.TargetType;
import eionet.cr.util.Pair;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.security.CRUser;

/**
 *
 * @author Jaanus Heinlaid
 */
@UrlBinding("/admin/postHarvestScripts")
public class PostHarvestScriptsActionBean extends AbstractActionBean {

    /** action type, indicating if scripts are Cut or copied. */
    public enum ActionType {
        COPY, CUT
    };

    /** */
    private static final String SEARCH_JSP = "/pages/admin/postHarvestScripts/searchScripts.jsp";
    private static final String SCRIPTS_JSP = "/pages/admin/postHarvestScripts/scripts.jsp";
    public static final String SCRIPTS_CONTAINER_JSP = "/pages/admin/postHarvestScripts/scriptsContainer.jsp";

    /** */
    private TargetType targetType;
    private String targetUrl;

    /** shows if scripts selected for copy/cut for this target type */
    // private boolean scriptsExistInClipBoard = false;

    /** */
    private List<Pair<String, Integer>> targets;
    private List<PostHarvestScriptDTO> scripts;
    private List<Tab> tabs;

    /** */
    private List<Integer> selectedIds;

    /**
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution list() throws DAOException {

        if (targetType != null) {
            targets = DAOFactory.get().getDao(PostHarvestScriptDAO.class).listTargets(targetType);
            if (targetUrl != null && !targetsContain(targetUrl)) {
                targets.add(0, new Pair<String, Integer>(targetUrl, Integer.valueOf(0)));
            }
        }

        if ((targetType == null && StringUtils.isBlank(targetUrl)) || (targetType != null && !StringUtils.isBlank(targetUrl))) {
            scripts = DAOFactory.get().getDao(PostHarvestScriptDAO.class).list(targetType, targetUrl);
        }

        return new ForwardResolution(SCRIPTS_CONTAINER_JSP);
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution delete() throws DAOException {

        if (selectedIds != null && !selectedIds.isEmpty()) {
            DAOFactory.get().getDao(PostHarvestScriptDAO.class).delete(selectedIds);
            addSystemMessage("Selected script(s) successfully deleted!");
        }

        return list();
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution activateDeactivate() throws DAOException {

        if (selectedIds != null && !selectedIds.isEmpty()) {
            DAOFactory.get().getDao(PostHarvestScriptDAO.class).activateDeactivate(selectedIds);
            addSystemMessage("Selected script(s) successfully activated/deactivated!");
        }

        return list();
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution moveUp() throws DAOException {

        DAOFactory.get().getDao(PostHarvestScriptDAO.class).move(targetType, targetUrl, new HashSet(selectedIds), -1);
        return list();
    }

    /**
     * Handler for the "search" event.
     *
     * @return
     * @throws DAOException
     */
    public Resolution search() throws DAOException {

        CRUser user = getUser();
        if (user != null && user.isAdministrator()) {
            String searchText = getContext().getRequestParameter("search");
            if (searchText != null) {
                searchText = searchText.trim();
                if (!searchText.isEmpty()) {
                    if (searchText.length() >= 3) {
                        scripts = DAOFactory.get().getDao(PostHarvestScriptDAO.class).search(searchText);
                    } else {
                        addCautionMessage("Search text must be at least 3 characters long!");
                    }
                }
            }
        } else {
            addGlobalValidationError("User not an administrator!");
        }

        return new ForwardResolution(SEARCH_JSP);
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution moveDown() throws DAOException {

        DAOFactory.get().getDao(PostHarvestScriptDAO.class).move(targetType, targetUrl, new HashSet(selectedIds), 1);
        return list();
    }

    /**
     *
     * @throws DAOException
     */
    @ValidationMethod(on = {"delete", "activateDeactivate", "moveUp", "moveDown", "cut", "copy"})
    public void validateMove() throws DAOException {

        if (getUser() == null || !getUser().isAdministrator()) {
            addGlobalValidationError("You are not authorized for this operation!");
            return;
        }

        if (selectedIds == null || selectedIds.isEmpty()) {
            addGlobalValidationError("At least one script must be selected!");
        }

        getContext().setSourcePageResolution(list());
    }

    /**
     * Validates if paste is legal. Checks if the names are unique and admin has permissions
     *
     * @throws DAOException if DB operation fails
     */
    @ValidationMethod(on = {"paste"})
    public void validatePaste() throws DAOException {

        if (getUser() == null || !getUser().isAdministrator()) {
            addGlobalValidationError("You are not authorized for this operation!");
            return;
        }

        // PostHarvestScriptUtil.validateScripts(this, this.getSession(), targetType, targetUrl);

        TargetType clipboardTargetType = (TargetType) getSession().getAttribute(SCRIPTS_CLIPBOARD_TYPE);
        List<String> validationErros =
                PostHarvestScriptUtil.getValidateScriptErrors(getClipBoardScripts(), clipboardTargetType, targetType, targetUrl);
        addGlobalValidationErrors(validationErros);

        getContext().setSourcePageResolution(list());
    }

    /**
     * @return the targetType
     */
    public TargetType getTargetType() {
        return targetType;
    }

    /**
     * @param targetType the targetType to set
     */
    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }

    /**
     * @return the targets
     * @throws DAOException
     */
    public List<Pair<String, Integer>> getTargets() throws DAOException {

        return targets;
    }

    /**
     * @return the scripts
     */
    public List<PostHarvestScriptDTO> getScripts() {
        return scripts;
    }

    /**
     * @return the tabs
     */
    public List<Tab> getTabs() {

        if (tabs == null) {
            tabs = Tabs.generate(targetType);
        }
        return tabs;
    }

    /**
     * @return the targetUrl
     */
    public String getTargetUrl() {
        return targetUrl;
    }

    /**
     * @param targetUrl the targetUrl to set
     */
    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    /**
     * @param selectedIds the selectedIds to set
     */
    public void setSelectedIds(List<Integer> selectedIds) {
        this.selectedIds = selectedIds;
    }

    /**
     *
     * @return
     */
    public String getPageToRender() {

        return SCRIPTS_JSP;
    }

    /**
     *
     * @param url
     * @return
     */
    private boolean targetsContain(String url) {

        if (targets != null && !targets.isEmpty()) {
            for (Pair<String, Integer> pair : targets) {
                if (url.equals(pair.getLeft())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Action for Cutting the scripts.
     *
     * @return Original page
     * @throws DAOException if action fails.
     */
    public Resolution cut() throws DAOException {
        getSession().setAttribute(PostHarvestScriptActionBean.SCRIPTS_CLIPBOARD_ACTION, ActionType.CUT);
        reInitClipBoard();
        return list();
    }

    /**
     * Action for Copying the scripts.
     *
     * @return Original page
     * @throws DAOException if action fails.
     */
    public Resolution copy() throws DAOException {
        getSession().setAttribute(PostHarvestScriptActionBean.SCRIPTS_CLIPBOARD_ACTION, ActionType.COPY);
        reInitClipBoard();
        return list();
    }

    /**
     * Action for pasting the scripts.
     *
     * @return Original page
     * @throws DAOException if action fails.
     */
    public Resolution paste() throws DAOException {
        // PostHarvestScriptUtil.pasteScripts(getSession(), targetType, targetUrl);
        ActionType actionType = (ActionType) getSession().getAttribute(SCRIPTS_CLIPBOARD_ACTION);
        PostHarvestScriptUtil.pasteScripts(getClipBoardScripts(), actionType, targetType, targetUrl);

        return list();
    }

    /**
     * Scripts selected to clipboard.
     *
     * @return Lsit of Script data objects
     */

    public List<PostHarvestScriptDTO> getClipBoardScripts() {
        return (List<PostHarvestScriptDTO>) getSession().getAttribute(PostHarvestScriptActionBean.SCRIPTS_CLIPBOARD);
    }

    /**
     * Reinitializes the selected scripts buffer.
     *
     * @throws DAOException if DB operation fails
     */
    private void reInitClipBoard() throws DAOException {
        List<PostHarvestScriptDTO> selectedScripts =
                DAOFactory.get().getDao(PostHarvestScriptDAO.class).getScriptsByIds(selectedIds);

        getSession().setAttribute(PostHarvestScriptActionBean.SCRIPTS_CLIPBOARD, selectedScripts);
        getSession().setAttribute(PostHarvestScriptActionBean.SCRIPTS_CLIPBOARD_TYPE, targetType);
    }

    /**
     * True if something is selected for paste and the type is correct.
     *
     * @return boolean
     */
    public boolean isPastePossible() {

        List<PostHarvestScriptDTO> clipboardScripts = getClipBoardScripts();
        TargetType clipboardTargetType = (TargetType) getSession().getAttribute(SCRIPTS_CLIPBOARD_TYPE);

        return PostHarvestScriptUtil.isPastePossible(clipboardScripts, clipboardTargetType, targetType);

    }

}
