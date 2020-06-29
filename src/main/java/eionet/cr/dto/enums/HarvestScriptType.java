package eionet.cr.dto.enums;

import org.apache.commons.lang.StringUtils;

/** Enum for possible harvest script types. */
public enum HarvestScriptType {

    /**
     * Post-harvest script to be run after harvest.
     */
    POST_HARVEST("Post Harvest script", "Post-harvest"),

    /**
     * Push script, pushing content to an external service.
     */
    PUSH("Push script", "Push");

    /**
     * The enum's human friendly label.
     */
    private String label;

    /**
     * The enum's human friendly shortLabel.
     */
    private String shortLabel;

    /**
     * Enum constructor.
     *
     * @param label
     */
    HarvestScriptType(String label, String acronym) {
        this.label = label;
        this.shortLabel = acronym;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns {@link Enum#name()} of this enum.
     *
     * @return the name
     */
    public String getName() {
        return name();
    }

    /**
     * @return the shortLabel
     */
    public String getShortLabel() {
        return StringUtils.isBlank(shortLabel) ? name() : shortLabel;
    }
}



