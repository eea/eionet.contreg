package eionet.cr.web.util.columns;

import java.util.Date;

import eionet.cr.harvest.statistics.dto.HarvestedUrlCountDTO;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 * 
 */

public class HarvestedUrlCountColumn extends SearchResultColumn {

    public enum COLUMN {
        HARVESTDAY, HARVESTCOUNT, HARVESTDAYSTRING
    };

    private COLUMN columnType;

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.web.util.columns.SearchResultColumn#format(java.lang.Object)
     */
    public String format(Object object) {

        String result = "";
        if (object != null) {

            HarvestedUrlCountDTO harvestUrgencyScore = (HarvestedUrlCountDTO) object;

            if (columnType == COLUMN.HARVESTDAY) {
                Date date = harvestUrgencyScore.getHarvestDay();
                if (date != null && date.getTime() > 0) {
                    result = SIMPLE_DATE_FORMAT.format(date);
                }
            }

            if (columnType == COLUMN.HARVESTDAYSTRING) {
                Date date = harvestUrgencyScore.getHarvestDay();
                if (date != null && date.getTime() > 0) {
                    result = DATE_ONLY_FORMAT.format(date);
                }
            }

            if (columnType == COLUMN.HARVESTCOUNT) {
                result = harvestUrgencyScore.getHarvestCount() + "";
            }

        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.web.util.columns.SearchResultColumn#getSortParamValue()
     */
    public String getSortParamValue() {
        return getClass().getSimpleName();
    }

    public COLUMN getColumnType() {
        return columnType;
    }

    public void setColumnType(COLUMN columnType) {
        this.columnType = columnType;
    }

}
