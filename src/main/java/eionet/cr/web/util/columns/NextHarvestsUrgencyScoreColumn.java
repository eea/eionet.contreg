package eionet.cr.web.util.columns;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import eionet.cr.harvest.statistics.dto.HarvestUrgencyScoreDTO;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

public class NextHarvestsUrgencyScoreColumn extends SearchResultColumn {

	public enum COLUMN {URL, LASTHARVEST, INTERVAL, URGENCY};

	private COLUMN columnType;
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.util.columns.SearchResultColumn#format(java.lang.Object)
	 */
	public String format(Object object) {
		
		String result = "";
		if (object!=null){
			HarvestUrgencyScoreDTO harvestUrgencyScore = (HarvestUrgencyScoreDTO)object;

			if (columnType == COLUMN.URL){
				result = harvestUrgencyScore.getUrl();
			}
			
			if (columnType == COLUMN.LASTHARVEST){
				Date date = harvestUrgencyScore .getLastHarvest();
				if (date!=null && date.getTime()>0){
					result = SIMPLE_DATE_FORMAT.format(date);
				}
			}
			
			if (columnType == COLUMN.INTERVAL){
				result = harvestUrgencyScore.getIntervalMinutes()+"";
			}
			
			if (columnType == COLUMN.URGENCY){
				NumberFormat formatter = new DecimalFormat("#.000"); 
				result = formatter.format(harvestUrgencyScore.getUrgency());
			}
		}
		
		return result;
	}

	/*
	 * (non-Javadoc)
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
