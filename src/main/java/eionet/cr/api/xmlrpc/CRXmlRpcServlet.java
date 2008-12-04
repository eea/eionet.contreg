package eionet.cr.api.xmlrpc;

import java.io.IOException;
import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.webserver.XmlRpcServlet;

/**
 * 
 * @author heinljab
 *
 */
public class CRXmlRpcServlet extends XmlRpcServlet{

	/*
	 * (non-Javadoc)
	 * @see org.apache.xmlrpc.webserver.XmlRpcServlet#newXmlRpcHandlerMapping()
	 */
	protected XmlRpcHandlerMapping newXmlRpcHandlerMapping() throws XmlRpcException {
		
		URL url = CRXmlRpcServlet.class.getClassLoader().getResource("cr-xmlrpc.properties");
		if (url == null) {
			throw new XmlRpcException("Failed to locate resource cr-xmlrpc.properties");
		}
		try {
			return newPropertyHandlerMapping(url);
		}
		catch (IOException e) {
			throw new XmlRpcException("Failed to load resource " + url + ": " + e.getMessage(), e);
		}
	}
}
