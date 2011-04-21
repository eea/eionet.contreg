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
 * Agency.  Portions created by Tieto Estonia are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Enriko Käsper, Tieto Estonia
 */
package eionet.cr.web.action;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dto.TagDTO;
import eionet.cr.web.util.ApplicationCache;

/**
 *
 * @author <a href="mailto:enriko.kasper@tieto.com">Enriko Käsper</a>
 *
 */

@UrlBinding("/json/{$event}")
public class JsonActionBean extends AbstractActionBean{

    @DefaultHandler
    public Resolution tags()
    {
        List<TagDTO> tagList = ApplicationCache.getTagCloudSortedByName(0);
        String queryParam = this.getContext().getRequestParameter("query");

        List<String> tagNameList = new ArrayList<String>(tagList.size());
        for (TagDTO tagObj : tagList) {
            if(queryParam == null ||
                    tagObj.getTag().toLowerCase().startsWith(queryParam.toLowerCase()))
            tagNameList.add(tagObj.getTag());
        }
        JSONArray jsonArray = JSONArray.fromObject( tagNameList );

        Map<String,Object> resultMap = new HashMap<String, Object>();
        resultMap.put("query", queryParam == null ? "" : queryParam);
        resultMap.put("suggestions", jsonArray);

        JSONObject jsonObject = JSONObject.fromObject( resultMap );

        return new StreamingResolution("text", new StringReader(jsonObject.toString()));
    }
}
