package eionet.cr.web.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class UploadDateFormatter implements Formatter {
	
	/** */
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.util.Formatter#format(java.lang.Object)
	 */
	public String format(Object object){
		
		String result = "";
		if (object!=null && object instanceof Date){
			result = simpleDateFormat.format((Date)object);
		}
		return result;
	}

}
