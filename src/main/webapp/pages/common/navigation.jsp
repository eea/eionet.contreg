<%@ page import="eionet.cr.web.security.CRUser" %>

<%@ include file="/pages/common/taglibs.jsp"%>	

<div id="leftcolumn" class="localnav">
	<ul>
		<li><a href="simpleSearch.action" title="Simple search">Simple search</a></li>
		<li><a href="dataflowSearch.action" title="Dataflow search">Search deliveries</a></li>
		<li><a href="customSearch.action" title="Custom search">Custom search</a></li>
		<li><a href="typeSearch.action" title="Type search">Type search</a></li>
		<li><a href="spatialSearch.action" title="Spatial search">Spatial search</a></li>
		<li><a href="spatialSearch.action?googleEarthIntro" title="Google Earth network link">Google Earth link</a></li>
		<li><a href="tagSearch.action" title="Tag search">Tag search</a></li>
		<li><a href="recentUploads.action" title="Recent uploads">Recent uploads</a></li>
		<li><a href="sparqlClient.action" title="Sparql client">Sparql client</a></li>
		<c:if test='${sessionScope.crUser!=null && crfn:hasPermission(sessionScope.crUser.userName, "/", "u")}'>
			<li><a href="registerUrl.action" title="URL registration">URL registration</a></li>						
			<li><a href="sources.action" title="Manage harvest sources">Harvest sources</a></li>
			<li><a href="harvestQueue.action" title="Monitor harvest queue">Harvest queue</a></li>
			<li><a href="home/${ actionBean.userName }" title="User Home">My Home</a></li>
		</c:if>		
    </ul>
</div>
