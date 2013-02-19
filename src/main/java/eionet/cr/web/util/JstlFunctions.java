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
package eionet.cr.web.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.QueryString;
import eionet.cr.util.SortOrder;
import eionet.cr.util.Util;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.action.factsheet.FactsheetActionBean;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.columns.SearchResultColumn;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public final class JstlFunctions {

    /**
     * Hide utility class constructor.
     */
    private JstlFunctions() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     * Parses the given string with a whitespace tokenizer and looks up the first token whose length exceeds <tt>cutAtLength</tt>.
     * If such a token is found, returns the given string's <code>substring(0, i + cutAtLength) + "..."</code>, where <code>i</code>
     * is the start index of the found token. If no tokens are found that exceed the length of <tt>cutAtLength</tt>, then this
     * method simply return the given string.
     *
     * @return
     */
    public static java.lang.String cutAtFirstLongToken(java.lang.String str, int cutAtLength) {

        if (str == null) {
            return "";
        }

        String firstLongToken = null;
        StringTokenizer st = new StringTokenizer(str);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.length() > cutAtLength) {
                firstLongToken = token;
                break;
            }
        }

        if (firstLongToken != null) {
            int i = str.indexOf(firstLongToken);
            StringBuffer buf = new StringBuffer(str.substring(0, i + cutAtLength));
            return buf.append("...").toString();
        } else {
            return str;
        }
    }

    /**
     * Checks if the given string (after being trimmed first) contains any whitespace. If yes, returns the given string surrounded
     * by quotes. Otherwise returns the given string. If the given string is <code>null</code>, returns null.
     *
     * @param str
     * @return
     */
    public static java.lang.String addQuotesIfWhitespaceInside(java.lang.String str) {

        if (str == null || str.trim().length() == 0) {
            return str;
        }

        if (!Util.hasWhiteSpace(str.trim())) {
            return str;
        } else {
            return "\"" + str + "\"";
        }
    }

    /**
     * Returns the value of {@link CRUser#hasPermission(HttpSession, String, String)}, using the given inputs.
     *
     * @param session
     * @param aclPath
     * @param permission
     * @return
     */
    public static boolean userHasPermission(HttpSession session, java.lang.String aclPath, java.lang.String permission) {
        return CRUser.hasPermission(session, aclPath, permission);
    }

    /**
     * Returns a string that is constructed by concatenating the given bean request's getRequestURI() + "?" + the given bean
     * request's getQueryString(), and replacing the sort predicate with the given one. The present sort order is replaced by the
     * opposite.
     *
     * @param request
     * @param sortP
     * @param sortO
     * @return
     */
    public static String sortUrl(AbstractActionBean actionBean, SearchResultColumn column) {

        HttpServletRequest request = actionBean.getContext().getRequest();
        StringBuffer buf = new StringBuffer(actionBean.getUrlBinding());
        buf.append("?");
        if (StringUtils.isBlank(column.getActionRequestParameter())) {

            if (!StringUtils.isBlank(request.getQueryString())) {

                QueryString queryString = QueryString.createQueryString(request);
                queryString.removeParameters(actionBean.excludeFromSortAndPagingUrls());
                buf.append(queryString.toURLFormat());
            }
        } else {
            buf.append(column.getActionRequestParameter());
        }

        String sortParamValue = column.getSortParamValue();
        if (sortParamValue == null) {
            sortParamValue = "";
        }

        String curValue = request.getParameter("sortP");
        if (curValue != null && buf.indexOf("sortP=") > 0) {
            buf =
                    new StringBuffer(StringUtils.replace(buf.toString(), "sortP=" + Util.urlEncode(curValue),
                            "sortP=" + Util.urlEncode(sortParamValue)));
        } else {
            buf.append("&amp;sortP=").append(Util.urlEncode(sortParamValue));
        }

        curValue = request.getParameter("sortO");
        if (curValue != null && buf.indexOf("sortO=") > 0) {
            buf =
                    new StringBuffer(StringUtils.replace(buf.toString(), "sortO=" + curValue,
                            "sortO=" + SortOrder.oppositeSortOrder(curValue)));
        } else {
            buf.append("&amp;sortO=").append(SortOrder.oppositeSortOrder(curValue));
        }

        String result = buf.toString();
        return result.startsWith("/") ? result.substring(1) : result;
    }

    /**
     * Finds the label for the given predicate in the given predicate-label map. If there is no match, then looks for the last
     * occurrence of '#' or '/' or ':' in the predicate. If such an occurrence is found, returns everything after that occurrence.
     * Otherwise returns the predicate as it was given.
     *
     * @param predicateLabels
     * @param predicate
     * @return
     */
    public static String getPredicateLabel(Map predicateLabels, String predicate) {

        Object o = predicateLabels == null ? null : predicateLabels.get(predicate);
        String label = o == null ? null : o.toString();
        if (StringUtils.isBlank(label)) {
            int last = Math.max(Math.max(predicate.lastIndexOf('#'), predicate.lastIndexOf('/')), predicate.lastIndexOf(':'));
            if (last >= 0) {
                label = predicate.substring(last + 1);
            }
        }

        return StringUtils.isBlank(label) ? predicate : label;
    }

    /**
     *
     * @param subjectDTO
     * @param predicates
     * @param object
     * @return
     */
    public static boolean subjectHasPredicateObject(SubjectDTO subjectDTO, Set predicates, String object) {

        boolean result = false;

        if (predicates == null) {
            return result;
        }

        for (Iterator i = predicates.iterator(); i.hasNext();) {
            if (subjectDTO.hasPredicateObject(i.next().toString(), object)) {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     *
     * @param objects
     * @param findObjectHash
     * @return
     */
    public static boolean isSourceToAny(long objectHash, Collection objects) {

        boolean result = false;
        for (Iterator i = objects.iterator(); i.hasNext();) {
            ObjectDTO objectDTO = (ObjectDTO) i.next();
            if (objectHash == objectDTO.getSourceObjectHash()) {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * Returns a color for the given source by supplying the source's hash to the <code>Colors.colorByModulus(long)</code>.
     *
     * @param source
     * @return
     */
    public static String colorForSource(String source) {

        return Colors.toKML(Colors.colorByModulus(Hashes.spoHash(source == null ? "" : source)), false);
    }

    /**
     *
     * @param s
     * @return
     */
    public static String urlEncode(String s) {

        return Util.urlEncode(s);
    }

    /**
     *
     * @param column
     * @param object
     * @param actionBean
     * @return
     */
    public static String format(SearchResultColumn column, Object object, AbstractActionBean actionBean) {
        column.setActionBean(actionBean);
        return column.format(object);
    }

    /**
     *
     * @param s
     * @return
     */
    public static long spoHash(String s) {
        return Hashes.spoHash(s);
    }

    /**
     *
     * @param subject
     * @param predicate
     * @return
     */
    public static String getObjectLiteral(SubjectDTO subject, String predicate) {

        if (subject == null) {
            return "";
        }

        ObjectDTO object = subject.getObject(predicate, ObjectDTO.Type.LITERAL);
        return object == null ? "" : object.getValue();
    }

    /**
     *
     * @param actionBean
     * @param object
     * @return
     */
    public static boolean isObjectInAcceptedLanguage(AbstractActionBean actionBean, ObjectDTO object) {

        return actionBean.getAcceptedLanguages().contains(Util.normalizeHTTPAcceptedLanguage(object.getLanguage()));
    }

    /**
     *
     * @param object
     * @return
     */
    public static String rawModeTitle(ObjectDTO object, Collection allObjects) {

        StringBuffer buf = new StringBuffer();
        if (object != null) {

            buf.append("[Type: ")
            .append(object.isLiteral() ? "Literal" : object.isAnonymous() ? "Anonymous resource" : "Resource");
            buf.append("]   [Inferred from object: ").append(getMatchingObjectValue(object.getSourceObjectHash(), allObjects));
            buf.append("]   [Inferred from source: ").append(
                    StringUtils.isBlank(object.getDerivSourceUri()) ? object.getDerivSourceHash() : object.getDerivSourceUri());
            buf.append("]");
        }
        return buf.toString();
    }

    /**
     *
     * @param hash
     * @param objects
     * @return
     */
    private static String getMatchingObjectValue(long hash, Collection objects) {

        String result = String.valueOf(hash);
        if (hash != 0 && objects != null && !objects.isEmpty()) {
            for (Object o : objects) {
                ObjectDTO object = (ObjectDTO) o;
                if (object.getHash() == hash) {
                    result = object.getValue();
                    break;
                }
            }
        }
        return result;

    }

    /**
     *
     * @param objectValue
     * @param pageContext
     * @return
     */
    public static boolean isObjectValueDisplayed(String predicate, String objectValue, PageContext pageContext) {

        boolean result = false;
        if (predicate != null) {

            String previousPredicate = (String) pageContext.getAttribute("prevPredicate");
            HashSet<String> objectValues = (HashSet<String>) pageContext.getAttribute("displayedObjectValues");
            if (objectValues == null || previousPredicate == null || !predicate.equals(previousPredicate)) {
                objectValues = new HashSet<String>();
                pageContext.setAttribute("displayedObjectValues", objectValues);
            }

            result = objectValues.contains(objectValue);
            pageContext.setAttribute("prevPredicate", predicate);
            objectValues.add(objectValue);
        }

        return result;
    }

    /**
     *
     * @param throwable
     * @return
     */
    public static String getStackTrace(Throwable throwable) {
        return Util.getStackTrace(throwable);
    }

    /**
     *
     * @param stackTrace
     * @return
     */
    public static String formatStackTrace(String stackTrace) {

        if (stackTrace == null || stackTrace.trim().length() == 0) {
            return stackTrace;
        }

        StringBuilder buf = new StringBuilder();
        StringTokenizer lines = new StringTokenizer(stackTrace, "\r\n");
        while (lines != null && lines.hasMoreElements()) {
            String line = lines.nextToken();
            line = StringUtils.replaceOnce(line, "\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
            buf.append(line).append("<br/>");
        }

        return buf.length() == 0 ? stackTrace : buf.toString();
    }

    /**
     *
     * @param factsheetActionBean
     * @param predicateUri
     * @param pageNumber
     * @return
     */
    public static String predicateExpandLink(FactsheetActionBean factsheetActionBean, String predicateUri, int pageNumber) {

        StringBuilder link = new StringBuilder(predicateCollapseLink(factsheetActionBean, predicateUri));
        if (pageNumber > 0) {

            try {
                link.append("&").append(FactsheetActionBean.PAGE_PARAM_PREFIX).append(pageNumber).append("=")
                .append(URLEncoder.encode(predicateUri, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new CRRuntimeException("Unsupported encoding", e);
            }
        }
        return link.toString();
    }

    /**
     *
     * @param factsheetActionBean
     * @param predicateUri
     * @return
     */
    public static String predicateCollapseLink(FactsheetActionBean factsheetActionBean, String predicateUri) {

        StringBuilder link = new StringBuilder();
        link.append(FactsheetActionBean.class.getAnnotation(UrlBinding.class).value()).append("?");

        HttpServletRequest request = factsheetActionBean.getContext().getRequest();
        Map<String, String[]> paramsMap = request.getParameterMap();
        if (paramsMap != null && !paramsMap.isEmpty()) {

            for (Map.Entry<String, String[]> entry : paramsMap.entrySet()) {

                String paramName = entry.getKey();
                String[] paramValues = entry.getValue();
                if (paramValues == null || paramValues.length == 0) {
                    try {
                        link.append(URLEncoder.encode(paramName, "UTF-8")).append("&");
                    } catch (UnsupportedEncodingException e) {
                        throw new CRRuntimeException("Unsupported encoding", e);
                    }
                } else {
                    boolean isPageParam = factsheetActionBean.isPredicatePageParam(paramName);
                    for (String paramValue : paramValues) {
                        if (!isPageParam || !paramValue.equals(predicateUri)) {
                            try {
                                link.append(URLEncoder.encode(paramName, "UTF-8")).append("=")
                                .append(URLEncoder.encode(paramValue, "UTF-8")).append("&");
                            } catch (UnsupportedEncodingException e) {
                                throw new CRRuntimeException("Unsupported encoding", e);
                            }
                        }
                    }
                }
            }
        }

        return StringUtils.removeEnd(link.toString(), "&");
    }

    /**
     *
     * @param matchCount
     * @param pageSize
     * @return
     */
    public static int numberOfPages(int matchCount, int pageSize) {

        int result = matchCount / pageSize;
        if (matchCount % pageSize != 0) {
            result = result + 1;
        }

        return result;
    }

    /**
     *
     * @param condition
     * @param ifTrue
     * @param ifFalse
     * @return
     */
    public static Object conditional(boolean condition, Object ifTrue, Object ifFalse) {

        return condition == true ? ifTrue : ifFalse;
    }

    /**
     * Removes root home URI part from given URI
     *
     * @param uri
     * @return String
     */
    public static String removeHomeUri(String uri) {
        if (uri == null) {
            return "";
        }
        String homeUri = GeneralConfig.getProperty(GeneralConfig.APPLICATION_HOME_URL) + "/home";
        return uri.replace(homeUri, "");
    }

    /**
     * Extract folder
     *
     * @param uri
     * @return String
     */
    public static String extractFolder(String uri) {
        if (uri == null) {
            return "";
        }
        String appHome = GeneralConfig.getProperty(GeneralConfig.APPLICATION_HOME_URL);
        return StringUtils.substringAfter(uri, appHome);
    }

    /**
     *
     * @param s
     * @return
     */
    public static String escapeHtml(String s) {

        return StringEscapeUtils.escapeHtml(s);
    }

    /**
     *
     * @param arrayOrCollection
     * @param object
     * @return
     */
    public static boolean contains(Object arrayOrCollection, Object object) {

        if (arrayOrCollection != null) {

            if (arrayOrCollection instanceof Object[]) {
                for (Object o : ((Object[]) arrayOrCollection)) {
                    if (o.equals(object)) {
                        return true;
                    }
                }
            } else if (arrayOrCollection instanceof Collection) {
                for (Object o : ((Collection) arrayOrCollection)) {
                    if (o.equals(object)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
