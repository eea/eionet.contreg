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
package eionet.cr.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.ISearchDao;
import eionet.cr.dao.mysql.MySQLDAOFactory;
import eionet.cr.dto.SubjectDTO;

/**
 * Utility class to handle export procedure.
 * 
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class Exporter {
	
	//config param in cr.properties
	private static final String EXPORT_ROW_LIMIT = "exporter.xls.row.limit";
	
	private ExportFormat exportFormat;
	private Map<String,String> selectedFilters;
	private Set<String> languages;
	
	//List of selected columns Pair.id - column URI, Pair.value - optional column label
	// if label is present - it is used in the output. Otherwise URI is used.
	// URI should be always present
	private List<Pair<String,String>> selectedColumns;
	
	
	public InputStream export() throws DAOException, IOException {
		Pair<Integer, List<SubjectDTO>> customSearch;
		Map<String,String> criteria = new HashMap<String, String>();
		for(Entry<String, String> entry : selectedFilters.entrySet()) {
			criteria.put(StringUtils.trim(entry.getKey()), StringUtils.trim(entry.getValue()));
		}
		customSearch = MySQLDAOFactory.get()
				.getDao(ISearchDao.class)
				.performCustomSearch(
						criteria,
						null,
						new PageRequest(
								1, 
								new Integer(GeneralConfig.getRequiredProperty(EXPORT_ROW_LIMIT))),
						null);
		InputStream result = null;
		if (exportFormat == ExportFormat.XLS) {
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet sheet = workbook.createSheet("exported data");
			CreationHelper helper = workbook.getCreationHelper();
			
			//some pretty print with headers
			CellStyle headerStyle = workbook.createCellStyle();
			Font headerFont = workbook.createFont();
			headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerStyle.setFont(headerFont);
			
			//output headers
			HSSFRow headers = sheet.createRow(0);
			int columnNumber= 0;
			//store width of each column
			int[] columnWidth = new int[selectedColumns.size()];
			
			for(Pair<String,String> columnPair : selectedColumns) {
				String column = columnPair.getValue() != null
						? columnPair.getValue()
						: columnPair.getId();
				columnWidth[columnNumber] = column.length();
				HSSFCell cell = headers.createCell(columnNumber++);
				cell.setCellValue(helper.createRichTextString(column));
				cell.setCellStyle(headerStyle);
			}
			sheet.createFreezePane(0, 1);
			
			int rowNumber = 1;
			for(SubjectDTO subject : customSearch.getValue()) {
				HSSFRow row = sheet.createRow(rowNumber++);
				columnNumber = 0;
				for(Pair<String,String> columnPair : selectedColumns) {
					String value = FormatUtils.getObjectValuesForPredicate(columnPair.getId(), subject, languages);
					columnWidth[columnNumber] = Math.max(columnWidth[columnNumber], value.length());
					row.createCell(columnNumber++).setCellValue(helper.createRichTextString(
							!StringUtils.isBlank(value)
									? value
									: ""));
				}
			}
			
			//set column width
			for (int i = 0; i< selectedColumns.size(); i++) {
				sheet.setColumnWidth(i, 256 * columnWidth[i]);				
			}
			
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			workbook.write(output);
			result = new ByteArrayInputStream(output.toByteArray());
		}
		
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
}
