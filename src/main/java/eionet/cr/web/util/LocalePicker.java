package eionet.cr.web.util;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import net.sourceforge.stripes.localization.DefaultLocalePicker;

/**
 * Custom locale picker that overrides {@link DefaultLocalePicker}.
 * 
 * @author gerasvad
 *
 */
public class LocalePicker extends DefaultLocalePicker {
	/**
	 * {@inheritDoc}
	 * <p>
	 * If parent method returns null then the method returns UTF-8 as default character encoding.
	 */
	@Override
	public String pickCharacterEncoding(HttpServletRequest request, Locale locale) {
		
		String encoding = super.pickCharacterEncoding(request, locale);
		return encoding == null ? "UTF-8" : null;
	}
}
