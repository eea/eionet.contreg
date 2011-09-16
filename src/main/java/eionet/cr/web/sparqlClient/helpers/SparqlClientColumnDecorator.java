package eionet.cr.web.sparqlClient.helpers;

import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.displaytag.decorator.TableDecorator;

public class SparqlClientColumnDecorator extends TableDecorator {

    public String getMap(String propertyName) throws Exception {

        Map<String, ResultValue> wm = (Map<String, ResultValue>) getCurrentRowObject();
        ResultValue obj = (ResultValue) wm.get(propertyName);

        if (obj == null) {
            return "";
        } else {
            if (obj.isLiteral()) {
                return StringEscapeUtils.escapeXml(obj.getValue());
            } else {
                /* NOTE: Probably don't need the context path. Could use relative links */
                HttpServletRequest req = (HttpServletRequest) getPageContext().getRequest();
                String path = req.getContextPath();

                String uri = URLEncoder.encode(obj.getValue(), "UTF-8");

                StringBuffer ret = new StringBuffer();
                ret.append("<a href=\"").append(path);
                ret.append("/factsheet.action?uri=").append(uri);
                ret.append("\">").append(obj.getValue()).append("</a>");

                return ret.toString();
            }
        }
    }
}
