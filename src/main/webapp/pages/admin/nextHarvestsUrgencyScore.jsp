<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>


<%@page import="net.sourceforge.stripes.action.ActionBean"%><stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvesting Statistics">

    <stripes:layout-component name="contents">

    <c:choose>
        <c:when test="${actionBean.adminLoggedIn}">
            <div id="operations">
            <ul>
                <li><stripes:link href="/admin">Back to Admin</stripes:link></li>
            </ul>
            </div>
            <h1>Urgency score for the next ${actionBean.urgencyScoreLimit} harvests</h1>
            <crfn:form action="" method="get">
            <p>
                <stripes:label for="urgencyScoreLimitValue" class="question">Urgency score Limit #</stripes:label>
                <stripes:text name="urgencyScoreLimit" id="urgencyScoreLimitValue" value="${actionBean.urgencyScoreLimit}" size="4"/>
                <stripes:submit name="filter" value="Filter" id="filterButton"/>
                <stripes:text name="dummy" style="visibility:hidden;display:none" disabled="disabled" size="1"/>
            </p>
            </crfn:form>
            <div class="advice-msg">
            Results found: ${actionBean.resultsFound}
            </div>
            <stripes:layout-render name="/pages/common/subjectsResultList.jsp" tableClass="datatable"/>
        </c:when>
        <c:otherwise>
            <div class="error-msg">
            No Access
            </div>
        </c:otherwise>
    </c:choose>
    </stripes:layout-component>

</stripes:layout-render>
