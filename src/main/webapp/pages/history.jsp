<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Registrations">
    <stripes:layout-component name="contents">

        <cr:tabMenu tabs="${actionBean.tabs}" />

        <br style="clear:left" />
        <c:if test="${actionBean.usersHistory}">
            <h1>My history</h1>
        </c:if>
        <c:if test="${!actionBean.usersHistory}">
            <h1>${actionBean.ownerName}'s history</h1>
        </c:if>
        <c:choose>
            <c:when test="${not empty actionBean.histories}">
                <display:table name="${actionBean.histories}" class="sortable"
                    pagesize="20" sort="list" id="history" htmlId="historylist"
                    requestURI="${actionBean.urlBinding}" style="width:100%">
                    <display:column title="Date" sortable="true" style="width:150px;">${history.lastOperation}
                                        </display:column>
                    <display:column title="URL" sortable="true">
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
