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
import org.apache.poi.ss.usermodel.Font;

import eionet.cr.common.Predicates;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.SearchDAO;
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
	
	//if true exports Resource uri, label otherwise.
	private boolean exportResourceUri;
	
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
				.getDao(SearchDAO.class)
				.performCustomSearch(
						criteria,
						null,
						new PageRequest(
								1, 
								new Integer(GeneralConfig.getRequiredProperty(EXPORT_ROW_LIMIT))),
						null);
		InputStream result = null;
		if (exportFormat == ExportFormat.XLS) {			
			result = exportXls(customSearch);
		}
		
		return result;
	}

	/**
	 * exports custom search to XLS format.
	 * 
	 * @param customSearch
	 * @return
	 * @throws IOException
	 */
	private InputStream exportXls(Pair<Integer, List<SubjectDTO>> customSearch) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("exported data");
		
		//some pretty print with headers
		CellStyle headerStyle = workbook.createCellStyle();
		Font headerFont = workbook.createFont();
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerStyle.setFont(headerFont);
		
		//if exporting with labels no need to export RDFS_LABEL
		if (!exportResourceUri) {
			selectedColumns.remove(new Pair<String,String>(Predicates.RDFS_LABEL, null));
		}
		
		//output headers
		HSSFRow headers = sheet.createRow(0);
		//store width of each column +1 for Uri or Label column
		int[] columnWidth = new int[selectedColumns.size() + 1];
		//output Uri or Label column
		String uriOrLabelColumn = exportResourceUri 
				? "Uri"
				: "Label";
		columnWidth[0] = uriOrLabelColumn.length();
		HSSFCell uriOrLabelCell= headers.createCell(0);
		setCellValue(uriOrLabelCell, uriOrLabelColumn).setCellStyle(headerStyle);

		//output rest of the headers
		int columnNumber= 1;
		for(Pair<String,String> columnPair : selectedColumns) {
			String column = columnPair.getRight() != null
						? columnPair.getRight()
						: columnPair.getLeft();
			columnWidth[columnNumber] = column.length();
			HSSFCell cell = headers.createCell(columnNumber++);
			setCellValue(cell, column).setCellStyle(headerStyle);
		}
		sheet.createFreezePane(0, 1);
		
		//output serarch results
		int rowNumber = 1;
		for(SubjectDTO subject : customSearch.getRight()) {
			HSSFRow row = sheet.createRow(rowNumber++);
			
			//output uri or label column value
			String value = exportResourceUri
					? subject.getUri()
					: FormatUtils.getObjectValuesForPredicate(Predicates.RDFS_LABEL, subject, getLanguages());
			columnWidth[0] = Math.max(columnWidth[0], value.length());
			setCellValue(row.createCell(0), value);

			//output other columns
			columnNumber = 1;
			for(Pair<String,String> columnPair : selectedColumns) {
				value = FormatUtils.getObjectValuesForPredicate(columnPair.getLeft(), subject, languages);
				columnWidth[columnNumber] = Math.max(columnWidth[columnNumber], value.length());
				setCellValue(row.createCell(columnNumber++), value);
			}
		}
		
		//set column width
		for (int i = 0; i < selectedColumns.size() + 1; i++) {
			sheet.setColumnWidth(i, Math.min(256 * columnWidth[i], 256*255));				
		}
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		workbook.write(output);
		return new ByteArrayInputStream(output.toByteArray());
	}
	
	private HSSFCell setCellValue(HSSFCell cell, String stringValue) {
		Double value = null;
		try {
			value = new Double(stringValue);
		} catch (Exception ignored) {}
		if (value != null) {
			cell.setCellValue(value);
		} else {
			cell.setCellValue(stringValue == null
					? "" 
					: stringValue);
		}
		return cell;
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
}
