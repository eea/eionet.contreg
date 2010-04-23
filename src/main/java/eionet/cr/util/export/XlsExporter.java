/*
 * Created on 23.04.2010
 */
package eionet.cr.util.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

import eionet.cr.common.Predicates;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.FormatUtils;
import eionet.cr.util.Pair;

/**
 * @author Enriko Käsper, TietoEnator Estonia AS
 * XlsExporter
 */

public class XlsExporter extends Exporter {
	/**
	 * exports custom search to XLS format.
	 * 
	 * @param customSearch
	 * @return
	 * @throws IOException
	 */
	protected InputStream exportContent(Pair<Integer, List<SubjectDTO>> customSearch) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("exported data");
		
		//some pretty print with headers
		CellStyle headerStyle = workbook.createCellStyle();
		Font headerFont = workbook.createFont();
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerStyle.setFont(headerFont);
		
		//if exporting with labels no need to export RDFS_LABEL
		if (!isExportResourceUri()) {
			getSelectedColumns().remove(new Pair<String,String>(Predicates.RDFS_LABEL, null));
		}
		
		//output headers
		HSSFRow headers = sheet.createRow(0);
		//store width of each column +1 for Uri or Label column
		int[] columnWidth = new int[getSelectedColumns().size() + 1];
		//output Uri or Label column
		String uriOrLabelColumn = isExportResourceUri() 
				? "Uri"
				: "Label";
		columnWidth[0] = uriOrLabelColumn.length();
		HSSFCell uriOrLabelCell= headers.createCell(0);
		setCellValue(uriOrLabelCell, uriOrLabelColumn).setCellStyle(headerStyle);

		//output rest of the headers
		int columnNumber= 1;
		for(Pair<String,String> columnPair : getSelectedColumns()) {
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
			String value = isExportResourceUri()
					? subject.getUri()
					: FormatUtils.getObjectValuesForPredicate(Predicates.RDFS_LABEL, subject, getLanguages());
			columnWidth[0] = Math.max(columnWidth[0], value.length());
			setCellValue(row.createCell(0), value);

			//output other columns
			columnNumber = 1;
			for(Pair<String,String> columnPair : getSelectedColumns()) {
				value = FormatUtils.getObjectValuesForPredicate(columnPair.getLeft(), subject, getLanguages());
				columnWidth[columnNumber] = Math.max(columnWidth[columnNumber], value.length());
				setCellValue(row.createCell(columnNumber++), value);
			}
		}
		
		//set column width
		for (int i = 0; i < getSelectedColumns().size() + 1; i++) {
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
}
