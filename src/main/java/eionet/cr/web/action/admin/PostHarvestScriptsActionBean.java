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
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dto.PostHarvestScriptDTO;
import eionet.cr.dto.PostHarvestScriptDTO.Type;
import eionet.cr.dto.SourcePostHarvestScriptDTO;
import eionet.cr.util.SortOrder;
import eionet.cr.web.action.AbstractActionBean;

/**
 *
 * @author Jaanus Heinlaid
 */
@UrlBinding("/admin/postHarvestScripts")
public class PostHarvestScriptsActionBean extends AbstractActionBean{

    /** */
    private static final String LIST_JSP = "/pages/admin/postHarvestScripts.jsp";

    /** */
    private List<PostHarvestScriptDTO> scripts;

    /** */
    protected int page = 1;
    protected String dir = SortOrder.ASCENDING.toString();

    /** */
    private Type targetType = Type.HARVEST_SOURCE;

    /**
     *
     * @return
     */
    @DefaultHandler
    public Resolution list(){

        scripts = new ArrayList<PostHarvestScriptDTO>();

        // TODO - generating dummy data here, fix later for real data
        if (getTargetType().equals(Type.HARVEST_SOURCE)){
            for (int i=0; i<23; i++){
                PostHarvestScriptDTO dto = new SourcePostHarvestScriptDTO("http://source" + i + ".ee");
                for (int j=0; j<(i+1); j++){
                    dto.addQuery("query" + (j+1));
                }
                scripts.add(dto);
            }
        }
        else{
            for (int i=0; i<23; i++){
                PostHarvestScriptDTO dto = new SourcePostHarvestScriptDTO("http://type" + i + ".ee");
                for (int j=0; j<(i+1); j++){
                    dto.addQuery("query" + (j+1));
                }
                scripts.add(dto);
            }
        }

        return new ForwardResolution(LIST_JSP);
    }

    /**
     * @return the scripts
     */
    public List<PostHarvestScriptDTO> getScripts() {
        return scripts;
    }

    /**
     * @return the page
     */
    public int getPage() {
        return page;
    }

    /**
     * @param page the page to set
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * @return the dir
     */
    public String getDir() {
        return dir;
    }

    /**
     * @param dir the dir to set
     */
    public void setDir(String dir) {
        this.dir = dir;
    }

    /**
     * @return the targetType
     */
    public Type getTargetType() {
        return targetType;
    }

    /**
     * @param targetType the targetType to set
     */
    public void setTargetType(Type targetType) {
        this.targetType = targetType;
    }
}
