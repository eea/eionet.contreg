<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.io.*,eionet.cr.dao.DAOFactory"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<%@ page import="eionet.cr.dto.HarvestBaseDTO" %>
<%@ page import="eionet.cr.harvest.Harvest" %>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="View Harvesting Source">
	<stripes:layout-component name="errors"/>
	<stripes:layout-component name="messages"/>
	<stripes:layout-component name="contents">
	
		<c:if test="${not empty actionBean.currentlyHarvestedQueueItem && (actionBean.currentlyHarvestedQueueItem.url==actionBean.harvestSource.url)}">
			<div class="important-msg" style="margin-bottom:10px">This source is being harvested right now!</div>
		</c:if>
		
		<h1>View source</h1>
		<br/>
	    <stripes:form action="/source.action" focus="">
	    	<stripes:hidden name="harvestSource.sourceId"/>
	        <table>
	            <tr>
	                <td>Name:</td>
	                <td>${fn:escapeXml(actionBean.harvestSource.name)}</td>
	            </tr>
	            <tr>
	                <td>URL:</td>
	                <td>${fn:escapeXml(actionBean.harvestSource.url)}</td>
	            </tr>
	            <tr>
	                <td>Type:</td>
	                <td>
	                	${fn:escapeXml(actionBean.harvestSource.type)}
	                </td>
	            </tr>
	            <tr>
	                <td>E-mails:</td>
	                <td>${fn:escapeXml(actionBean.harvestSource.emails)}</td>
	            </tr>
	            <tr>
	                <td>Date created:</td>
	                <td>
	                	${fn:escapeXml(actionBean.harvestSource.dateCreated)}
	                </td>
	            </tr>
	            <tr>
	                <td>Creator:</td>
	                <td>
	                	${fn:escapeXml(actionBean.harvestSource.creator)}
	                </td>
	            </tr>
	            <tr>
	                <td>Number of resources:</td>
	                <td>
	                	to be implemented
	                </td>
	            </tr>
	            <tr>
	                <td>Schedule cron expression:</td>
	                <td class="cronExpression">${fn:escapeXml(actionBean.harvestSource.scheduleCron)}</td>
	            </tr>
           		<c:if test="${actionBean.harvestSource.unavailable}">
           			<tr>
           				<td colspan="2" class="warning-msg" style="color:#E6E6E6">The source has been unavailable for too many times!</td>
           			</tr>
           		</c:if>
           		
	            <tr>
	                <td colspan="2" style="padding-top:10px">
						<c:if test="${!actionBean.productionMode}">
	                		<stripes:submit name="harvestNow" value="Harvest now"/>
	                	</c:if>
	                    <stripes:submit name="scheduleUrgentHarvest" value="Schedule urgent harvest"/>
	                </td>
	            </tr>
	        </table>
	        <br/><br/>
	        <strong>Last 10 harvests:</strong>
	        <table class="datatable">	        	
	        	<thead>
		        	<tr>
		        		<th scope="col">Type</th>
		        		<th scope="col">User</th>
		        		<th scope="col">Started</th>
		        		<th scope="col">Finished</th>
		        		<th scope="col">Triples</th>
		        		<th scope="col">Subjects</th>
		        		<th scope="col"></th>
		        	</tr>
	        	</thead>
	        	<tbody>
	        		<c:forEach items="${actionBean.harvests}" var="harv" varStatus="loop">
	        			<tr>
	        				<td>${fn:escapeXml(harv.harvestType)}</td>
	        				<td>${fn:escapeXml(harv.user)}</td>
	        				<td><fmt:formatDate value="${harv.datetimeStarted}" pattern="dd-MM-yy HH:mm:ss"/></td>
	        				<td><fmt:formatDate value="${harv.datetimeFinished}" pattern="dd-MM-yy HH:mm:ss"/></td>		        				
	        				<td>${fn:escapeXml(harv.totalStatements)}</td>
	        				<td>${fn:escapeXml(harv.totalResources)}</td>
							<td>
		        				<stripes:link href="/harvest.action">
		        					<img src="${pageContext.request.contextPath}/images/view2.gif" title="View" alt="View"/>
									<c:if test="${(!(empty harv.hasFatals) && harv.hasFatals) || (!(empty harv.hasErrors) && harv.hasErrors)}">
										<img src="${pageContext.request.contextPath}/images/error.png" title="Errors" alt="Errors"/>
									</c:if>
									<c:if test="${!(empty harv.hasWarnings) && harv.hasWarnings}">
										<img src="${pageContext.request.contextPath}/images/warning.png" title="Warnings" alt="Warnings"/>
									</c:if>	                                
	                                <stripes:param name="harvestDTO.harvestId" value="${harv.harvestId}"/>
	                            </stripes:link>
		        			</td>	        				
	        			</tr>
	        		</c:forEach>
	        	</tbody>
	        </table>
	    </stripes:form>

	</stripes:layout-component>
</stripes:layout-render>
