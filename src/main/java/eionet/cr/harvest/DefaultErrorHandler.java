/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.harvest;

import org.apache.log4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class DefaultErrorHandler implements ErrorHandler {
	
	private static final Logger logger = Logger.getLogger(DefaultErrorHandler.class);
	
	private SAXParseException saxError;
	private SAXParseException saxWarning;

	/** 
	 * @see eionet.cr.harvest.IRDFHandler#error(org.xml.sax.SAXParseException)
	 * {@inheritDoc}
	 */
	public void error(SAXParseException e) throws SAXException {

			logger.warn("SAX error encountered: " + e.toString(), e);			
			saxError = new SAXParseException(new String(e.getMessage()==null ? "" : e.getMessage()),
											 new String(e.getPublicId()==null ? "" : e.getPublicId()),
											 new String(e.getSystemId()==null ? "" : e.getSystemId()),
											 e.getLineNumber(),
											 e.getColumnNumber());
	}

	/** 
	 * @see eionet.cr.harvest.IRDFHandler#warning(org.xml.sax.SAXParseException)
	 * {@inheritDoc}
	 */
	public void warning(SAXParseException e) throws SAXException {
		
			logger.warn("SAX warning encountered: " + e.toString(), e);
			saxWarning = new SAXParseException(new String(e.getMessage()==null ? "" : e.getMessage()),
					 new String(e.getPublicId()==null ? "" : e.getPublicId()),
					 new String(e.getSystemId()==null ? "" : e.getSystemId()),
					 e.getLineNumber(),
					 e.getColumnNumber());
	}

	/** 
	 * @see eionet.cr.harvest.IRDFHandler#fatalError(org.xml.sax.SAXParseException)
	 * {@inheritDoc}
	 */
	public void fatalError(SAXParseException e) throws SAXException {
		throw new LoadException(e.toString(), e);
	}

	/**
	 * @return the saxError
	 */
	public SAXParseException getSaxError() {
		return saxError;
	}

	/**
	 * @return the saxWarning
	 */
	public SAXParseException getSaxWarning() {
		return saxWarning;
	}
}
