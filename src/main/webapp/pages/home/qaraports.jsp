<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="QA Raports">

<stripes:layout-component name="contents">

	<h1>My ${actionBean.section}</h1>

	<div id="operations">
		<ul>
			<li>
				<stripes:link href="/qaraports.action" event="add">Add new QA raport</stripes:link>
			</li>
		</ul>
	</div>

	<c:choose>
		<c:when test="${not empty actionBean.raportsListing}">
			<display:table name="">
				<display:column title="Date" sortable="false" style="width:150px;"></display:column>
			</display:table>
		</c:when>
		<c:otherwise>
			<p>No QA Raports found.</p>
		</c:otherwise>
	</c:choose>
</stripes:layout-component>


</stripes:layout-render>