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
	                <td>Identifier:</td>
	                <td>${actionBean.harvestSource.identifier}</td>
	            </tr>
	            <tr>
	                <td>Pull URL:</td>
	                <td>${actionBean.harvestSource.pullUrl}</td>
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
	                <td>
	                	${actionBean.harvestSource.statements}
	                </td>
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
	                    <stripes:submit name="exec" value="Schedule for immediate execution"/>
	                    <!--stripes:submit name="push" value="Push from local file"/-->
	                </td>
	            </tr>
	        </table>
	    </stripes:form>

	</stripes:layout-component>
</stripes:layout-render>
