<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.io.*,eionet.cr.dao.DAOFactory"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Add Harvesting Source">
	<stripes:layout-component name="errors"/>
	<stripes:layout-component name="messages"/>
	<stripes:layout-component name="contents">
		<h1>Add source</h1>
	    <stripes:form action="/source.action" focus="">
	        <table>
	            <tr>
	                <td>Identifier:</td>
	                <td><stripes:text name="harvestSource.identifier"/></td>
	            </tr>
	            <tr>
	                <td>Pull URL:</td>
	                <td><stripes:text name="harvestSource.pullUrl"/></td>
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
	                <td>Schedule:</td>
	                <td>
	                	weekday
	                	<stripes:select name="harvestSource.weekday">
	                		<stripes:option value="monday" label="Monday"/>
	                		<stripes:option value="sunday" label="Sunday"/>
	                	</stripes:select>
	                	hour
	                	<stripes:select name="harvestSource.hour">
	                		<% for(int i=0; i<24; i++){ 
		                		String day = new Integer(i).toString();%>
	                		<stripes:option value="<%=day%>" label="<%=day%>"/>
	                		<% } %>
	                	</stripes:select>
	                	period
	                	<stripes:select name="harvestSource.period">
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
	                    <stripes:submit name="add" value="Add"/>       
	                    <stripes:submit name="exec" value="Schedule for immediate execution"/>
	                    <stripes:submit name="push" value="Push from local file"/>
	                </td>
	            </tr>
	        </table>
	    </stripes:form>

	</stripes:layout-component>
</stripes:layout-render>
