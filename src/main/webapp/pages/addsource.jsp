<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Add Harvesting Source">

	<stripes:layout-component name="contents">
		<h1>Add source</h1>
	    <stripes:form action="/source.action" method="post">
	        <table>
	            <tr>
	                <td>URL:</td>
	                <td><stripes:text name="harvestSource.url" size="100"/></td>
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
