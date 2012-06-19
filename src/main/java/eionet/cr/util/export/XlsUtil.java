package eionet.cr.util.export;

import org.apache.poi.hssf.usermodel.HSSFCell;

/**
 *
 * Utility class helping in the generation of the Excel files.
 *
 * @author Jaanus Heinlaid
 */
public final class XlsUtil {

    /**
     * Hide utility class constructor.
     */
    private XlsUtil() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     *
     * @param cell
     * @param stringValue
     */
    public static void setCellValue(HSSFCell cell, String stringValue) {
        Double value = null;
        try {
            value = new Double(stringValue);
        } catch (Exception ignored) {
            // No need to throw or log it.
        }
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue(stringValue == null ? "" : stringValue);
        }
    }

}
