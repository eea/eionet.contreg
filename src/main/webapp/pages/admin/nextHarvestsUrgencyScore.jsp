<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	


<%@page import="net.sourceforge.stripes.action.ActionBean"%><stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvesting Statistics">

	<stripes:layout-component name="contents">
	
	<c:choose>
		<c:when test="${actionBean.adminLoggedIn}">
			<stripes:link href="/admin">Back to Admin</stripes:link><br><br>
			<h1>Urgency score for the next ${actionBean.urgencyScoreLimit} harvests</h1>
			<crfn:form action="" method="get">
				<stripes:label for="urgencyScoreLimitValue" class="question">Urgency score Limit #</stripes:label>
				<stripes:text name="urgencyScoreLimit" id="urgencyScoreLimitValue" value="${actionBean.urgencyScoreLimit}" size="10"/> 
				<stripes:submit name="filter" value="Filter" id="filterButton"/>
				<stripes:text name="dummy" style="visibility:hidden;display:none" disabled="disabled" size="1"/>
			</crfn:form>
			<br>
			<b>Results found:</b> ${actionBean.resultsFound}
			<br>
			
			<stripes:layout-render name="/pages/common/subjectsResultList.jsp" tableClass="sortable"/>			
		</c:when>
		<c:otherwise>
			<h1>No Access</h1>
		</c:otherwise>	
	</c:choose>
	</stripes:layout-component>

</stripes:layout-render>