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
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.api.xmlrpc;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    /*
     * (non-Javadoc)
     * @see org.apache.xmlrpc.webserver.XmlRpcServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        response.setCharacterEncoding("UTF-8");
        super.doPost(request, response);
    }
}
