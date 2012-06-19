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
package eionet.cr.web.util.columns;

import java.text.SimpleDateFormat;

import eionet.cr.web.action.AbstractActionBean;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class SearchResultColumn {

    protected static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
    protected static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("dd.MM.yy");

    /** */
    private String title;
    private boolean isSortable;

    /** */
    private boolean escapeXml;
    private String actionRequestParameter;

    /** */
    protected AbstractActionBean actionBean;

    /**
     *
     */
    public SearchResultColumn() {
        // blank constructor
    }

    /**
     * @param title
     * @param isSortable
     */
    public SearchResultColumn(String title, boolean isSortable) {
        this.title = title;
        this.isSortable = isSortable;
    }

    /**
     *
     * @param object
     * @return
     */
    public abstract String format(Object object);

    /**
     *
     * @param escapeXml
     */
    public void setEscapeXml(boolean escapeXml) {
        this.escapeXml = escapeXml;
    }

    /**
     *
     * @return
     */
    public abstract String getSortParamValue();

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the isSortable
     */
    public boolean isSortable() {
        return isSortable;
    }

    /**
     * @param isSortable the isSortable to set
     */
    public void setSortable(boolean isSortable) {
        this.isSortable = isSortable;
    }

    /**
     * @return the actionRequestParameter
     */
    public String getActionRequestParameter() {
        return actionRequestParameter;
    }

    /**
     * @param actionRequestParameter the actionRequestParameter to set
     */
    public void setActionRequestParameter(String actionRequestParameter) {
        this.actionRequestParameter = actionRequestParameter;
    }

    /**
     * @return the actionBean
     */
    public AbstractActionBean getActionBean() {
        return actionBean;
    }

    /**
     * @param actionBean the actionBean to set
     */
    public void setActionBean(AbstractActionBean actionBean) {
        this.actionBean = actionBean;
    }
}
