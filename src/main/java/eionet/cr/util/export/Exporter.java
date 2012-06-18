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
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.util.export;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.ExporterDAO;
import eionet.cr.dao.readers.ResultSetExportReader;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.FormatUtils;
import eionet.cr.util.Pair;
import eionet.cr.util.URIUtil;
import eionet.cr.util.Util;
import eionet.cr.util.pagination.PagingRequest;

/**
 * Utility class to handle export procedure.
 * 
 * @author Aleksandr Ivanov <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public abstract class Exporter {

    protected Logger logger = Logger.getLogger(Exporter.class);

    private ExportFormat exportFormat;
    private Map<String, String> selectedFilters;
    private List<String> languages;

    // if true exports Resource uri, label otherwise.
    private boolean exportResourceUri;

    // List of selected columns Pair.id - column URI, Pair.value - optional column label
    // if label is present - it is used in the output. Otherwise URI is used.
    // URI should be always present
    private List<Pair<String, String>> selectedColumns;

    /**
     * exports search result into given format.
     * 
     * @param customSearch
     * @return
     * @throws IOException
     * @throws DAOException
     */
    protected abstract InputStream doExport() throws ExportException, IOException, DAOException;

    /**
     * Creates Exporter object for given export format.
     * 
     * @param exportFormat
     * @return
     */
    public static Exporter getExporter(ExportFormat exportFormat) {
        Exporter exporter = null;
        switch (exportFormat) {
            case XLS:
                exporter = new XlsExporter();
                break;
            case XML:
                exporter = new XmlExporter();
                break;
            case XML_WITH_SCHEMA:
                exporter = new XmlWithSchemaExporter();
                break;
            default:
                throw new CRRuntimeException("Exporter is not implemented for format: " + exportFormat);
        }
        exporter.setExportFormat(exportFormat);

        return exporter;
    }

    public InputStream export() throws DAOException, IOException, ExportException {

        long startTime = System.currentTimeMillis();

        // add label into selected columns if not exist yet
        Pair<String, String> labelPredicate = new Pair<String, String>(Predicates.RDFS_LABEL, null);
        if (!selectedColumns.contains(labelPredicate)) {
            selectedColumns.add(labelPredicate);
        }

        InputStream result = doExport();

        logger.trace("Export process took: " + Util.durationSince(startTime));

        return result;
    }

    public void doExportQueryAndWriteDataIntoOutput(ResultSetExportReader reader) throws DAOException {
        Map<String, String> criteria = new HashMap<String, String>();
        for (Entry<String, String> entry : selectedFilters.entrySet()) {
            criteria.put(StringUtils.trim(entry.getKey()), StringUtils.trim(entry.getValue()));
        }
        // do the query and write data rows directly to export file
        DAOFactory.get().getDao(ExporterDAO.class).exportByTypeAndFilters(criteria, getSelectedColumnsList(), reader);

    }

    /**
     * Returns the label of subject's uri or label depending on exportResourceUri value.
     * 
     * @return
     */
    protected String getUriOrLabel() {
        String uriOrLabelElement = isExportResourceUri() ? "Uri" : "Label";

        return uriOrLabelElement;
    }

    /**
     * This method creates the PaginRequest object for limiting the rows in the search results.
     * 
     * @return
     */
    protected PagingRequest getRowLimitPagingRequest() {
        return null;
    }

    /**
     * Returns the value of subject's uri or label depending on exportResourceUri value.
     * 
     * @return
     */
    protected String getUriOrLabelValue(SubjectDTO subject) {
        String value = "";
        String uri = subject.getUri();

        if (isExportResourceUri()) {
            value = uri;
        } else {
            value = FormatUtils.getObjectValuesForPredicate(Predicates.RDFS_LABEL, subject, getLanguages());
            // extract value from uri
            if (StringUtils.isBlank(value)) {
                value = URIUtil.extractURILabel(uri, uri);
            }

        }

        return value;
    }

    /**
     * @return the exportFormat
     */
    public ExportFormat getExportFormat() {
        return exportFormat;
    }

    /**
     * @param exportFormat the exportFormat to set
     */
    public void setExportFormat(ExportFormat exportFormat) {
        this.exportFormat = exportFormat;
    }

    /**
     * @return the selectedFilters
     */
    public Map<String, String> getSelectedFilters() {
        return selectedFilters;
    }

    /**
     * @param selectedFilters the selectedFilters to set
     */
    public void setSelectedFilters(Map<String, String> selectedFilters) {
        this.selectedFilters = selectedFilters;
    }

    /**
     * @return the selectedColumns
     */
    public List<Pair<String, String>> getSelectedColumns() {
        return selectedColumns;
    }

    /**
     * @param selectedColumns the selectedColumns to set
     */
    public void setSelectedColumns(List<Pair<String, String>> selectedColumns) {
        this.selectedColumns = selectedColumns;
    }

    /**
     * @return the languages
     */
    public List<String> getLanguages() {
        return languages;
    }

    /**
     * @param languages the languages to set
     */
    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    /**
     * @return the exportResourceUri
     */
    public boolean isExportResourceUri() {
        return exportResourceUri;
    }

    /**
     * @param exportResourceUri the exportResourceUri to set
     */
    public void setExportResourceUri(boolean exportResourceUri) {
        this.exportResourceUri = exportResourceUri;
    }

    /**
     * Rerturns the number of rows the exporter output can handle
     * 
     * @return
     */
    public static Integer getRowsLimit() {
        return -1;
    }

    /**
     * Returns the list of selected predicates uris
     * 
     * @return
     */
    public List<String> getSelectedColumnsList() {
        List<String> list = new ArrayList<String>();
        if (selectedColumns != null && !selectedColumns.isEmpty()) {
            for (Pair<String, String> col : selectedColumns) {
                list.add(col.getLeft());
            }
        }
        return list;
    }
}
