<%@ page import="eionet.cr.web.security.CRUser,eionet.cr.web.util.ICRWebConstants" %>

<%@ include file="/pages/common/taglibs.jsp"%>	

<div id="leftcolumn" class="localnav">
	<ul>
		<li><a href="simpleSearch.action" title="Simple search">Simple search </a></li>
		<li><a href="dataflowSearch.action" title="Dataflow search">Dataflow search </a></li>
		<li><a href="customSearch.action" title="Custom search">Custom search </a></li>
		<li><a href="recentUploads.action" title="Recent uploads">Recent uploads </a></li>
		<c:if test='${sessionScope.crUser!=null && crfn:hasPermission(sessionScope.crUser.userName, "/", "u")}'>
			<li><a href="sources.action" title="Manage harvest sources">Harvest sources </a></li>
			<li><a href="harvestQueue.action" title="Monitor harvest queue">Harvest queue </a></li>
	    	<li><a href="luceneQuery.action" title="Lucene query">Lucene query </a></li>		
		</c:if>		
    </ul>
</div>