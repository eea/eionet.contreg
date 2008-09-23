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
	                <td>Schedule cron expression:</td>
	                <td><stripes:text name="harvestSource.scheduleCron"/></td>
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
