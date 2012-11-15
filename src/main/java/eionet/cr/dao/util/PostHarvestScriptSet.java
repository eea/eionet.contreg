package eionet.cr.dao.util;

import java.util.List;

import eionet.cr.dto.PostHarvestScriptDTO;

/**
 *
 * Container class for Post Harvest scripts related to one Type or source.
 * Includes helper methods for changing the scripts order etc.
 *
 */
public class PostHarvestScriptSet {
    /** private script container. */
    private List<PostHarvestScriptDTO> scripts;

    /** count of scripts for the type/url. */
    private int scriptCount;

    /**
     * Initializes the container based on script lists.
     * @param phScripts Post harvest scripts list
     */
    public PostHarvestScriptSet(List<PostHarvestScriptDTO> phScripts) {
        this.scripts = phScripts;
        this.scriptCount = phScripts.size();
    }


    /**
     * Returns Post harvest script by given position.
     * @param position position number (not the array position)
     * @return Matching Post harvest script. Null if no script with this position
     */
    public PostHarvestScriptDTO getScriptByPosition(int position) {
        for (PostHarvestScriptDTO script :  scripts)
            if (script.getPosition() == position) {
                return script;
            }
        return null;
    }

    /**
     * Returns position of the last script.
     *
     * @return int
     */
    public int getMaxPosition() {
        return scripts.get(scriptCount - 1).getPosition();
    }

    /**
     * Returns position of the first script.
     *
     * @return int
     */
    public int getMinPosition() {
        return scripts.get(0).getPosition();
    }
}
