<%@page contentType="text/html;charset=UTF-8"%>

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
	                	<stripes:text name="harvestSource.intervalMinutes" size="10"/>
	                	<stripes:select name="intervalMultiplier" value="${actionBean.selectedIntervalMultiplier}">
	                		<c:forEach items="${actionBean.intervalMultipliers}" var="intervalMultiplier"> 
		                		<stripes:option value="${intervalMultiplier.key}" label="${intervalMultiplier.value}"/>
		                	</c:forEach>
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
