<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	


<%@page import="net.sourceforge.stripes.action.ActionBean"%><stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvesting Statistics">

	<stripes:layout-component name="contents">
	
	<c:choose>
		<c:when test="${actionBean.adminLoggedIn}">
			<h1>CR Admin area</h1><br>
			<stripes:link href="/admin/harvestedurl">Harvested Urls</stripes:link><br>
			<stripes:link href="/admin/nhus">Next Harvest Urgency Score </stripes:link><br>
		</c:when>
		<c:otherwise>
			<h1>No Access</h1>
		</c:otherwise>	
	</c:choose>
	</stripes:layout-component>

</stripes:layout-render>