package eionet.cr.web.util.columns;

import java.util.Date;

import eionet.cr.harvest.statistics.dto.HarvestedUrlCountDTO;

public class DateColumn extends SearchResultColumn {

    public enum COLUMN_TYPE {
        DATE_AND_TIME
    };

    private COLUMN_TYPE columnType;

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.web.util.columns.SearchResultColumn#format(java.lang.Object)
     */
    public String format(Object object) {

        String result = "";
        if (object != null) {

            HarvestedUrlCountDTO harvestUrgencyScore = (HarvestedUrlCountDTO) object;

            if (columnType == COLUMN_TYPE.DATE_AND_TIME) {
                Date date = harvestUrgencyScore.getHarvestDay();
                if (date != null && date.getTime() > 0) {
                    result = SIMPLE_DATE_FORMAT.format(date);
                }
            }
        }

        return result;
    }

    @Override
    public String getSortParamValue() {
        // TODO Auto-generated method stub
        return null;
    }

    public COLUMN_TYPE getColumnType() {
        return columnType;
    }

    public void setColumnType(COLUMN_TYPE columnType) {
        this.columnType = columnType;
    }

}
