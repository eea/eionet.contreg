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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        jaanus
 */

package eionet.cr.harvest.util;

import java.io.IOException;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.params.HttpClientParams;

/**
 * Just a helper extension of {@link HttpClient} that enables to get the last response code and message of the last
 * executeMethod(...) method.
 *
 * @author jaanus
 */
public class EndpointHttpClient extends HttpClient {

    /** */
    private HttpMethod lastExecutedMethod;

    /**
     *
     * Just calls the superclass constructor with the same inputs.
     *
     * @param params
     * @param httpConnectionManager
     */
    public EndpointHttpClient(HttpClientParams params, HttpConnectionManager httpConnectionManager) {
        super(params, httpConnectionManager);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.commons.httpclient.HttpClient#executeMethod(org.apache.commons.httpclient.HttpMethod)
     */
    @Override
    public int executeMethod(HttpMethod method) throws IOException, HttpException {
        int ret = super.executeMethod(method);
        lastExecutedMethod = method;
        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.httpclient.HttpClient#executeMethod(org.apache.commons.httpclient.HostConfiguration,
     * org.apache.commons.httpclient.HttpMethod)
     */
    @Override
    public int executeMethod(HostConfiguration hostConfiguration, HttpMethod method) throws IOException, HttpException {
        int ret = super.executeMethod(hostConfiguration, method);
        lastExecutedMethod = method;
        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.httpclient.HttpClient#executeMethod(org.apache.commons.httpclient.HostConfiguration,
     * org.apache.commons.httpclient.HttpMethod, org.apache.commons.httpclient.HttpState)
     */
    @Override
    public int executeMethod(HostConfiguration hostconfig, HttpMethod method, HttpState state) throws IOException, HttpException {
        int ret = super.executeMethod(hostconfig, method, state);
        lastExecutedMethod = method;
        return ret;
    }

    /**
     * @return the lastExecutionResponseCode
     */
    public int getLastExecutionResponseCode() {
        return lastExecutedMethod == null ? 0 : lastExecutedMethod.getStatusCode();
    }

    /**
     * @return the lastExecutionResponseText
     */
    public String getLastExecutionResponseText() {
        return lastExecutedMethod == null ? "" : lastExecutedMethod.getStatusText();
    }

    /**
     * @return the lastExecutedMethod
     */
    public HttpMethod getLastExecutedMethod() {
        return lastExecutedMethod;
    }
}
