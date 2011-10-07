<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ attribute name="tabs" required="true" type="java.util.ArrayList" %>

<div id="tabbedmenu">
    <ul>
    <c:forEach items="${tabs}" var="tab">
        <c:choose>
            <c:when test="${tab.selected}">
                <li id="currenttab"><span><c:out value="${tab.title}" /></span></li>
            </c:when>
            <c:otherwise>
                <c:url value="${tab.url}" var="url" />
                <li><a href="${url}"><c:out value="${tab.title}" /></a></li>
            </c:otherwise>
        </c:choose>
    </c:forEach>
    </ul>
</div>
