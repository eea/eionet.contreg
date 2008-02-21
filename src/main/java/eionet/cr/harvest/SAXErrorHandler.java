package eionet.cr.harvest;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class SAXErrorHandler implements org.xml.sax.ErrorHandler{
	
	/** */
	private static Logger logger = Logger.getLogger(SAXErrorHandler.class);

	/*
	 *  (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	public void warning(SAXParseException e) throws SAXException {
		logger.warn("SAX warning encountered: " + e.toString(), e);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
	 */
	public void error(SAXParseException e) throws SAXException {
		logger.error("SAX error encountered: " + e.toString(), e);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	public void fatalError(SAXParseException e) throws SAXException {
		logger.error("SAX fatal error encountered: " + e.toString(), e);
	}
}
