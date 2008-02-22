package eionet.cr.util;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.ExceptionHandler;

/**
 * @author altnyris
 *
 */
public class CRStripesExceptionHandler implements ExceptionHandler {
	
	/** */
	private static Log logger = LogFactory.getLog(CRStripesExceptionHandler.class);

	/*
	 * (non-Javadoc)
	 * @see net.sourceforge.stripes.config.ConfigurableComponent#init(net.sourceforge.stripes.config.Configuration)
	 */
    public void init(Configuration configuration) throws Exception {
    }

    /*
     * (non-Javadoc)
     * @see net.sourceforge.stripes.exception.ExceptionHandler#handle(java.lang.Throwable, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void handle(Throwable throwable,
                       HttpServletRequest request,
                       HttpServletResponse response) throws ServletException, IOException {
    	
    	Throwable t = (throwable instanceof ServletException) ? ((ServletException)throwable).getRootCause() : throwable;
    	if (t==null)
    		t = throwable;

    	logger.error(t.getMessage(), t);
    	request.setAttribute("exception", t);
    	request.getRequestDispatcher("/pages/error.jsp").forward(request, response);
    	
    }
}
