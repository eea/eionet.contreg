package eionet.cr.util.pagination;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class Page {

	/** */
	private int number;
	private boolean selected;
	private String href;
	
	/**
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}
	/**
	 * @param number the number to set
	 */
	public void setNumber(int number) {
		this.number = number;
	}
	/**
	 * @return the selected
	 */
	public boolean isSelected() {
		return selected;
	}
	/**
	 * @param selected the selected to set
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	/**
	 * @return the href
	 */
	public String getHref() {
		return href;
	}
	/**
	 * @param href the href to set
	 */
	public void setHref(String url) {
		this.href = url;
	}
	
	/**
	 * 
	 */
	public String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append(number).append(selected ? "!" : "");
//		append(", ").append(selected).append(", ").append(href);
		return buf.toString();
	}
}
