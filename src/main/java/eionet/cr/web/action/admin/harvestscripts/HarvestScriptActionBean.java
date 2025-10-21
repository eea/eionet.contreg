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

package eionet.cr.web.action.admin.harvestscripts;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.ExternalServiceDAO;
import eionet.cr.dao.HarvestScriptDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.ExternalServiceDTO;
import eionet.cr.dto.HarvestScriptDTO;
import eionet.cr.dto.HarvestScriptDTO.Phase;
import eionet.cr.dto.HarvestScriptDTO.TargetType;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.ScriptTemplateDTO;
import eionet.cr.dto.enums.HarvestScriptType;
import eionet.cr.filestore.ScriptTemplateDaoImpl;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.action.HarvestSourcesActionBean;
import eionet.cr.web.action.admin.harvestscripts.HarvestScriptsActionBean.ActionType;
import eionet.cr.web.util.ApplicationCache;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static eionet.cr.web.action.admin.harvestscripts.HarvestScriptsActionBean.SCRIPTS_CONTAINER_JSP;

/**
 * Action bean for dealing with a post-harvest script.
 *
 * @author Jaanus Heinlaid
 */
@UrlBinding("/admin/harvestScript")
public class HarvestScriptActionBean extends AbstractActionBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(HarvestSourcesActionBean.class);

    /** Default JSP to return to. */
    private static final String SCRIPT_JSP = "/pages/admin/harvestScripts/script.jsp";

    /** Default script to display to user. */
    private static final String DEFAULT_SCRIPT = "PREFIX cr: <http://cr.eionet.europa.eu/ontologies/contreg.rdf#>\n\n"
            + "INSERT INTO ?" + HarvestScriptParser.HARVESTED_SOURCE_VARIABLE + " {\n" + "    ?s cr:tag `bif:lower(?o)`\n"
            + "}\n" + "WHERE {\n" + "  GRAPH ?" + HarvestScriptParser.HARVESTED_SOURCE_VARIABLE + " {\n"
            + "    ?s <http://www.eea.europa.eu/portal_types/Article#themes> ?o\n" + "  }\n" + "}";

    /** The script id. */
    private int id;

    /** The script title. */
    private String title;

    /** The script content (i.e. the SPARQL query that is the content of the script). */
    private String script = DEFAULT_SCRIPT;

    /** The target URL. */
    private String targetUrl;

    /** The target type. */
    private TargetType targetType;

    /** The URL of source to test on. */
    private String testSourceUrl;

    /** Is the script activated? */
    private boolean active;

    /** The script's phase. See {@link HarvestScriptDTO.Phase}. */
    private HarvestScriptDTO.Phase phase;
    
    private HarvestScriptType type;
    
    private Integer externalServiceId;
    
    private String externalServiceParams;

    /** Should the script be run only once by the harvester? (alternative is to do until returned update count is 0). */
    private boolean runOnce = true;

    /** Should the bean ignore ill-formed SPARQL for this particular request? */
    private boolean ignoreMalformedSparql;

    /** The tabs to display in JSP. */
    private List<Tab> tabs;

    /** The back to target URL. */
    private String backToTargetUrl;

    /** Script test result list. */
    private List<Map<String, ObjectDTO>> testResults;

    /** Script test result list columns. */
    private List<String> testResultColumns;

    /** The actual executed test query after doing necessary processing. */
    private String executedTestQuery;

    /** Script test error if any. */
    private String testError;

    /** Copy-paste clipboard item id (relevant when copy-pasting scripts). */
    private int clipboardItemId;

    /** Is bulk paste from clipboard? */
    private boolean bulkPaste;

    /** URL to redirect to when Cancel pressed. Used when script page was reached (e.g.) from harvest source view page. */
    private String cancelUrl;

    /** Distinct predicates used in all triples in the graph identified by the target source URL. */
    List<String> sourceAllDistinctPredicates;

    /** Distinct predicates used in all reources whose rdf:type is the current script target type. */
    List<String> typeAllDistinctPredicates;

    /** Predicate to be used in relevant placeholder when generating script from a template. */
    String scriptPredicate;

    /** Available script templates. */
    private List<ScriptTemplateDTO> scriptTemplates;

    /** Current script template id. */
    private String scriptTemplateId;

    /**
     * ActionBean default handler.
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution defaultHandler() throws DAOException {

        if (id > 0) {
            HarvestScriptDTO dto = DAOFactory.get().getDao(HarvestScriptDAO.class).fetch(id);
            if (dto != null) {
                title = dto.getTitle();
                script = dto.getScript();
                targetUrl = dto.getTargetUrl();
                targetType = dto.getTargetType();
                active = dto.isActive();
                runOnce = dto.isRunOnce();
                phase = dto.getPhase();
                type = dto.getType();
                externalServiceId = dto.getExternalServiceId();
                externalServiceParams = dto.getExternalServiceParams();
            }
        }

        if (clipboardItemId > 0) {
            List<HarvestScriptDTO> clipboardScripts = getClipBoardScripts();

            for (HarvestScriptDTO clipboardScript : clipboardScripts) {
                if (clipboardScript.getId() == clipboardItemId) {
                    script = clipboardScript.getScript();
                    title = clipboardScript.getTitle();
                    break;
                }
            }
        }

        return new ForwardResolution(SCRIPTS_CONTAINER_JSP);
    }

    /**
     * Resolution after paste action.
     *
     * @return The same page
     * @throws DAOException
     *             if paste fails
     */
    public Resolution paste() throws DAOException {

        ActionType actionType = (ActionType) getSession().getAttribute(SCRIPTS_CLIPBOARD_ACTION);
        HarvestScriptUtil.pasteScripts(getClipBoardScripts(), actionType, targetType, targetUrl);

        addSystemMessage("Scripts successfully pasted!");

        // view the list of all scripts.
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Redirecting after paste");
        }
        return resolutionToScripts();
    }

    /**
     *
     * @return Resolution
     * @throws DAOException
     */
    public Resolution save() throws DAOException {

        // If id given, do save by the given id, otherwise do addition of brand new script.
        HarvestScriptDAO dao = DAOFactory.get().getDao(HarvestScriptDAO.class);
        if (id > 0) {
            dao.save(id, title, script, active, runOnce, phase, type, externalServiceId, externalServiceParams);
        } else {
            id = dao.insert(targetType, targetUrl, title, script, active, runOnce, phase, type, externalServiceId, 
                    externalServiceParams);
        }
        addSystemMessage("Script successfully saved!");

        // Depending on whether "Save" or "Save & close" was pressed, forward back to the same script's
        // view or to the list of all scripts.
        if (getContext().getRequestParameter("save").equalsIgnoreCase("Save & close")) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Redirecting after save & close");
            }
            return resolutionToScripts();
        } else {
            RedirectResolution redirResolution = null;
            try {
                redirResolution = new RedirectResolution(HarvestScriptActionBean.class).addParameter("id", id);
            } catch (NullPointerException npe) {
                // In unit-test runtime Stripes' OnwardResolution throws NPE from its constructor due to what seems to be a bug.
            }

            if (redirResolution != null) {

                if (StringUtils.isNotBlank(cancelUrl)) {
                    redirResolution.addParameter("cancelUrl", cancelUrl);
                }
                if (StringUtils.isNotBlank(testSourceUrl)) {
                    redirResolution.addParameter("testSourceUrl", testSourceUrl);
                }

                redirResolution.addParameter("scriptPredicate", scriptPredicate);
                redirResolution.addParameter("scriptTemplateId", scriptTemplateId);
            }

            return redirResolution;
        }
    }

    /**
     * Replaces the script and title fields with a template script and title. Title is replaced ONLY if empty.
     *
     * @return Resolution
     * @throws DAOException
     */
    public Resolution useTemplate() throws DAOException {

        if (validateAdministrator()) {
            if (StringUtils.isNotEmpty(scriptPredicate) && StringUtils.isNotEmpty(scriptTemplateId)) {
                ScriptTemplateDTO scriptTemplate = new ScriptTemplateDaoImpl().getScriptTemplate(scriptTemplateId);
                setScript(StringUtils.replace(scriptTemplate.getScript(), "[TABLECOLUMN]", "<" + scriptPredicate + ">"));

                if (StringUtils.isEmpty(title)) {
                    title = scriptTemplate.getName();
                    addSystemMessage("Script and title replaced with template!");
                } else {
                    addSystemMessage("Script replaced with template!");
                }
            } else {
                addWarningMessage("Both script template and predicate must be selected to add script from a template.");
            }
        }

        return new ForwardResolution(SCRIPTS_CONTAINER_JSP).addParameter("title", title).addParameter("script", script);
    }

    /**
     * Functionality to reload the templates and predicates list for new scripts.
     *
     * @return Resolution
     * @throws DAOException
     */
    public Resolution loadTemplatePredicates() throws DAOException {

        if (validateAdministrator()) {
            if (StringUtils.isEmpty(targetUrl)) {
                addWarningMessage("Target is not defined. Cannot load predicates for empty target.");
            }
        }

        return new ForwardResolution(SCRIPTS_CONTAINER_JSP);
    }

    /**
     * Adds from bulk paste.
     *
     * @return Resolution
     * @throws DAOException
     */
    public Resolution addFromBulkPaste() throws DAOException {
        return paste();
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution test() throws Exception {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Handling test event");
        }

        String harvestedSource = testSourceUrl;
        if (StringUtils.isBlank(harvestedSource)) {
            if (targetType != null && targetType.equals(TargetType.SOURCE)) {
                harvestedSource = targetUrl;
            }
        }

        String associatedType = targetType != null && targetType.equals(TargetType.TYPE) ? targetUrl : null;

        try {
            executedTestQuery = HarvestScriptParser.deriveConstruct(script, harvestedSource, associatedType);
        } catch (ScriptParseException e) {
            addWarningMessage(e.toString());
        }
        LOGGER.debug("Executing derived CONSTRUCT query: " + executedTestQuery);
        // logger.debug("Using " + harvestedSource + " as the default graph");

        try {
            testResults =
                    DAOFactory.get().getDao(HarvestScriptDAO.class)
                            .test(executedTestQuery, targetType, targetUrl, harvestedSource);
        } catch (DAOException e) {
            testError = e.getMessage();
        }
        return new ForwardResolution(SCRIPTS_CONTAINER_JSP);
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution cancel() throws DAOException {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Handling cancel event");
        }

        if (StringUtils.isNotBlank(cancelUrl)) {
            return new RedirectResolution(cancelUrl);
        }

        if (StringUtils.isBlank(targetUrl) && !StringUtils.isBlank(backToTargetUrl)) {
            targetUrl = backToTargetUrl;
        }

        if (targetType != null && targetType.equals(TargetType.SOURCE) && !StringUtils.isBlank(targetUrl)) {
            HarvestSourceDTO dto = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(targetUrl);
            if (dto == null) {
                targetUrl = null;
            }
        }

        return resolutionToScripts();
    }

    /**
     *
     * @return
     */
    private RedirectResolution resolutionToScripts() {

        RedirectResolution redirectResolution = new RedirectResolution(HarvestScriptsActionBean.class);
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

        if (!validateAdministrator()) {
            return;
        }

        validateTargetType();

        if (StringUtils.isBlank(title)) {
            addGlobalValidationError("Title must not be blank!");
        } else if (title.length() > 255) {
            addGlobalValidationError("Title must be no longer than 255 characters!");
        }

        if (StringUtils.isBlank(script)) {
            addGlobalValidationError("Script must not be blank!");
        }
        
        if (HarvestScriptType.PUSH.equals(type) && externalServiceId == null) {
            addGlobalValidationError("Please select an external service!");
        }

        getContext().setSourcePageResolution(new ForwardResolution(SCRIPTS_CONTAINER_JSP));
    }

    private void validateTargetType() throws DAOException {
        if (targetType != null) {
            if (StringUtils.isBlank(targetUrl)) {
                addGlobalValidationError("Target " + targetType.toString().toLowerCase() + " must not be blank!");
            } else if (targetType.equals(TargetType.SOURCE)) {
                HarvestSourceDTO dto = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(targetUrl);
                if (dto == null) {
                    addGlobalValidationError("No source by this URL was found: " + targetUrl);
                    targetUrl = null;
                }
            }
        }
    }

    private boolean validateAdministrator() {
        if (getUser() == null || !getUser().isAdministrator()) {
            addGlobalValidationError("You are not authorized for this operation!");
            return false;
        }
        return true;
    }

    /**
     * Validates bulk paste
     */
    @ValidationMethod(on = {"addFromBulkPaste"})
    public void validateAddFromBulkPaste() throws DAOException {
        if (!validateAdministrator()) {
            return;
        }
        validateTargetType();

        getContext().setSourcePageResolution(new ForwardResolution(SCRIPTS_CONTAINER_JSP).addParameter("bulkPaste", true));
    }

    /**
     * Validates if the script(s) in the buffer can be pasted.
     *
     * @throws DAOException
     *             if validating fails.
     */
    @ValidationMethod(on = {"paste"})
    public void validatePaste() throws DAOException {
        if (getUser() == null || !getUser().isAdministrator()) {
            addGlobalValidationError("You are not authorized for this operation!");
            return;
        }

        // if targetUrl is not selected - raise error, not incldued in the common validation method as in scripts page
        // the url may be null because of all sources scripts
        if (StringUtils.isBlank(targetUrl)) {
            addGlobalValidationError("Target URL has to be selected");
        } else {
            // no need to process the other checks to avoid unnecessary error messages
            TargetType clipboardTargetType = (TargetType) getSession().getAttribute(SCRIPTS_CLIPBOARD_TYPE);
            List<String> validationErros =
                    HarvestScriptUtil.getValidateScriptErrors(getClipBoardScripts(), clipboardTargetType, targetType,
                            targetUrl);

            addGlobalValidationErrors(validationErros);
        }
        getContext().setSourcePageResolution(new ForwardResolution(SCRIPTS_CONTAINER_JSP));
    }

    /**
     *
     * @throws DAOException
     */
    @ValidationMethod(on = {"test"})
    public void validateTest() throws DAOException {

        if (getUser() == null || !getUser().isAdministrator()) {
            addGlobalValidationError("You are not authorized for this operation!");
            return;
        }

        if (StringUtils.isBlank(script)) {
            addGlobalValidationError("Script must not be blank!");
        }

        // If harvested-source variable present in the script, then test source is mandatory
        // (because the former will be substituted by the latter).
        String harvestedSourceVariable = "?" + HarvestScriptParser.HARVESTED_SOURCE_VARIABLE;
        if (HarvestScriptParser.containsToken(script, harvestedSourceVariable)) {
            String testSource = targetType != null && targetType.equals(TargetType.SOURCE) ? targetUrl : testSourceUrl;
            if (StringUtils.isBlank(testSource)) {
                addGlobalValidationError("Test source must not be blank when using " + harvestedSourceVariable + " in the script!");
            }
        }

        getContext().setSourcePageResolution(new ForwardResolution(SCRIPTS_CONTAINER_JSP));
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
     *
     * @return
     */
    public String getPageToRender() {

        return SCRIPT_JSP;
    }

    /**
     *
     * @return
     */
    public String getBackToTargetUrl() {
        return backToTargetUrl;
    }

    /**
     *
     * @param backToTargetUrl
     */
    public void setBackToTargetUrl(String backToTargetUrl) {
        this.backToTargetUrl = backToTargetUrl;
    }

    /**
     *
     * @return
     */
    public List<Map<String, ObjectDTO>> getTestResults() {
        return testResults;
    }

    /**
     *
     * @return
     */
    public List<String> getTestResultColumns() {

        if (testResultColumns == null) {

            testResultColumns = new ArrayList<String>();
            if (testResults != null && !testResults.isEmpty()) {

                Map<String, ObjectDTO> firstRow = testResults.get(0);
                for (String key : firstRow.keySet()) {
                    testResultColumns.add(key);
                }
            }
        }
        return testResultColumns;
    }

    /**
     * @return the executedTestQuery
     */
    public String getExecutedTestQuery() {
        return executedTestQuery;
    }

    /**
     * @return the testError
     */
    public String getTestError() {
        return testError;
    }

    /**
     *
     * @return
     */
    public String getHarvestedSourceVariable() {
        return HarvestScriptParser.HARVESTED_SOURCE_VARIABLE;
    }

    /**
     *
     * @return
     */
    public String getAssociatedTypeVariable() {
        return HarvestScriptParser.ASSOCIATED_TYPE_VARIABLE;
    }

    /**
     * @return the cancelUrl
     */
    public String getCancelUrl() {
        return cancelUrl;
    }

    /**
     * @param cancelUrl
     *            the cancelUrl to set
     */
    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

    /**
     * True if something is selected for this target type.
     *
     * @return true if scripts selected
     */
    public boolean isPastePossible() {
        List<HarvestScriptDTO> clipboardScripts = getClipBoardScripts();
        TargetType clipboardTargetType = (TargetType) getSession().getAttribute(SCRIPTS_CLIPBOARD_TYPE);

        return HarvestScriptUtil.isPastePossible(clipboardScripts, clipboardTargetType, targetType);
    }

    /**
     * Scripts selected for copy/cut - pasting.
     *
     * @return List of script data objects
     */

    @SuppressWarnings("unchecked")
    public List<HarvestScriptDTO> getClipBoardScripts() {
        return (List<HarvestScriptDTO>) getSession().getAttribute(SCRIPTS_CLIPBOARD);
    }

    public int getClipboardItemId() {
        return clipboardItemId;
    }

    public void setClipboardItemId(int clipboardItemId) {
        this.clipboardItemId = clipboardItemId;
    }

    public boolean isBulkPaste() {
        return bulkPaste;
    }

    public void setBulkPaste(boolean bulkPaste) {
        this.bulkPaste = bulkPaste;
    }

    /**
     * Returns distinct list of predicates associated to a source.
     *
     * @return
     * @throws DAOException
     */
    public List<String> getSourceAllDistinctPredicates() throws DAOException {
        if (sourceAllDistinctPredicates == null) {
            sourceAllDistinctPredicates =
                    DAOFactory.get().getDao(HarvestSourceDAO.class).getSourceAllDistinctPredicates(targetUrl);
        }
        return sourceAllDistinctPredicates;
    }

    /**
     * @return the scriptTemplates
     */
    public List<ScriptTemplateDTO> getScriptTemplates() {
        if (scriptTemplates == null) {
            scriptTemplates = new ScriptTemplateDaoImpl().getScriptTemplates();
        }
        return scriptTemplates;
    }

    /**
     * Returns distinct list of predicates associated to a type.
     *
     * @return
     * @throws DAOException
     */
    public List<String> getTypeAllDistinctPredicates() throws DAOException {
        if (typeAllDistinctPredicates == null) {
            typeAllDistinctPredicates = DAOFactory.get().getDao(HarvestSourceDAO.class).getTypeAllDistinctPredicates(targetUrl);
        }
        return typeAllDistinctPredicates;
    }

    /**
     * Gets the script predicate.
     *
     * @return the script predicate
     */
    public String getScriptPredicate() {
        return scriptPredicate;
    }

    /**
     * Sets the script predicate.
     *
     * @param scriptPredicate the new script predicate
     */
    public void setScriptPredicate(String scriptPredicate) {
        this.scriptPredicate = scriptPredicate;
    }

    /**
     * Gets the script template id.
     *
     * @return the script template id
     */
    public String getScriptTemplateId() {
        return scriptTemplateId;
    }

    /**
     * Sets the script template id.
     *
     * @param scriptTemplateId the new script template id
     */
    public void setScriptTemplateId(String scriptTemplateId) {
        this.scriptTemplateId = scriptTemplateId;
    }

    /**
     * @return the phase
     */
    public HarvestScriptDTO.Phase getPhase() {
        return phase;
    }

    /**
     * @param phase the phase to set
     */
    public void setPhase(HarvestScriptDTO.Phase phase) {
        this.phase = phase;
    }

    /**
     * Returns a list of possible harvest phases.
     * @return the list
     */
    public List<Phase> getPossiblePhases() {
        return Arrays.asList(HarvestScriptDTO.Phase.values());
    }
    
    public List<ExternalServiceDTO> getExternalServices() throws DAOException {
        return DAOFactory.get().getDao(ExternalServiceDAO.class).getExternalServices();
    }

    /**
     * Returns a list of possible harvest script types.
     * @return the list
     */
    public List<HarvestScriptType> getPossibleTypes() {
        return Arrays.asList(HarvestScriptType.values());
    }

    public Integer getExternalServiceId() {
        return externalServiceId;
    }

    public void setExternalServiceId(Integer externalServiceId) {
        this.externalServiceId = externalServiceId;
    }

    public String getExternalServiceParams() {
        return externalServiceParams;
    }

    public void setExternalServiceParams(String externalServiceParams) {
        this.externalServiceParams = externalServiceParams;
    }

    public HarvestScriptType getType() {
        return type;
    }

    public void setType(HarvestScriptType type) {
        this.type = type;
    }
}
