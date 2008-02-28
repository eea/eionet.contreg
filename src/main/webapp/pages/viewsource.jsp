<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.io.*,eionet.cr.dao.DAOFactory"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="View Harvesting Source">
	<stripes:layout-component name="errors"/>
	<stripes:layout-component name="messages"/>
	<stripes:layout-component name="contents">
		<h1>View source</h1>
	    <stripes:form action="/source.action" focus="">
	    	<stripes:hidden name="harvestSource.sourceId"/>
	        <table>
	            <tr>
	                <td>Name:</td>
	                <td>${actionBean.harvestSource.name}</td>
	            </tr>
	            <tr>
	                <td>URL:</td>
	                <td>${actionBean.harvestSource.url}</td>
	            </tr>
	            <tr>
	                <td>Type:</td>
	                <td>
	                	${actionBean.harvestSource.type}
	                </td>
	            </tr>
	            <tr>
	                <td>E-mails:</td>
	                <td>${actionBean.harvestSource.emails}</td>
	            </tr>
	            <tr>
	                <td>Date created:</td>
	                <td>
	                	${actionBean.harvestSource.dateCreated}
	                </td>
	            </tr>
	            <tr>
	                <td>Creator:</td>
	                <td>
	                	${actionBean.harvestSource.creator}
	                </td>
	            </tr>
	            <tr>
	                <td>Statements harvested:</td>
	                <td>&nbsp;</td>
	            </tr>
	            <tr>
	                <td>Schedule:</td>
	                <td>
	                	weekday: 
	                	${actionBean.harvestSource.harvestSchedule.weekday}<br/> 
	                	hour: 
	                	${actionBean.harvestSource.harvestSchedule.hour} <br/>
	                	period (weeks): 
	                	${actionBean.harvestSource.harvestSchedule.period}
	                </td>
	            </tr>
	            <tr>
	                <td colspan="2">
	                	<stripes:submit name="harvestNow" value="Harvest now"/>
	                    <stripes:submit name="scheduleImmediateHarvest" value="Schedule for immediate harvest"/>
	                    <!--stripes:submit name="push" value="Push from local file"/-->
	                </td>
	            </tr>
	        </table>
	        <br/>
	        Last 10 harvests:
	        <table class="datatable">
	        	<thead>
		        	<tr>
		        		<th scope="col">Type</th>
		        		<th scope="col">User</th>
		        		<th scope="col">Started</th>
		        		<th scope="col">Finished</th>
		        		<th scope="col">Resources</th>
		        		<th scope="col">Encoding schemes</th>
		        		<th scope="col">Statements</th>
		        	</tr>
	        	</thead>
	        	<tbody>
	        		<c:forEach items="${actionBean.harvests}" var="harv" varStatus="loop">
	        			<tr>
	        				<td>
	        					<stripes:link href="/source.action" event="preViewHarvest">
	                                ${harv.harvestType}
	                                <stripes:param name="harvest.harvestId" value="${harv.harvestId}"/>
	                                <stripes:param name="harvest.harvestSourceId" value="${harv.harvestSourceId}"/>
	                            </stripes:link>
	        				</td>
	        				<td>${harv.user}</td>
	        				<td><fmt:formatDate value="${harv.datetimeStarted}" pattern="dd MMM yy HH:mm:ss"/></td>
	        				<td><fmt:formatDate value="${harv.datetimeFinished}" pattern="dd MMM yy HH:mm:ss"/></td>		        				
	        				<td>${harv.totalResources}</td>
	        				<td>${harv.encodingSchemes}</td>
	        				<td>${harv.totalStatements}</td>
	        			</tr>
	        		</c:forEach>
	        	</tbody>
	        </table>
	    </stripes:form>

	</stripes:layout-component>
</stripes:layout-render>
