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
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */
package eionet.cr.web.action;

import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.StreamingResolution;


/**
 * Streaming resolution for sending
 * back HTTP errors to external clients in plain/text format.
 *
 * @author kaido
 */
public class ErrorStreamingResolution extends StreamingResolution {
    /** HTTP error code. */
    private int errorCode;

    /** Error message sent to the stream .*/
    private String errorMessage;

    /**
     * Creates a new resolution.
     * @param errCode HTTP Error code to be returned
     * @param errMsg Error message
     */
    public ErrorStreamingResolution(int errCode, String errMsg) {
        super("text/plain");

        this.errorCode = errCode;
        this.errorMessage = errMsg;

    }

    @Override
    protected void applyHeaders(HttpServletResponse response) {
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-type", "text/plain");
    }

    @Override
    protected void stream(HttpServletResponse response) throws Exception {
        response.setStatus(errorCode);
        response.getWriter().write(errorMessage);
    }

}
