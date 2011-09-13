<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Error">
    <stripes:layout-component name="contents">
        <h1>Error</h1>
        <c:choose>
            <c:when test="${not empty requestScope.exception}">
                <h4>message:</h4>
                <p><c:out value="${requestScope.exception}"/></p>
                <h4>stack trace:</h4>
                <p>${crfn:formatStackTrace(fn:escapeXml(crfn:getStackTrace(requestScope.exception)))}</p>
            </c:when>
            <c:otherwise>
                <p>But no error message found!</p>
            </c:otherwise>
        </c:choose>
    </stripes:layout-component>
</stripes:layout-render>
