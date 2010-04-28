/*
 * Created on 26.04.2010
 */
package eionet.cr.util.export;

import eionet.cr.common.CRException;

/**
 * @author Enriko Käsper, TietoEnator Estonia AS
 * ExporterException
 */

public class ExportException  extends CRException{

	/**
	 * 
	 */
	public ExportException(){
		super();
	}
	
	/**
	 * 
	 * @param message
	 */
	public ExportException(String message){
		super(message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public ExportException(String message, Throwable cause){
		super(message, cause);
	}
}