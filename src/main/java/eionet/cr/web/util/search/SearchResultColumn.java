package eionet.cr.web.util.search;

import eionet.cr.common.Md5Map;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SearchResultColumn {

	/** */
	private String property;
	private String propertyKey;
	private String title;
	private boolean sortable;
	
	/**
	 * @return the property
	 */
	public String getProperty() {
		return property;
	}
	/**
	 * @param property the property to set
	 */
	public void setProperty(String property) {
		this.property = property;
	}
	/**
	 * @return the propertyKey
	 */
	public String getPropertyKey() {
		if (propertyKey==null){
			if (property!=null){
				propertyKey = Md5Map.addValue(property);
			}
		}
		return propertyKey;
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
