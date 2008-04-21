<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.io.*"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvesting Sources">

	<stripes:layout-component name="errors"/>
	<stripes:layout-component name="messages"/>
	<stripes:layout-component name="contents">
	
	<div id="operations">
		<ul>
			<li><a href="pages/addsource.jsp">Add new source</a></li>
		</ul>
	</div>
	      			
	<h1>Harvesting sources</h1>
	             
	<display:table name="${actionBean.harvestSources}" class="sortable" pagesize="0" sort="list" id="harvestSource" htmlId="harvestSources" requestURI="${actionBean.urlBinding}">
	
		<display:column property="url" title="URL" sortable="true"/>
		<display:column property="type" title="Type" sortable="true"/>
		<display:column>
			<stripes:link href="/source.action" event="preViewHarvestSource">
				<img src="${pageContext.request.contextPath}/images/view2.gif" title="View"/>
				<stripes:param name="harvestSource.sourceId" value="${harvestSource.sourceId}"/>
			</stripes:link>
		</display:column>
		<display:column>
			<stripes:link href="/source.action" event="preEditHarvestSource">
				<img src="${pageContext.request.contextPath}/images/edit.gif" title="Edit"/>
				<stripes:param name="harvestSource.sourceId" value="${harvestSource.sourceId}"/>
			</stripes:link>
		</display:column>
		<display:column>
			<stripes:link href="/source.action" event="deleteHarvestSource" onclick="return confirm('Are you sure you want to delete this harvesting source');">
				<img src="${pageContext.request.contextPath}/images/delete.gif" title="Delete"/>
				<stripes:param name="harvestSource.sourceId" value="${harvestSource.sourceId}"/>
			</stripes:link>
		</display:column>
		
	</display:table>
                     
	</stripes:layout-component>
</stripes:layout-render>
