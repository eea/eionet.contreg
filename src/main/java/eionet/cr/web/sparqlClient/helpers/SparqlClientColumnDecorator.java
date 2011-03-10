package eionet.cr.web.sparqlClient.helpers;

import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import org.displaytag.decorator.TableDecorator;

public class SparqlClientColumnDecorator extends TableDecorator {

    public String getMap(String propertyName) throws Exception {

        Map<String, ResultValue> wm = (Map<String, ResultValue>) getCurrentRowObject();
        ResultValue obj = (ResultValue) wm.get(propertyName);

        if(obj != null && obj.isLiteral()){
            return obj.getValue();
        } else if(obj != null && !obj.isLiteral()) {

            HttpServletRequest req = (HttpServletRequest) getPageContext().getRequest();
            String path = req.getContextPath();

            String endpoint = (String) req.getParameter("endpoint");
            String query = (String) req.getParameter("query");

            endpoint = URLEncoder.encode(endpoint, "UTF-8");
            query = URLEncoder.encode(query, "UTF-8");
            String explore = URLEncoder.encode(obj.getValue(), "UTF-8");

            StringBuffer ret = new StringBuffer();
            ret.append("<a href=\"").append(path).append("/sparqlClient.action?explore=").append(explore).append("&amp;endpoint=").append(endpoint);
            if(query != null && query.length() > 0){
                ret.append("&amp;query=").append(query);
            }
            ret.append("\">").append(obj.getValue()).append("</a>");

            return ret.toString();

        } else {
            return "";
        }
    }

}
