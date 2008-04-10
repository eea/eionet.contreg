<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.io.*"%>

<%@ include file="/pages/common/taglibs.jsp"%>	

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvesting Sources">

	<stripes:layout-component name="errors"/>
	<stripes:layout-component name="messages"/>
	<stripes:layout-component name="contents">
	
	<div id="operations">
		<ul>
			<li><a href="addsource.jsp">Add new source</a></li>
		</ul>
	</div>
	      			
	<jsp:useBean id="harvestSourceList" scope="page"
                     class="eionet.cr.web.action.HarvestSourceListActionBean"/>
        
        <h1>Harvesting sources</h1>             
        <display:table name="${harvestSourceList.harvestSources}" class="datatable" pagesize="15" id="sources" sort="list">
		    <display:column property="url" title="URL"/>
		    <display:column property="type" sortable="true" headerClass="sortable"/>
		    <display:column>
		    	<stripes:link href="/source.action" event="preViewHarvestSource">
                    <img src="${pageContext.request.contextPath}/images/view2.gif" title="View"/>
                    <stripes:param name="harvestSource.sourceId" value="${sources.sourceId}"/>
                </stripes:link>
		    </display:column>
		    <display:column>
		    	<stripes:link href="/source.action" event="preEditHarvestSource">
	            	<img src="${pageContext.request.contextPath}/images/edit.gif" title="Edit"/>
	                <stripes:param name="harvestSource.sourceId" value="${sources.sourceId}"/>
	            </stripes:link>
		    </display:column>
		    <display:column>
		    	<stripes:link href="/source.action" event="deleteHarvestSource" onclick="return confirm('Are you sure you want to delete this harvesting source');">
                    <img src="${pageContext.request.contextPath}/images/delete.gif" title="Delete"/>
                    <stripes:param name="harvestSource.sourceId" value="${sources.sourceId}"/>
                </stripes:link>
		    </display:column>
		</display:table>
                     
	</stripes:layout-component>
</stripes:layout-render>
