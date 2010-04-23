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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.pagination.PagingRequest;

/**
 * Utility class to handle export procedure.
 * 
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public abstract class Exporter {
	
	//config param in cr.properties
	private static final String EXPORT_ROW_LIMIT = "exporter.xls.row.limit";
	
	private ExportFormat exportFormat;
	private Map<String,String> selectedFilters;
	private Set<String> languages;
	
	//if true exports Resource uri, label otherwise.
	private boolean exportResourceUri;
	
	//List of selected columns Pair.id - column URI, Pair.value - optional column label
	// if label is present - it is used in the output. Otherwise URI is used.
	// URI should be always present
	private List<Pair<String,String>> selectedColumns;
	
	/**
	 * exports search result into given fomrat
	 * @param customSearch
	 * @return
	 * @throws IOException
	 */
	protected abstract InputStream exportContent(Pair<Integer, List<SubjectDTO>> customSearch) throws IOException;

	/**
	 * Creates Exporter object for given export format 
	 * @param exportFormat
	 * @return
	 */
	public static Exporter getExporter(ExportFormat exportFormat){
		Exporter exporter = null;
		switch (exportFormat){
			case XLS:
				exporter = new XlsExporter();
				break;
			case XML:
				exporter = new XlsExporter();
				break;
			default:
				throw new CRRuntimeException("Exporter is not implemented for format: " + exportFormat);
		}
		exporter.setExportFormat(exportFormat);
		
		return exporter;
	}

	public InputStream export() throws DAOException, IOException {
		Pair<Integer, List<SubjectDTO>> customSearch;
		Map<String,String> criteria = new HashMap<String, String>();
		for(Entry<String, String> entry : selectedFilters.entrySet()) {
			criteria.put(StringUtils.trim(entry.getKey()), StringUtils.trim(entry.getValue()));
		}
		customSearch = DAOFactory.get()
				.getDao(SearchDAO.class)
				.searchByFilters(
						criteria,
						null,
						PagingRequest.create(1, 
								new Integer(GeneralConfig.getRequiredProperty(EXPORT_ROW_LIMIT))),
						null);
		InputStream result = null;
		result = exportContent(customSearch);
		
		return result;
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
	public Set<String> getLanguages() {
		return languages;
	}

	/**
	 * @param set the languages to set
	 */
	public void setLanguages(Set<String> set) {
		this.languages = set;
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
	 * Rerturns the number of rows MS Excel can handle on one sheet 
	 * @return
	 */
	public static Integer getXlsRowsLimit(){
		return new Integer(GeneralConfig.getRequiredProperty(EXPORT_ROW_LIMIT));		
	}
}
