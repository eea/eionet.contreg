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

import java.util.ArrayList;
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

/**
 *
 * @author Jaanus Heinlaid
 */
@UrlBinding("/admin/postHarvestScripts")
public class PostHarvestScriptsActionBean extends AbstractActionBean {

    /** */
    //private static final String TARGETS_JSP = "/pages/admin/postHarvestScripts/targets.jsp";
    private static final String SCRIPTS_JSP = "/pages/admin/postHarvestScripts/scripts.jsp";
    private static final String SCRIPTS_CONTAINER_JSP = "/pages/admin/postHarvestScripts/scriptsContainer.jsp";

    /** */
    private TargetType targetType;
    private String targetUrl;

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

        if (targetType != null){
            targets = DAOFactory.get().getDao(PostHarvestScriptDAO.class).listTargets(targetType);
        }

        if ((targetType==null && StringUtils.isBlank(targetUrl)) || (targetType!=null && !StringUtils.isBlank(targetUrl))){
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
    @ValidationMethod(on = {"delete", "activateDeactivate", "moveUp", "moveDown"})
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

            tabs = new ArrayList<Tab>();
            tabs.add(new Tab("All-source scripts", getUrlBinding(), "Scripts to run for all sources", targetType == null));
            tabs.add(new Tab("Source-specific scripts", getUrlBinding() + "?targetType=" + TargetType.SOURCE,
                    "Scripts to run for specific sources", targetType != null && targetType.equals(TargetType.SOURCE)));
            tabs.add(new Tab("Type-specific scripts", getUrlBinding() + "?targetType=" + TargetType.TYPE,
                    "Scripts to run for specific types", targetType != null && targetType.equals(TargetType.TYPE)));
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
     * @param targetUrl
     *            the targetUrl to set
     */
    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    /**
     * @param selectedIds
     *            the selectedIds to set
     */
    public void setSelectedIds(List<Integer> selectedIds) {
        this.selectedIds = selectedIds;
    }
    
    /**
     * 
     * @return
     */
    public String getPageToRender(){
    	
    	return SCRIPTS_JSP;
    }

    /**
     * @author Jaanus Heinlaid
     */
    public class Tab {

        /** */
        private String title;
        private String href;
        private String hint;
        private boolean selected;

        /**
         * @param title
         * @param href
         * @param hint
         * @param selected
         */
        public Tab(String title, String href, String hint, boolean selected) {
            this.title = title;
            this.href = href;
            this.hint = hint;
            this.selected = selected;
        }

        /**
         * @return the title
         */
        public String getTitle() {
            return title;
        }

        /**
         * @return the href
         */
        public String getHref() {
            return href;
        }

        /**
         * @return the hint
         */
        public String getHint() {
            return hint;
        }

        /**
         * @return the selected
         */
        public boolean isSelected() {
            return selected;
        }
    }
}
