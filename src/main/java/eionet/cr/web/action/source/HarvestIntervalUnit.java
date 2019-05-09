package eionet.cr.web.action.source;

import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public enum HarvestIntervalUnit {

    MINUTES(1), HOURS(60), DAYS(24 * 60), WEEKS(24 * 60 * 7);

    private int minutes;

    HarvestIntervalUnit(int minutes) {
        this.minutes = minutes;
    }

    public String getLabel() {
        return name().toLowerCase();
    }

    public String getLabelSingular() {
        return StringUtils.substringBefore(getLabel(), "s");
    }

    public int getMinutes() {
        return minutes;
    }

    /**
     *
     * @return
     */
    public static Map<Integer, String> valuesMap() {

        Map map = new LinkedHashMap();
        for (HarvestIntervalUnit choice : HarvestIntervalUnit.values()) {
            map.put(choice.getMinutes(), choice.getLabel());
        }
        return map;
    }
}
