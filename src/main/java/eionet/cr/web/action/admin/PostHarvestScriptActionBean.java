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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.PostHarvestScriptDAO;
import eionet.cr.dto.PostHarvestScriptDTO;
import eionet.cr.dto.PostHarvestScriptDTO.Type;
import eionet.cr.web.action.AbstractActionBean;

/**
 *
 * @author Jaanus Heinlaid
 */
@UrlBinding("/admin/postHarvestScript")
public class PostHarvestScriptActionBean extends AbstractActionBean {

    /** */
    private static final String SCRIPT_JSP = "/pages/admin/postHarvestScript.jsp";

    /** */
    private static final String QUERY_PARAM = "query";

    /** */
    public Type targetType = Type.HARVEST_SOURCE;

    /** */
    private PostHarvestScriptDTO scriptDTO;

    /** */
    private String uri;
    private List<String> queries;

    /**
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution view() throws DAOException {

        if (!StringUtils.isBlank(uri)){

            PostHarvestScriptDTO dto = DAOFactory.get().getDao(PostHarvestScriptDAO.class).get(targetType, uri);
            if (dto!=null){
                queries = dto.getQueries();
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

        PostHarvestScriptDTO dto = PostHarvestScriptDTO.create(targetType, uri);
        dto.addQueries(getQueries());

        DAOFactory.get().getDao(PostHarvestScriptDAO.class).save(dto);

        return new ForwardResolution(SCRIPT_JSP);
    }

    /**
     * @return the targetType
     */
    public Type getTargetType() {
        return targetType;
    }

    /**
     * @param targetType
     *            the targetType to set
     */
    public void setTargetType(Type targetType) {
        this.targetType = targetType;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri
     *            the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the queries
     */
    public List<String> getQueries() {

        if (queries == null) {

            queries = new ArrayList<String>();
            HashMap<Integer, String> queryMap = new HashMap<Integer, String>();
            HttpServletRequest request = getContext().getRequest();
            Map<String, String[]> paramsMap = request.getParameterMap();

            if (paramsMap != null && !paramsMap.isEmpty()) {

                for (Map.Entry<String, String[]> entry : paramsMap.entrySet()) {

                    String paramName = entry.getKey();
                    if (paramName.startsWith(QUERY_PARAM)) {

                        int queryIndex = NumberUtils.toInt(paramName.substring(QUERY_PARAM.length()));
                        if (queryIndex > 0) {

                            String query = entry.getValue()[0];
                            if (!StringUtils.isBlank(query)) {
                                queryMap.put(queryIndex, query);
                            }
                        }
                    }
                }
            }

            if (!queryMap.isEmpty()) {

                ArrayList<Integer> queryIndexes = new ArrayList<Integer>(queryMap.keySet());
                Collections.sort(queryIndexes);
                for (Integer queryIndex : queryIndexes) {
                    queries.add(queryMap.get(queryIndex));
                }
            }
        }

        return queries;
    }
}
