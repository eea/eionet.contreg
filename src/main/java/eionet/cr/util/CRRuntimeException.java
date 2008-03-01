package eionet.cr.util;

/**
 * 
 * @author heinljab
 *
 */
public class CRRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	public CRRuntimeException(){
		super();
	}

	/**
	 * @param s
	 */
	public CRRuntimeException(String s)
	{
		super(s);
	}

	/**
	 * @param s
	 * @param throwable
	 */
	public CRRuntimeException(String s, Throwable throwable)
	{
		super(s, throwable);
	}

	/**
	 * @param throwable
	 */
	public CRRuntimeException(Throwable throwable)
	{
		super(throwable);
	}

}
