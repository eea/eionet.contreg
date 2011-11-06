package eionet.cr.web.action.admin.postHarvest;

/**
 * @author Jaanus Heinlaid
 */
public class Tab {

    /** */
    private String title;
    private String href;
    private String hint;
    private boolean selected;

    /**
     * @param title
     * @param href
     * @param hint
     * @param selected
     */
    public Tab(String title, String href, String hint, boolean selected) {
        this.title = title;
        this.href = href;
        this.hint = hint;
        this.selected = selected;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the href
     */
    public String getHref() {
        return href;
    }

    /**
     * @return the hint
     */
    public String getHint() {
        return hint;
    }

    /**
     * @return the selected
     */
    public boolean isSelected() {
        return selected;
    }
}
