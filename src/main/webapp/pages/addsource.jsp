<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.io.*,eionet.cr.dao.DAOFactory"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Add Harvesting Source">
	<stripes:layout-component name="errors"/>
	<stripes:layout-component name="messages"/>
	<stripes:layout-component name="contents">
		<h1>Add source</h1>
	    <stripes:form action="/source.action" method="post">
	        <table>
	            <tr>
	                <td>Name:</td>
	                <td><stripes:text name="harvestSource.name" size="100"/></td>
	            </tr>
	            <tr>
	                <td>URL:</td>
	                <td><stripes:text name="harvestSource.url" size="100"/></td>
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
	                <td><stripes:text name="harvestSource.emails" size="100"/></td>
	            </tr>
	            <tr>
	                <td>Harvest interval:</td>
	                <td>
	                	<stripes:text name="harvestSource.intervalMinutes" size="10" value="6"/>
	                	<stripes:select name="intervalMultiplier" value="10080">
	                		<c:forEach items="${actionBean.intervalMultipliers}" var="intervalMultiplier"> 
		                		<stripes:option value="${intervalMultiplier.key}" label="${intervalMultiplier.value}"/>
		                	</c:forEach>
	                	</stripes:select>
	                </td>
	            </tr>
	            <tr>
	                <td colspan="2">
	                    <stripes:submit name="add" value="Add"/>       
	                </td>
	            </tr>
	        </table>
	    </stripes:form>

	</stripes:layout-component>
</stripes:layout-render>
