<%@ page import="eionet.cr.web.security.CRUser" %>

<%@ include file="/pages/common/taglibs.jsp"%>

<div id="leftcolumn" class="localnav">
    <ul>
        <li><a href="${pageContext.request.contextPath}/documentation">Help and documentation</a></li>
        <li><a href="${pageContext.request.contextPath}/simpleSearch.action">Simple search</a></li>
        <c:if test='${not empty sessionScope.crUser && crfn:userHasPermission(pageContext.session, "/mergedeliveries", "v")}'>
            <c:if test="${initParam.enableEEAFunctionality}">
               <li><a href="${pageContext.request.contextPath}/deliverySearch.action">Merge deliveries</a></li>
            </c:if>
        </c:if>
        <li><a href="${pageContext.request.contextPath}/customSearch.action">Custom search</a></li>
        <li><a href="${pageContext.request.contextPath}/typeSearch.action">Type search</a></li>
<%--
        <li><a href="${pageContext.request.contextPath}/spatialSearch.action">Spatial search</a></li>
        <li><a href="${pageContext.request.contextPath}/spatialSearch.action?googleEarthIntro">Google Earth link</a></li>
--%>
        <li><a href="${pageContext.request.contextPath}/tagSearch.action">Tag search</a></li>
        <li><a href="${pageContext.request.contextPath}/recentUploads.action">Recent uploads</a></li>
        <li><a href="${pageContext.request.contextPath}/browseDatasets.action">Browse datasets</a></li>
        <li><a href="${pageContext.request.contextPath}/sparql">SPARQL endpoint</a></li>
        <li><a href="${pageContext.request.contextPath}/sharedSparqlBookmarks.action">Shared SPARQL bookmarks</a></li>
        <li><a href="${pageContext.request.contextPath}/sources.action">Harvesting sources</a></li>
        <li><a href="${pageContext.request.contextPath}/harvestQueue.action">Harvest queue</a></li>
        <li><a href="${pageContext.request.contextPath}/home">User homes</a></li>
        <c:if test='${not empty sessionScope.crUser && crfn:userHasPermission(pageContext.session, "/sparqlclient", "v")}'>
            <li><a href="${pageContext.request.contextPath}/sparqlclient">Other SPARQL systems</a></li>
        </c:if>
        <c:if test="${not empty sessionScope.crUser && sessionScope.crUser.administrator}">
            <li><a href="${pageContext.request.contextPath}/admin" title="Administrative activities">Admin actions</a></li>
        </c:if>
        <c:if test='${not empty sessionScope.crUser && crfn:userHasPermission(pageContext.session, "/registrations", "u")}'>
            <li><a href="${pageContext.request.contextPath}/registerUrl.action">URL registration</a></li>
            <%-- We're sure that session has user, because we assume anonymous users --%>
            <%-- don't have permissions in "/registrations" ACL.                     --%>
            <li><a href="${pageContext.request.contextPath}/view.action?uri=${fn:escapeXml(sessionScope.crUser.homeUri)}">My home</a></li>
        </c:if>
        <c:if test='${not empty sessionScope.crUser && crfn:userHasPermission(pageContext.session, "/project", "v")}'>
            <li><a href="${pageContext.request.contextPath}/view.action?uri=${fn:escapeXml(sessionScope.crUser.projectUri)}">Projects</a></li>
        </c:if>
    </ul>
</div>
