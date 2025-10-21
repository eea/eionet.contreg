package eionet.cr.web.sparqlClient.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.displaytag.decorator.TableDecorator;

import eionet.cr.dao.virtuoso.VirtuosoBaseDAO;
import eionet.cr.web.action.factsheet.FactsheetActionBean;

/**
 * A Dispkyatg decorator for the table cells of the SPARQL ednpoint query results.
 *
 * @author jaanus
 */
public class SparqlClientColumnDecorator extends TableDecorator {

    /** The default factsheet URL binding if cannot be detected from class annotations. */
    private static final String DEFAULT_FACTSHEET_URL_BINDING = "/factsheet.action";

    /** The factsheet URL binding to be used in this instance. */
    private String factsheetUrlBinding;

    /**
     * Called by Displaytag upon displaying the given property of a given result set row. Returns thr property's "decorated" value.
     *
     * @param propertyName The given property.
     * @return As indicated above.
     * @throws UnsupportedEncodingException Thrown when URL-encoding is done with unsupported character encoding.
     */
    @SuppressWarnings("unchecked")
    public String getMap(String propertyName) throws UnsupportedEncodingException {

        Map<String, ResultValue> currRowMap = (Map<String, ResultValue>) getCurrentRowObject();
        return getDecoratedValue(currRowMap, propertyName, ((HttpServletRequest) getPageContext().getRequest()).getContextPath());
    }

    /**
     * Convenience method, that returns the "decorated" value of the given property in the given row-map.
     *
     * @param rowMap The given row-map.
     * @param propertyName The given property.
     * @param webappContextPath Context path of the current web-app.
     * @return The "decorated" value.
     * @throws UnsupportedEncodingException Thrown when URL-encoding is done with unsupported character encoding.
     */
    protected String getDecoratedValue(Map<String, ResultValue> rowMap, String propertyName, String webappContextPath)
            throws UnsupportedEncodingException {
        ResultValue obj = rowMap.get(propertyName);
        if (obj == null) {
            return "";
        } else {
            if (obj.isLiteral()) {
                return StringEscapeUtils.escapeXml(obj.getValue());
            } else {
                String uri = obj.getValue();
                if (obj.isAnonymous()) {
                    uri = VirtuosoBaseDAO.VIRTUOSO_BNODE_PREFIX + uri;
                }
                uri = URLEncoder.encode(uri, "UTF-8");

                if (webappContextPath == null) {
                    webappContextPath = StringUtils.EMPTY;
                }

                StringBuffer ret = new StringBuffer();
                ret.append("<a href=\"").append(webappContextPath).append(getFactsheetUrlBinding()).append("?uri=").append(uri);
                ret.append("\">").append(StringEscapeUtils.escapeXml(obj.getValue())).append("</a>");

                return ret.toString();
            }
        }
    }

    /**
     * Lazy getter for the {@link #factsheetUrlBinding}.
     *
     * @return
     */
    private String getFactsheetUrlBinding() {

        if (factsheetUrlBinding == null) {

            factsheetUrlBinding = DEFAULT_FACTSHEET_URL_BINDING;
            UrlBinding urlBindingAnnotation = FactsheetActionBean.class.getAnnotation(UrlBinding.class);
            if (urlBindingAnnotation != null) {
                String bindingValue = urlBindingAnnotation.value();
                if (bindingValue != null) {
                    factsheetUrlBinding = bindingValue;
                }
            }
        }

        return factsheetUrlBinding;
    }
}
