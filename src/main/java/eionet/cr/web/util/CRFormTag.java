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
package eionet.cr.web.util;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import net.sourceforge.stripes.exception.StripesJspException;

/**
 * Modified Stripes {@link net.sourceforge.stripes.tag.FormTag}. This form tag doesn't include _sourcePage and _fp parameters.
 *
 * @author Aleksandr Ivanov <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class CRFormTag extends net.sourceforge.stripes.tag.FormTag {

    /**
     * @see net.sourceforge.stripes.tag.FormTag#doEndTag() {@inheritDoc}
     */
    @Override
    public int doEndTag() throws JspException {
        try {
            // Default the method to post
            if (getMethod() == null) {
                setMethod("post");
            }

            set("method", getMethod());
            set("enctype", getEnctype());
            set("action", buildAction());

            JspWriter out = getPageContext().getOut();
            writeOpenTag(out, "form");
            if (getBodyContent() != null) {
                getBodyContent().writeOut(getPageContext().getOut());
            }

            if (isWizard()) {
                writeWizardFields();
            }

            writeCloseTag(getPageContext().getOut(), "form");
        } catch (IOException ioe) {
            throw new StripesJspException("IOException in FormTag.doEndTag().", ioe);
        }

        return EVAL_PAGE;

    }

}
