package eionet.cr.harvest.statistics.dto;

import java.util.Date;

/**
 *
 * @author <a href="mailto:jaak.kapten@tietoenator.com">Jaak Kapten</a>
 *
 */

public class HarvestedUrlCountDTO {

    private Date harvestDay;
    private long harvestCount;

    public Date getHarvestDay() {
        return harvestDay;
    }

    public void setHarvestDay(Date harvestDay) {
        this.harvestDay = harvestDay;
    }

    public long getHarvestCount() {
        return harvestCount;
    }

    public void setHarvestCount(long harvestCount) {
        this.harvestCount = harvestCount;
    }

}
