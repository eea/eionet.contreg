<%@ page import="eionet.cr.web.security.CRUser,eionet.cr.web.util.ICRWebConstants" %>
<% CRUser crUser = (CRUser)session.getAttribute(ICRWebConstants.USER_SESSION_ATTR); %>	 
<div id="leftcolumn" class="localnav">
	<ul>
		<li><a href="/cr/simpleSearch.action" title="Simple search">Simple search </a></li>
		<li><a href="/cr/dataflowSearch.action" title="Dataflow search">Dataflow search </a></li>
		<li><a href="/cr/customSearch.action" title="Custom search">Custom search </a></li>
		<li><a href="/cr/recentUploads.action" title="Recent uploads">Recent uploads </a></li>
	    <%
  		if (crUser != null && crUser.hasPermission("/", "u")){ %>
	  		<li><a href="/cr/pages/sources.jsp" title="Manage harvester">Harvester </a></li>
	    	<li><a href="/cr/luceneQuery.action" title="Lucene query">Lucene query </a></li>
	    <%
	    }
		%>		
    </ul>
</div>