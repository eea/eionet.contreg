package eionet.cr.web.action.admin.harvestscripts;

import java.util.ArrayList;
import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestScriptDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestScriptDTO;
import eionet.cr.dto.HarvestScriptDTO.TargetType;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.web.action.admin.harvestscripts.HarvestScriptsActionBean.ActionType;

/**
 * Common utility methods for handling Post Harvest Scripts.
 *
 * @author Kaido Laine
 */
public final class HarvestScriptUtil {
    /**
     * to prevent initialization.
     */
    private HarvestScriptUtil() {
    }

    /**
     * Validates if any of the scripts exist in the target source/type.
     *
     * @param scriptsInClipBoard
     *            scripts in clipboard
     * @param targetTypeClipBoard
     *            type of clipboard scripts
     * @param targetType
     *            current target type
     * @param targetUrl
     *            target url (source or RDF type)
     * @return list of error messages, if no messages, empty array
     * @throws DAOException
     *             if validation fails
     */
    public static List<String> getValidateScriptErrors(List<HarvestScriptDTO> scriptsInClipBoard,
            HarvestScriptDTO.TargetType targetTypeClipBoard, HarvestScriptDTO.TargetType targetType, String targetUrl)
            throws DAOException {

        List<String> errors = new ArrayList<String>();

        if (scriptsInClipBoard == null || scriptsInClipBoard.isEmpty()) {
            errors.add("At least one script must be selected to be pasted!");
        }

        if (scriptsInClipBoard != null && scriptsInClipBoard.size() > 0 && !typesMatchTogether(targetType, targetTypeClipBoard)) {
            errors.add("The script(s) cannot be pasted to this target");
        }

        if (targetType.equals(TargetType.SOURCE) && targetUrl != null) {
            HarvestSourceDTO dto = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(targetUrl);
            if (dto == null) {
                errors.add("No source by this URL was found: " + targetUrl);
            }
        }

        return errors;
    }

    /**
     * Pastes scripts to given targetUrl or null if all sources scripts.
     *
     * @param scriptsInClipBoard
     *            selected script(s)
     * @param actionType
     *            action type: cut or copy
     * @param targetType
     *            target type: RDF TYpe or source
     * @param targetUrl
     *            target url or null if all sources scripts
     * @throws DAOException
     *             if pasting fails
     */
    public static void pasteScripts(List<HarvestScriptDTO> scriptsInClipBoard, ActionType actionType,
            HarvestScriptDTO.TargetType targetType, String targetUrl) throws DAOException {

        DAOFactory.get().getDao(HarvestScriptDAO.class).addScripts(targetType, targetUrl, scriptsInClipBoard);

        if (actionType.equals(ActionType.CUT)) {
            List<Integer> scriptIds = new ArrayList<Integer>();
            for (HarvestScriptDTO script : scriptsInClipBoard) {
                // LOGGER.debug("Adding script ID to deletable " + script.getId());
                scriptIds.add(script.getId());
            }

            DAOFactory.get().getDao(HarvestScriptDAO.class).delete(scriptIds);
        }
    }

    /**
     * Checks if selected scripts an be pasted on the active page (determines the type).
     *
     * @param scriptsInClipBoard
     *            Scripts buffered
     * @param targetTypeClipBoard
     *            target type of buffered scripts
     * @param targetType
     *            current target type
     * @return true if scripts can be pasted
     */
    public static boolean isPastePossible(List<HarvestScriptDTO> scriptsInClipBoard,
            HarvestScriptDTO.TargetType targetTypeClipBoard, HarvestScriptDTO.TargetType targetType) {

        // if null - it is an all sources script - if any of the 2 target types: clipboard and page is null and the other is source
        // , actions are allowed
        // if both nulls: allowed as well - copy paste to all sources scripts
        return scriptsInClipBoard != null && scriptsInClipBoard.size() > 0 && typesMatchTogether(targetType, targetTypeClipBoard);
    }

    /**
     * Determines if script types are correct - if any or both are null's one or both are Source scripts.
     *
     * @param targetType
     *            target type in page
     * @param targetTypeClipBoard
     *            target type in clipboard
     * @return true if match
     */
    private static boolean typesMatchTogether(HarvestScriptDTO.TargetType targetType,
            HarvestScriptDTO.TargetType targetTypeClipBoard) {

        return !((targetType == null && (targetTypeClipBoard != null && !targetTypeClipBoard
                .equals(HarvestScriptDTO.TargetType.SOURCE)))
                || (targetTypeClipBoard == null && (targetType != null && !targetType
                        .equals(HarvestScriptDTO.TargetType.SOURCE))) || (targetTypeClipBoard != null && targetType != null && !targetType
                .equals(targetTypeClipBoard)));
    }
}
