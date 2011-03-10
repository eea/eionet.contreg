<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="User History">

<stripes:layout-component name="contents">
    <c:if test="${ actionBean.userAuthorized}">
        <h1>My history</h1>
    </c:if>
    <c:if test="${ !actionBean.userAuthorized}">
        <h1>${actionBean.attemptedUserName}'s history</h1>
    </c:if>
    <c:choose>
        <c:when test="${not empty actionBean.history}">
            <display:table name="${actionBean.history}" class="sortable"
                pagesize="20" sort="list" id="history" htmlId="historylist"
                requestURI="${actionBean.urlBinding}" style="width:100%">
                <display:column title="Date" sortable="false" style="width:150px;">${history.lastOperation}
                                    </display:column>
                <display:column title="URL" sortable="false">
                    <stripes:link href="/factsheet.action">${history.url}
                                            <stripes:param name="uri" value="${history.url}" />
                    </stripes:link>
                </display:column>
            </display:table>
        </c:when>
        <c:otherwise>
            <p>No history found.</p>
        </c:otherwise>
    </c:choose>
</stripes:layout-component>
</stripes:layout-render>
