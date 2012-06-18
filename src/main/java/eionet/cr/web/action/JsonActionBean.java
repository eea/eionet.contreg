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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.TagDTO;
import eionet.cr.web.util.ApplicationCache;

/**
 * 
 * @author <a href="mailto:enriko.kasper@tieto.com">Enriko Käsper</a>
 * 
 */

@UrlBinding("/json/{$event}.action")
public class JsonActionBean extends AbstractActionBean {

    /** */
    private static final Logger LOGGER = Logger.getLogger(JsonActionBean.class);

    /**
     * 
     * @return
     */
    @DefaultHandler
    public Resolution defaultHandler() {

        return createSuggestionsResolution(new ArrayList<String>(), "");
    }

    /**
     * 
     * @return
     */
    public Resolution tags() {

        List<TagDTO> tagList = ApplicationCache.getTagCloudSortedByName(0);
        String query = getContext().getRequestParameter("query");

        List<String> matchingTags = new ArrayList<String>(tagList.size());
        for (TagDTO tagDTO : tagList) {

            String tag = tagDTO.getTag();
            if (StringUtils.isBlank(query) || StringUtils.startsWithIgnoreCase(tag, query)) {
                matchingTags.add(tag);
            }
        }

        return createSuggestionsResolution(matchingTags, query);
    }

    /**
     * 
     * @return
     * @throws DAOException
     */
    public Resolution harvestSources() throws DAOException {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Getting harvest source suggestions ...");
        }

        long startTime = System.currentTimeMillis();
        String query = getContext().getRequestParameter("query");
        List<String> sourceUrls = DAOFactory.get().getDao(HarvestSourceDAO.class).filter(query, 101, 0);
        if (sourceUrls.size() == 101) {
            sourceUrls.set(100, "Narrow your search for more ...");
        } else if (sourceUrls.isEmpty()) {
            sourceUrls.add("No suggestions found!");
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Harvest source suggestions retrieved in " + (System.currentTimeMillis() - startTime) + " ms");
        }

        return createSuggestionsResolution(sourceUrls, query);
    }

    /**
     * 
     * @return
     * @throws DAOException
     */
    public Resolution rdfTypes() throws DAOException {

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Getting type suggestions ...");
        }

        long startTime = System.currentTimeMillis();
        List<String> typeUris = ApplicationCache.getTypeUris();

        String query = getContext().getRequestParameter("query");

        List<String> resultList = new ArrayList<String>();
        for (String typeUri : typeUris) {
            if (typeUri.toLowerCase().contains(query.toLowerCase())) {
                resultList.add(typeUri);
            }
        }

        if (resultList.isEmpty()) {
            resultList.add("No suggestions found!");
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Type suggestions retrieved in " + (System.currentTimeMillis() - startTime) + " ms");
        }
        return createSuggestionsResolution(resultList, query);
    }

    /**
     * 
     * @param suggestions
     * @param query
     * @return
     */
    private StreamingResolution createSuggestionsResolution(List<String> suggestions, String query) {

        JSONArray jsonArray = JSONArray.fromObject(suggestions);

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("query", query == null ? "" : query);
        resultMap.put("suggestions", jsonArray);

        JSONObject jsonObject = JSONObject.fromObject(resultMap);

        return new StreamingResolution("text", new StringReader(jsonObject.toString()));
    }
}
