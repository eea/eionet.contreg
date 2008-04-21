package eionet.cr.web.util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SearchResultColumn {

	/** */
	private String propertyUri;
	private String propertyKey;
	private String title;
	private boolean sortable;
	
	/**
	 * @return the property
	 */
	public String getPropertyUri() {
		return propertyUri;
	}
	/**
	 * @param property the property to set
	 */
	public void setPropertyUri(String property) {
		this.propertyUri = property;
	}
	/**
	 * @return the propertyMd5
	 */
	public String getPropertyKey() {
		return propertyKey;
	}
	/**
	 * @param propertyMd5 the propertyMd5 to set
	 */
	public void setPropertyKey(String propertyMd5) {
		this.propertyKey = propertyMd5;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the sortable
	 */
	public boolean isSortable() {
		return sortable;
	}
	/**
	 * @param sortable the sortable to set
	 */
	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}
}
