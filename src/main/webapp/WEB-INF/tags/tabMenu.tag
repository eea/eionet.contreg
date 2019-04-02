<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ attribute name="tabs" required="true" type="java.util.ArrayList" %>

<div id="tabbedmenu" class="tabbedmenu">
    <ul>
    <c:forEach items="${tabs}" var="tab">
        <c:choose>
            <c:when test="${tab.selected}">
                <li id="currenttab"><a class="current"><c:out value="${tab.title}" /></a></li>
            </c:when>
            <c:otherwise>
                <li>
                    <stripes:link href="${tab.href}"><c:out value="${tab.title}" />
                        <c:if test="${not empty tab.event}">
                            <stripes:param name="${tab.event}" />
                        </c:if>
                        <c:forEach var="item" items="${tab.params}">
                            <stripes:param name="${item.key}" value="${item.value}" />
                        </c:forEach>
                    </stripes:link>
                </li>
            </c:otherwise>
        </c:choose>
    </c:forEach>
    </ul>
</div>
