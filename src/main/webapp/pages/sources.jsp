<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.io.*"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvesting Sources">

	<stripes:layout-component name="errors"/>
	<stripes:layout-component name="messages"/>
	<stripes:layout-component name="contents">
	
	<div id="operations">
		<ul>
			<li>
				<stripes:link href="/source.action" event="add">Add new source</stripes:link>
			</li>
		</ul>
	</div>
	      			
	<h1>Harvesting sources</h1>
	<p></p>
	<div id="tabbedmenu">
	    <ul>
	    	<c:forEach items="${actionBean.sourceTypes}" var="loopItem">
				<c:choose>
			  		<c:when test="${actionBean.type==loopItem.type}" > 
						<li id="currenttab"><span>${fn:escapeXml(loopItem.title)}</span></li>
					</c:when>
					<c:otherwise>
						<li>
							<stripes:link href="/sources.action">
								${fn:escapeXml(loopItem.title)}
				                <stripes:param name="type" value="${loopItem.type}"/>
				            </stripes:link>
			            </li>
					</c:otherwise>
				</c:choose>
			</c:forEach>
	    </ul>
	</div>
	<br style="clear:left" />
	<div style="margin-top:20px">	
		<display:table name="${actionBean.harvestSources}" class="sortable" pagesize="15" sort="list" id="harvestSource" htmlId="harvestSources" requestURI="${actionBean.urlBinding}" decorator="eionet.cr.web.util.HarvestSourcesTableDecorator" style="width:100%">
		
			<display:setProperty name="paging.banner.items_name" value="sources"/>
			
			<display:column property="url" title="URL" sortable="true"/>
			<display:column>
				<stripes:link href="/source.action" event="view">
					<img src="${pageContext.request.contextPath}/images/view2.gif" title="View" alt="View"/>
					<stripes:param name="harvestSource.sourceId" value="${harvestSource.sourceId}"/>
				</stripes:link>
			</display:column>
			<display:column>
				<stripes:link href="/source.action" event="edit">
					<img src="${pageContext.request.contextPath}/images/edit.gif" title="Edit" alt="Edit"/>
					<stripes:param name="harvestSource.sourceId" value="${harvestSource.sourceId}"/>
				</stripes:link>
			</display:column>
			<display:column>
				<stripes:link href="/source.action" event="delete" onclick="return confirm('Are you sure you want to delete this harvesting source');">
					<img src="${pageContext.request.contextPath}/images/delete_small.gif" title="Delete" alt="Delete"/>
					<stripes:param name="harvestSource.sourceId" value="${harvestSource.sourceId}"/>
				</stripes:link>
			</display:column>
			
		</display:table>
	</div>                     
	</stripes:layout-component>
</stripes:layout-render>
