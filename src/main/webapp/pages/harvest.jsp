<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.io.*,eionet.cr.dao.DAOFactory"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvest">
	<stripes:layout-component name="errors"/>
	<stripes:layout-component name="messages"/>
	<stripes:layout-component name="contents">
	
		<h1>Harvest</h1>
		
        <table>
        	<tr>
                <td>Harvest source:</td>
                <td>
                	<stripes:link href="/source.action" event="preViewHarvestSource">
                        ${actionBean.harvestSourceDTO.url}
                        <stripes:param name="harvestSource.sourceId" value="${actionBean.harvestSourceDTO.sourceId}"/>
                    </stripes:link>
                </td>
            </tr>
            <tr>
                <td>Type:</td>
                <td>${actionBean.harvestDTO.harvestType}</td>
            </tr>
            <tr>
                <td>User:</td>
                <td>${actionBean.harvestDTO.user}</td>
            </tr>
            <tr>
                <td>Status:</td>
                <td>
                	${actionBean.harvestDTO.status}
                </td>
            </tr>
            <tr>
                <td>Started:</td>
                <td><fmt:formatDate value="${actionBean.harvestDTO.datetimeStarted}" pattern="dd MMM yy HH:mm:ss"/></td>
            </tr>
            <tr>
                <td>Finished:</td>
                <td><fmt:formatDate value="${actionBean.harvestDTO.datetimeFinished}" pattern="dd MMM yy HH:mm:ss"/></td>
            </tr>
            <tr>
                <td>Statements in total:</td>
                <td>${actionBean.harvestDTO.totalStatements}</td>
            </tr>
            <tr>
                <td>Statements with literal objects:</td>
                <td>${actionBean.harvestDTO.litObjStatements}</td>
            </tr>
            <tr>
                <td>Statements with resource objects:</td>
                <td>${actionBean.harvestDTO.totalResources}</td>
            </tr>
            <tr>
                <td>Encoding schemes:</td>
                <td>${actionBean.harvestDTO.encodingSchemes}</td>
            </tr>
        </table>
        <br/><br/>
        <c:choose>
        	<c:when test="${(empty actionBean.fatals) && (empty actionBean.errors) && (empty actionBean.warnings)}">
        		<strong>No messages recorded for this harvest!</strong>
			</c:when>
			<c:otherwise>
				<c:if test="${!(empty actionBean.fatals)}">
					<strong>Fatal errors:</strong>
					<table class="datatable">
			        	<thead>
				        	<tr>
				        		<th scope="col">Message</th>
				        		<th scope="col">StackTrace</th>
				        	</tr>
			        	</thead>
			        	<tbody>
			        		<c:forEach items="${actionBean.fatals}" var="msg" varStatus="loop">
			        			<tr>
			        				<td>${msg.message}</td>
			        				<td>${msg.stackTrace}</td>
			        			</tr>
			        		</c:forEach>
			        	</tbody>
			        </table>
				</c:if>
				<c:if test="${!(empty actionBean.errors)}">
					<strong>Errors:</strong>
					<table class="datatable">
			        	<thead>
				        	<tr>
				        		<th scope="col">Message</th>
				        		<th scope="col">StackTrace</th>
				        	</tr>
			        	</thead>
			        	<tbody>
			        		<c:forEach items="${actionBean.errors}" var="msg" varStatus="loop">
			        			<tr>
			        				<td>${msg.message}</td>
			        				<td>${msg.stackTrace}</td>
			        			</tr>
			        		</c:forEach>
			        	</tbody>
			        </table>
				</c:if>
				<c:if test="${!(empty actionBean.warnings)}">
					<strong>Warnings:</strong>
					<table class="datatable">
			        	<thead>
				        	<tr>
				        		<th scope="col">Message</th>
				        		<th scope="col">StackTrace</th>
				        	</tr>
			        	</thead>
			        	<tbody>
			        		<c:forEach items="${actionBean.warnings}" var="msg" varStatus="loop">
			        			<tr>
			        				<td>${msg.message}</td>
			        				<td>${msg.stackTrace}</td>
			        			</tr>
			        		</c:forEach>
			        	</tbody>
			        </table>
				</c:if>
				
			</c:otherwise>
        </c:choose>

	</stripes:layout-component>
</stripes:layout-render>
