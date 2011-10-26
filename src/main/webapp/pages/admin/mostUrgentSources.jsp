<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>


<%@page import="net.sourceforge.stripes.action.ActionBean"%><stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvesting Statistics">

    <stripes:layout-component name="contents">

    <c:choose>
        <c:when test="${actionBean.adminLoggedIn}">

            <div id="operations">
	            <ul>
	                <li><stripes:link href="/admin">Back to admin page</stripes:link></li>
	            </ul>
            </div>

            <h1>Most urgent harvest sources</h1>

            <crfn:form action="${actionBean.urlBinding}" method="get" style="margin-top:2em">
	            <div>
	                <stripes:label for="limitText" class="question">Number of sources to list:</stripes:label>
	                <stripes:text name="limit" id="limitText" value="${actionBean.limit}" size="4"/>
	                <stripes:submit name="filter" value="Go" id="filterButton"/>
	            </div>
            </crfn:form>

            <div class="advice-msg" style="margin-top:1em">
	            Note: number of sources with harvest urgency score above <fmt:formatNumber value="${actionBean.urgencyThreshold}" pattern="#.###"/> is ${actionBean.noOfSourcesAboveUrgencyThreshold}.
            </div>

            <display:table name="${actionBean.sources}" class="datatable" id="source" requestURI="${actionBean.urlBinding}" style="width:100%">

                <display:column property="url" title="URL"/>
                <display:column property="lastHarvest" title="Last harvest" format="{0,date,dd.MM.yy HH:mm:ss}"/>
                <display:column property="intervalMinutes" title="Interval (min)"/>
                <display:column property="harvestUrgencyScore" title="Urgency" format="{0,number,#.###}"/>

                <display:caption style="text-align:left;font-weight:normal;">${fn:length(actionBean.sources)} sources found</display:caption>
            </display:table>

        </c:when>
        <c:otherwise>
            <div class="error-msg">Access not allowed!</div>
        </c:otherwise>
    </c:choose>

    </stripes:layout-component>

</stripes:layout-render>
