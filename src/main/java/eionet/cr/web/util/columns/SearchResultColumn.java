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

import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import eionet.cr.dao.virtuoso.VirtuosoBaseDAO;
import eionet.cr.util.Util;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.action.factsheet.FactsheetActionBean;

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

    /**
     * Builds a factsheet HTML link (relative to the webapp root) for the given URI.
     * Returns full
     *
     * <pre>
     * <a href="..." title="...">...</a>
     * </pre>
     *
     * tag.
     * URL encodings and XML escapings also preformed.
     *
     * @param uri The given URI.
     * @param isAnonymous True if the given URI is an anonymous resource.
     * @param label The link's displayed label.
     * @param showTitle If true then the given URI will be put into title="...", otherwise no title attribute rendered.
     * @return As indicated above.
     */
    String buildFactsheetLink(String uri, boolean isAnonymous, String label, boolean showTitle) {

        String factsheetUrlBinding = FactsheetActionBean.class.getAnnotation(UrlBinding.class).value();
        int i = factsheetUrlBinding.lastIndexOf("/");

        String uriParam = uri;
        if (isAnonymous) {
            if (uriParam.startsWith(VirtuosoBaseDAO.N3_BNODE_PREFIX)) {
                uriParam =
                        StringUtils.replaceOnce(uriParam, VirtuosoBaseDAO.N3_BNODE_PREFIX, VirtuosoBaseDAO.VIRTUOSO_BNODE_PREFIX);
            } else if (!uriParam.startsWith(VirtuosoBaseDAO.VIRTUOSO_BNODE_PREFIX)) {
                uriParam = VirtuosoBaseDAO.VIRTUOSO_BNODE_PREFIX + uriParam;
            }
        }

        StringBuffer href = new StringBuffer(i >= 0 ? factsheetUrlBinding.substring(i + 1) : factsheetUrlBinding).append("?");
        href.append("uri=").append(Util.urlEncode(uriParam));

        StringBuilder result = new StringBuilder("<a href=\"").append(href).append("\"");
        result.append(showTitle ? "title=\"" + StringEscapeUtils.escapeXml(uri) + "\">" : ">");
        result.append(StringEscapeUtils.escapeXml(label)).append("</a>");
        return result.toString();
    }
}
