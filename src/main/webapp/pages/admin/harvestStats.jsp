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
                <display:column title="URL" sortable="true" sortProperty="sourceUrl">
                    <stripes:link beanclass="${actionBean.harvestDetailsBeanClass.name}">
                        <stripes:param name="harvestDTO.harvestId" value="${item.harvestId}"/><c:out value="${item.sourceUrl}" />
                    </stripes:link>
                </display:column>
                <display:column title="Started" sortable="true">
                    <fmt:formatDate value="${item.datetimeStarted}" pattern="yyyy-MM-dd HH:mm:ss"/>
                </display:column>
                <display:column title="Duration" sortable="true" sortProperty="duration">
                    <c:choose>
                        <c:when test="${item.duration == null}">N/A</c:when>
                        <c:otherwise>&#126;&nbsp;${item.duration}&nbsp;sec</c:otherwise>
                    </c:choose>
                </display:column>
                <display:column property="totalStatements" title="Statements" sortable="true" />
                <display:column title="Duration/statements" sortable="true" sortProperty="durationStatementsRatio">
                    <c:choose>
                        <c:when test="${item.durationStatementsRatio == null}">N/A</c:when>
                        <c:otherwise>&#126;&nbsp;<fmt:formatNumber type="number" pattern="0.000" value="${item.durationStatementsRatio}"/></c:otherwise>
                    </c:choose>
                </display:column>
            </display:table>
        </c:when>
        <c:otherwise>
            <div class="error-msg">Access not allowed!</div>
        </c:otherwise>
    </c:choose>
    </stripes:layout-component>

</stripes:layout-render>
