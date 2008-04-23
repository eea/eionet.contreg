<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.io.*,eionet.cr.dao.DAOFactory"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Edit Harvesting Source">

	<stripes:layout-component name="errors"/>
	<stripes:layout-component name="messages"/>
	<stripes:layout-component name="contents">
	
		<h1>Edit source</h1>
		
	    <stripes:form action="/source.action" focus="">
	    	<stripes:hidden name="harvestSource.sourceId"/>
	        <table>
	            <tr>
	                <td>Name:</td>
	                <td><stripes:text name="harvestSource.name"/></td>
	            </tr>
	            <tr>
	                <td>URL:</td>
	                <td><stripes:text name="harvestSource.url"/></td>
	            </tr>
	            <tr>
	                <td>Type:</td>
	                <td>
	                	<stripes:select name="harvestSource.type">
	                		<stripes:option value="data" label="Data"/>
	                		<stripes:option value="schema" label="Schema"/>
	                	</stripes:select>
	                </td>
	            </tr>
	            <tr>
	                <td>E-mails:</td>
	                <td><stripes:text name="harvestSource.emails"/></td>
	            </tr>
	            <tr>
	                <td>Date created:</td>
	                <td>
	                	${fn:escapeXml(actionBean.harvestSource.dateCreated)}
	                	<stripes:hidden name="harvestSource.dateCreated"/>
	                </td>
	            </tr>
	            <tr>
	                <td>Creator:</td>
	                <td>
	                	${fn:escapeXml(actionBean.harvestSource.creator)}
	                	<stripes:hidden name="harvestSource.creator"/>
	                </td>
	            </tr>
	            <tr>
	                <td>Number of resources:</td>
	                <td>
	                	${fn:escapeXml(actionBean.harvestSource.resources)}
	                </td>
	            </tr>
	            <tr>
	                <td>Schedule:</td>
	                <td>
	                	<stripes:hidden name="harvestSource.harvestSchedule.harvestSourceId"/>
	                	weekday
	                	<stripes:select name="harvestSource.harvestSchedule.weekday">
	                		<stripes:option value="monday" label="Monday"/>
	                		<stripes:option value="sunday" label="Sunday"/>
	                	</stripes:select>
	                	hour
	                	<stripes:select name="harvestSource.harvestSchedule.hour">
	                		<% for(int i=0; i<24; i++){ 
		                		String day = new Integer(i).toString();%>
	                		<stripes:option value="<%=day%>" label="<%=day%>"/>
	                		<% } %>
	                	</stripes:select>
	                	period
	                	<stripes:select name="harvestSource.harvestSchedule.period">
	                		<stripes:option value="1" label="1 week"/>
	                		<stripes:option value="2" label="2 weeks"/>
	                		<stripes:option value="3" label="3 weeks"/>
	                		<stripes:option value="4" label="4 weeks"/>
	                		<stripes:option value="5" label="5 weeks"/>
	                		<stripes:option value="6" label="6 weeks"/>
	                	</stripes:select>
	                </td>
	            </tr>
	            <tr>
	                <td colspan="2">
	                    <stripes:submit name="edit" value="Save"/>       
	                </td>
	            </tr>
	        </table>
	    </stripes:form>

	</stripes:layout-component>
</stripes:layout-render>
