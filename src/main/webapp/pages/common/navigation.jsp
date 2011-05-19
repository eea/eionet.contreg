<%@ page import="eionet.cr.web.security.CRUser" %>

<%@ include file="/pages/common/taglibs.jsp"%>

<div id="leftcolumn" class="localnav">
    <ul>
        <li><a href="simpleSearch.action">Simple search</a></li>
        <li><a href="dataflowSearch.action">Search deliveries</a></li>
        <li><a href="customSearch.action">Custom search</a></li>
        <li><a href="typeSearch.action">Type search</a></li>
        <li><a href="spatialSearch.action">Spatial search</a></li>
        <li><a href="spatialSearch.action?googleEarthIntro">Google Earth link</a></li>
        <li><a href="tagSearch.action">Tag search</a></li>
        <li><a href="recentUploads.action">Recent uploads</a></li>
        <li><a href="sparql">SPARQL endpoint</a></li>
        <li><a href="sources.action">Harvesting sources</a></li>
        <li><a href="harvestQueue.action">Harvest queue</a></li>
        <li><a href="home">User folders</a></li>
        <c:if test='${crfn:userHasPermission(sessionScope.crUser, "/", "u")}'>
            <li><a href="registerUrl.action">URL registration</a></li>
            <li><a href="admin" title="Administrative activities">Admin actions</a></li>
            <li><a href="home/${ actionBean.userName }">My Home</a></li>
        </c:if>
    </ul>
</div>
