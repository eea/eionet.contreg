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
		        	</tr>
	        	</thead>
	        	<tbody>
	        		<c:forEach items="${harvestSourceList.harvestSources}" var="harvestSource" varStatus="loop">
		        		<tr>
		        			<td>${harvestSource.identifier}</td>
		        			<td>${harvestSource.pullUrl}</td>
		        			<td>${harvestSource.type}</td>
		        			<td>${harvestSource.emails}</td>
		        			<td>${harvestSource.dateCreated}</td>
		        			<td>${harvestSource.creator}</td>
		        			<td>${harvestSource.statements}</td>
		        		</tr>
	        		</c:forEach>
	        	</tbody>
	        </table>
	</stripes:layout-component>
</stripes:layout-render>
