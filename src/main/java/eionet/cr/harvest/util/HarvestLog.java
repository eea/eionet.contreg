package eionet.cr.harvest.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HarvestLog implements Log{
	
	/** */
	private Log logger;
	
	/** */
	private String harvestSourceUrl;
	private long harvestGenTime;
	
	/**
	 * 
	 */
	public HarvestLog(String harvestSourceUrl, Log logger){
		this(harvestSourceUrl, -1, logger);
	}

	/**
	 * 
	 */
	public HarvestLog(String harvestSourceUrl, long harvestGenTime, Log logger){
		
		this.harvestSourceUrl = harvestSourceUrl;
		this.harvestGenTime = harvestGenTime;
		this.logger = logger;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.logging.Log#debug(java.lang.Object)
	 */
	public void debug(Object obj) {
		logger.debug(format(obj));
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.logging.Log#debug(java.lang.Object, java.lang.Throwable)
	 */
	public void debug(Object obj, Throwable throwable) {
		logger.debug(format(obj), throwable);
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.logging.Log#error(java.lang.Object)
	 */
	public void error(Object obj) {
		logger.error(format(obj));
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.logging.Log#error(java.lang.Object, java.lang.Throwable)
	 */
	public void error(Object obj, Throwable throwable) {
		logger.error(format(obj), throwable);
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.logging.Log#fatal(java.lang.Object)
	 */
	public void fatal(Object obj) {
		logger.fatal(format(obj));
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.logging.Log#fatal(java.lang.Object, java.lang.Throwable)
	 */
	public void fatal(Object obj, Throwable throwable) {
		logger.fatal(format(obj), throwable);
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.logging.Log#info(java.lang.Object)
	 */
	public void info(Object obj) {
		logger.info(format(obj));
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.logging.Log#info(java.lang.Object, java.lang.Throwable)
	 */
	public void info(Object obj, Throwable throwable) {
		logger.info(format(obj), throwable);
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.logging.Log#isDebugEnabled()
	 */
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.logging.Log#isErrorEnabled()
	 */
	public boolean isErrorEnabled() {
		return logger.isErrorEnabled();
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.logging.Log#isFatalEnabled()
	 */
	public boolean isFatalEnabled() {
		return logger.isFatalEnabled();
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.logging.Log#isInfoEnabled()
	 */
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.logging.Log#isTraceEnabled()
	 */
	public boolean isTraceEnabled() {
		return logger.isTraceEnabled();
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.logging.Log#isWarnEnabled()
	 */
	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.logging.Log#trace(java.lang.Object)
	 */
	public void trace(Object obj) {
		logger.trace(format(obj));
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.logging.Log#trace(java.lang.Object, java.lang.Throwable)
	 */
	public void trace(Object obj, Throwable throwable) {
		logger.trace(format(obj), throwable);
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.logging.Log#warn(java.lang.Object)
	 */
	public void warn(Object obj) {
		logger.warn(format(obj));
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.logging.Log#warn(java.lang.Object, java.lang.Throwable)
	 */
	public void warn(Object obj, Throwable throwable) {
		logger.warn(format(obj), throwable);
	}
	
	/**
	 * 
	 * @param obj
	 * @return
	 */
	private String format(Object obj){
		
		StringBuilder sb = new StringBuilder();
		sb.append(obj).append(" [source=").append(harvestSourceUrl);
		if (harvestGenTime>=0){
			sb.append(", genTime=").append(harvestGenTime);
		}
		sb.append("]");
		return sb.toString();
	}
}
