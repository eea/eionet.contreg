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

package eionet.cr.web.util;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

/**
 * An extension of {@link OutputStream} that wraps a given ServletResponse, and forwards all {@link OutputStream} method calls to
 * the same methods of {@link ServletOutputStream}. The point is that it does not initialize the latter before it is needed first
 * time. Hence the "lazy" in class name. Postponing this initialization to the latest possible moment allows clients of this class
 * to do things like response.sendError(int) as late as possible before the servlet output stream gets intialized, i.e. committed.
 *
 * @author jaanus
 */
public class ServletOutputLazyStream extends OutputStream {

    /** */
    private ServletResponse servletResponse;
    private ServletOutputStream servletOutputStream;

    /**
     * Constructor.
     * @param servletResponse The ServletResponse to wrap.
     */
    public ServletOutputLazyStream(ServletResponse servletResponse) {

        if (servletResponse == null) {
            throw new IllegalArgumentException("The given ServletResponse must not be null!");
        }
        this.servletResponse = servletResponse;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(int b) throws IOException {
        getServletOutputStream().write(b);
    }

    /*
     * (non-Javadoc)
     * @see java.io.OutputStream#write(byte[])
     */
    @Override
    public void write(byte[] b) throws IOException {
        getServletOutputStream().write(b);
    }

    /*
     * (non-Javadoc)
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        getServletOutputStream().write(b, off , len);
    }

    /*
     * (non-Javadoc)
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close() throws IOException {
        getServletOutputStream().close();
    }

    /*
     * (non-Javadoc)
     * @see java.io.OutputStream#flush()
     */
    @Override
    public void flush() throws IOException {
        getServletOutputStream().flush();
    }

    /**
     * A "lazy" getter for the wrapped ServletResponse's output stream.
     *
     * @return
     * @throws IOException
     */
    private ServletOutputStream getServletOutputStream() throws IOException {
        if (servletOutputStream == null) {
            servletOutputStream = servletResponse.getOutputStream();
        }
        return servletOutputStream;
    }
}
