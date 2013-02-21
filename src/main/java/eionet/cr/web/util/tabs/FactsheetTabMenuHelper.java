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

import java.util.ArrayList;
import java.util.List;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.SubjectDTO;

/**
 * Helper for creating factsheet tab menu.
 *
 * @author Juhan Voolaid
 */
public final class FactsheetTabMenuHelper {

    /** The subject data object found by the requestd URI or URI hash. */
    private final SubjectDTO subject;

    private final boolean uriIsHarvestSource;
    private final boolean mapDisplayable;
    private boolean sparqlBookmarkType;
    private boolean compiledDatasetType;
    private boolean folderType;
    private boolean bookmarksFileType;
    private boolean registrationsFileType;
    private boolean historyFileType;
    private boolean reviewType;
    private boolean tableFileType;

    /** */
    private String latitude;
    private String longitude;

    /** */
    private HarvestSourceDTO harvestSourceDTO;

    /**
     *
     * Class constructor.
     *
     * @param uri
     * @param subject
     * @param harvesterSourceDao
     * @throws DAOException
     */
    public FactsheetTabMenuHelper(String uri, SubjectDTO subject, HarvestSourceDAO harvesterSourceDao) throws DAOException {
        if (subject == null) {
            subject = new SubjectDTO(uri, false);
        }

        this.subject = subject;

        harvestSourceDTO = harvesterSourceDao.getHarvestSourceByUrl(subject.getUri());
        uriIsHarvestSource = harvestSourceDTO != null;

        //TODO: mapDisplayable = Subjects.WGS_SPATIAL_THING.equals(subject.getObject(Predicates.RDF_TYPE).getValue());
        mapDisplayable = subject.getObject(Predicates.WGS_LAT) != null && subject.getObject(Predicates.WGS_LONG) != null;
        if (mapDisplayable) {
            latitude = subject.getObject(Predicates.WGS_LAT).getValue();
            longitude = subject.getObject(Predicates.WGS_LONG).getValue();
        }

        // TODO: Is there some point to calling subject.getObject(Predicates.RDF_TYPE) over and over
        // to check if it is null?
        if (subject.getObject(Predicates.RDF_TYPE) != null) {
            sparqlBookmarkType = Subjects.CR_SPARQL_BOOKMARK.equals(subject.getObject(Predicates.RDF_TYPE).getValue());
        }

        if (subject.getObject(Predicates.RDF_TYPE) != null) {
            compiledDatasetType = Subjects.CR_COMPILED_DATASET.equals(subject.getObject(Predicates.RDF_TYPE).getValue());
        }

        if (subject.getObject(Predicates.RDF_TYPE) != null) {
            tableFileType = Subjects.CR_TABLE_FILE.equals(subject.getObject(Predicates.RDF_TYPE).getValue());
        }

        if (subject.getObject(Predicates.RDF_TYPE) != null) {
            reviewType =
                    Subjects.CR_REVIEW_FOLDER.equals(subject.getObject(Predicates.RDF_TYPE).getValue())
                    || Subjects.CR_FEEDBACK.equals(subject.getObject(Predicates.RDF_TYPE).getValue());
        }

        if (subject.getObject(Predicates.RDF_TYPE) != null) {
            bookmarksFileType = Subjects.CR_BOOKMARKS_FILE.equals(subject.getObject(Predicates.RDF_TYPE).getValue());
        }

        if (subject.getObject(Predicates.RDF_TYPE) != null) {
            registrationsFileType = Subjects.CR_REGISTRATIONS_FILE.equals(subject.getObject(Predicates.RDF_TYPE).getValue());
        }

        if (subject.getObject(Predicates.RDF_TYPE) != null) {
            historyFileType = Subjects.CR_HISTORY_FILE.equals(subject.getObject(Predicates.RDF_TYPE).getValue());
        }

        if (subject.getObject(Predicates.RDF_TYPE) != null) {
            folderType =
                    Subjects.CR_FOLDER.equals(subject.getObject(Predicates.RDF_TYPE).getValue())
                    || Subjects.CR_USER_FOLDER.equals(subject.getObject(Predicates.RDF_TYPE).getValue());
        }

    }

    /**
     * Returns tabs.
     *
     * @param selected
     *            - selected tab's title
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

        result.addAll(getTypeSpecificTabs(selected));
        return result;
    }

    /**
     * Returns the list of tab objects without a selected tab.
     *
     * @return
     */
    public List<TabElement> getTypeSpecificTabs() {
        return getTypeSpecificTabs(null);
    }

    /**
     * Returns the list of tab objects with the selected tab.
     *
     * @param selected
     *            - the title of the selected tab
     * @return List<TabElement>
     */
    public List<TabElement> getTypeSpecificTabs(String selected) {

        List<TabElement> result = new ArrayList<TabElement>();

        if (mapDisplayable) {
            TabElement t = new TabElement(TabTitle.SHOW_ON_MAP, "/factsheet.action", selected);
            t.setEvent("showOnMap");
            t.addParam("uri", subject.getUri());
            t.addParam("latitude", latitude);
            t.addParam("longitude", longitude);
            result.add(t);
        }

        if (sparqlBookmarkType) {
            TabElement t = new TabElement(TabTitle.BOOKMARKED_SPARQL, "/sparqlBookmark.action", selected);
            t.addParam("uri", subject.getUri());
            result.add(t);
        }

        if (compiledDatasetType) {
            TabElement t = new TabElement(TabTitle.COMPILED_DATASET, "/compiledDataset.action", selected);
            t.addParam("uri", subject.getUri());
            result.add(t);
        }

        if (reviewType) {
            TabElement t = new TabElement(TabTitle.REVIEW_FOLDER, "/reviews.action", selected);
            t.addParam("uri", subject.getUri());
            result.add(t);
        }

        if (folderType) {
            TabElement t = new TabElement(TabTitle.FOLDER, "/folder.action", selected);
            t.addParam("uri", subject.getUri());
            result.add(t);
        }

        if (bookmarksFileType) {
            TabElement t = new TabElement(TabTitle.BOOKMARKS, "/bookmarks.action", selected);
            t.addParam("uri", subject.getUri());
            result.add(t);
        }

        if (registrationsFileType) {
            TabElement t = new TabElement(TabTitle.REGISTRATIONS, "/registrations.action", selected);
            t.addParam("uri", subject.getUri());
            result.add(t);
        }

        if (historyFileType) {
            TabElement t = new TabElement(TabTitle.HISTORY, "/history.action", selected);
            t.addParam("uri", subject.getUri());
            result.add(t);
        }

        if (tableFileType) {
            TabElement t = new TabElement(TabTitle.TABLE_FILE_CONTENTS, "/tableFile.action", selected);
            t.addParam("uri", subject.getUri());
            result.add(t);
        }

        return result;
    }

    /**
     * @return the uriIsHarvestSource
     */
    public boolean isUriIsHarvestSource() {
        return uriIsHarvestSource;
    }

    /**
     * True if the resource is a local folder.
     */
    public boolean isUriFolder() {
        return folderType;
    }

    /**
     * Tab titles.
     */
    public static class TabTitle {

        public static final String RESOURCE_PROPERTIES = "Resource properties";
        public static final String RESOURCE_REFERENCES = "Resource references";
        public static final String OBJECTS_IN_SOURCE = "Objects in source";
        public static final String SHOW_ON_MAP = "Show on map";
        public static final String BOOKMARKED_SPARQL = "Bookmarked SPARQL";
        public static final String COMPILED_DATASET = "Compiled dataset";
        public static final String REVIEW_FOLDER = "Reviews";
        public static final String FOLDER = "Contents";
        public static final String BOOKMARKS = "Bookmarks";
        public static final String REGISTRATIONS = "Registrations";
        public static final String HISTORY = "History";
        public static final String TABLE_FILE_CONTENTS = "CSV/TSV contents";

        /**
         * Hide utility class constructor.
         */
        private TabTitle() {
            // Just an empty private constructor to avoid instantiating this utility class.
        }
    }

    /**
     * @return the harvestSourceDTO
     */
    public HarvestSourceDTO getHarvestSourceDTO() {
        return harvestSourceDTO;
    }
}
