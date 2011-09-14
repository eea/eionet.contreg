<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Last 100 Harvests Statistics">

    <stripes:layout-component name="contents">

    <c:choose>
        <c:when test="${actionBean.adminLoggedIn}">
            <div id="operations">
                <ul>
                    <li><stripes:link href="/admin">Back to Admin</stripes:link></li>
                </ul>
            </div>
            <h1>Last 100 Harvests</h1>
            <display:table name="${actionBean.resultList}" class="sortable" id="item" sort="list" requestURI="${actionBean.urlBinding}">
                <display:column property="sourceUrl" title="URL" sortable="true"/>
                <display:column title="Harvest start" sortable="true">
                    <fmt:formatDate value="${item.datetimeStarted}" pattern="yyyy-MM-dd HH:mm:ss"/>
                </display:column>
                <display:column property="duration" title="Duration (ms)" sortable="true" />
                <display:column property="totalStatements" title="Statements" sortable="true" />
                <display:column property="statementDuration" title="Statement duration (ms)" sortable="true" />
            </display:table>
        </c:when>
        <c:otherwise>
            <div class="error-msg">
            No Access
            </div>
        </c:otherwise>
    </c:choose>
    </stripes:layout-component>

</stripes:layout-render>
