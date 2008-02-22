<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.io.*"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvesting Sources">
	<stripes:layout-component name="errors"/>
	<stripes:layout-component name="messages"/>
	<stripes:layout-component name="contents">
	
	<jsp:useBean id="harvestSourceList" scope="page"
                     class="eionet.cr.web.action.HarvestSourceListActionBean"/>
                     
		<h1>Harvesting sources</h1>
	        <table class="datatable">
	        	<thead>
		        	<tr>
		        		<th scope="col">Identifier</th>
		        		<th scope="col">Pull URL</th>
		        		<th scope="col">Type</th>
		        		<th scope="col">E-mails</th>
		        		<th scope="col">Date Created</th>
		        		<th scope="col">Creator</th>
		        		<th scope="col">Statements</th>
		        		<th scope="col"></th>
		        		<th scope="col"></th>
		        		<th scope="col"></th>
		        	</tr>
	        	</thead>
	        	<tbody>
	        		<c:forEach items="${harvestSourceList.harvestSources}" var="source" varStatus="loop">
		        		<tr>
		        			<td>${source.identifier}</td>
		        			<td>${source.pullUrl}</td>
		        			<td>${source.type}</td>
		        			<td>${source.emails}</td>
		        			<td>${source.dateCreated}</td>
		        			<td>${source.creator}</td>
		        			<td>${source.statements}</td>
		        			<td><img src="${pageContext.request.contextPath}/images/view.gif" title="View"/></td>
		        			<td>
		        				<stripes:link href="/source.action" event="preEdit">
	                                <img src="${pageContext.request.contextPath}/images/edit.gif" title="Edit"/>
	                                <stripes:param name="harvestSource.sourceId" value="${source.sourceId}"/>
	                            </stripes:link>
		        			</td>
		        			<td><img src="${pageContext.request.contextPath}/images/delete.gif" title="Delete"/></td>
		        		</tr>
	        		</c:forEach>
	        	</tbody>
	        </table>
	</stripes:layout-component>
</stripes:layout-render>
