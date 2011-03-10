package eionet.cr.util.export;

import org.apache.poi.hssf.usermodel.HSSFCell;

public class XlsUtil {

    public static void setCellValue(HSSFCell cell, String stringValue) {
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
    }

}
