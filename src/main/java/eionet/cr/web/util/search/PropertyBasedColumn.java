package eionet.cr.web.util.search;

import eionet.cr.web.util.Formatter;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PropertyBasedColumn extends SearchResultColumn{

	/** */
	private String property;
	private Formatter formatter;

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
	 * @return the formatter
	 */
	public Formatter getFormatter() {
		return formatter;
	}

	/**
	 * @param formatter the formatter to set
	 */
	public void setFormatter(Formatter formatter) {
		this.formatter = formatter;
	}
}
