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
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.cr.web.util.tabs;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.URLUtil;

/**
 * Helper for creating factsheet tab menu.
 *
 * @author Juhan Voolaid
 */
public class FactsheetTabMenuHelper {

    private static final Logger LOGGER = Logger.getLogger(FactsheetTabMenuHelper.class);

    /** The subject data object found by the requestd URI or URI hash. */
    private SubjectDTO subject;

    private boolean uriIsHarvestSource;
    private boolean mapDisplayable;
    private boolean sparqlBookmarkType;

    private String latitude;
    private String longitude;

    public FactsheetTabMenuHelper(String uri, SubjectDTO subject, HarvestSourceDAO harvesterSourceDao) throws DAOException {
        if (subject == null) {
            subject = new SubjectDTO(uri, false);
        }

        this.subject = subject;

        HarvestSourceDTO dto = harvesterSourceDao.getHarvestSourceByUrl(subject.getUri());
        uriIsHarvestSource = dto != null;

        mapDisplayable = subject.getObject(Predicates.WGS_LAT) != null && subject.getObject(Predicates.WGS_LONG) != null;
        if (mapDisplayable) {
            latitude = subject.getObject(Predicates.WGS_LAT).getValue();
            longitude = subject.getObject(Predicates.WGS_LONG).getValue();
        }

        if (subject.getObject(Predicates.RDF_TYPE) != null) {
            sparqlBookmarkType = Subjects.CR_SPARQL_BOOKMARK.equals(subject.getObject(Predicates.RDF_TYPE).getValue());
        }
    }

    /**
     * Returns tabs.
     *
     * @param selected
     *            selected tab element's title
     * @return
     */
    public List<TabElement> getTabs(String selected) {
        List<TabElement> result = new ArrayList<TabElement>();

        TabElement te1 = new TabElement(TabTitle.RESOURCE_PROPERTIES, "/factsheet.action", selected);
        te1.addParam("uri", subject.getUri());
        result.add(te1);

        TabElement te2 = new TabElement(TabTitle.RESOURCE_REFERENCES, "/references.action", selected);
        te2.setEvent("search");
        te2.addParam("uri", subject.getUri());
        result.add(te2);

        if (uriIsHarvestSource) {
            TabElement te3 = new TabElement(TabTitle.OBJECTS_IN_SOURCE, "/objectsInSource.action", selected);
            te3.setEvent("search");
            te3.addParam("uri", subject.getUri());
            result.add(te3);

        }

        if (mapDisplayable) {
            TabElement te4 = new TabElement(TabTitle.SHOW_ON_MAP, "/factsheet.action", selected);
            te4.setEvent("showOnMap");
            te4.addParam("uri", subject.getUri());
            te4.addParam("latitude", latitude);
            te4.addParam("longitude", longitude);
            result.add(te4);
        }

        if (sparqlBookmarkType) {
            TabElement te5 = new TabElement(TabTitle.BOOKMARKED_SPARQL, "/sparqlBookmark.action", selected);
            te5.addParam("uri", subject.getUri());
            result.add(te5);
        }

        return result;
    }

    /**
     * @return the uriIsHarvestSource
     */
    public boolean isUriIsHarvestSource() {
        return uriIsHarvestSource;
    }



    public static class TabTitle {
        public static final String RESOURCE_PROPERTIES = "Resource properties";
        public static final String RESOURCE_REFERENCES = "Resource references";
        public static final String OBJECTS_IN_SOURCE = "Objects in source";
        public static final String SHOW_ON_MAP = "Show on map";
        public static final String BOOKMARKED_SPARQL = "Bookmarked SPARQL";
    }
}
