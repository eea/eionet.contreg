<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Shared SPARQL bookmarks">

    <stripes:layout-component name="contents">

        <h1>Shared bookmarked SPARQL queries</h1>
        <c:choose>
            <c:when test="${not empty actionBean.sharedSparqlBookmars}">
                <ul>
                <c:forEach items="${actionBean.sharedSparqlBookmars}" var="bookmarkedQuery">
                    <li>
                        <stripes:link href="/sparql" title="${bookmarkedQuery.queryString}">
                            <stripes:param name="fillfrom" value="${bookmarkedQuery.subj}" />
                            <stripes:param name="selectedBookmarkName" value="${bookmarkedQuery.label}" />
                            <c:out value="${bookmarkedQuery.label}"/>
                        </stripes:link>
                    </li>
                </c:forEach>
                </ul>
            </c:when>
            <c:otherwise>
                No bookmarked queries found.
            </c:otherwise>
        </c:choose>

    </stripes:layout-component>
</stripes:layout-render>