package eionet.cr.web.util.search;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class SearchResultColumn {

	/** */
	private String title;
	private boolean isSortable;
	
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
	 * @return the isSortable
	 */
	public boolean isSortable() {
		return isSortable;
	}
	/**
	 * @param isSortable the isSortable to set
	 */
	public void setSortable(boolean isSortable) {
		this.isSortable = isSortable;
	}

}
