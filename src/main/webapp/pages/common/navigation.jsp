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
        <li><a href="home">User homes</a></li>
        <c:if test='${crfn:userHasPermission(pageContext.session, "/", "u")}'>
        
            <li><a href="registerUrl.action">URL registration</a></li>
            <li><a href="admin" title="Administrative activities">Admin actions</a></li>
            
            <%-- We can be in this if-block even if no user in session, because a missing user --%>
            <%-- is considered anonymous, and anonymous can still have some permissions.       --%>
            <%-- So double check user existence before displaying link to his home.            --%>
            <c:if test="${not empty sessionScope.crUser && not empty sessionScope.crUser.userName}">
                <li><a href="home/${sessionScope.crUser.userName}">My home</a></li>
            </c:if>
        </c:if>
    </ul>
</div>
