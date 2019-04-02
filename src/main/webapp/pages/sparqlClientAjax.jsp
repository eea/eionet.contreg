<%@page contentType="text/html;charset=UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>

<%-- These blocks are loaded through SPARQLEndpointActionBean and sparqClient.jsp by AJAX request and AJAX id --%>

<c:if test="${actionBean.ajaxRequestId == 1}">

	<h1>Bookmarked SPARQL queries</h1>
	<c:choose>
	    <c:when test="${not empty actionBean.bookmarkedQueries}">
	        <crfn:form id="bookmarkedQueriesForm" action="/sparql" method="post">
	        <c:if test="${not empty actionBean.defaultGraphUris}">
	            <c:forEach var="defaultGraphUri" items="${actionBean.defaultGraphUris}">
	                <input type="hidden" name="default-graph-uri" value="${defaultGraphUri}" />
	            </c:forEach>
	        </c:if>
	        <c:if test="${not empty actionBean.namedGraphUris}">
	            <c:forEach var="namedGraphUri" items="${actionBean.namedGraphUris}">
	                <input type="hidden" name="named-graph-uri" value="${namedGraphUri}" />
	            </c:forEach>
	        </c:if>
	        <table>
	            <c:forEach items="${actionBean.bookmarkedQueries}" var="bookmarkedQuery">
	            <tr>
	                <td style="width:1%"><stripes:checkbox value="${bookmarkedQuery.subj}" name="deleteQueries"/></td>
	                <td style="width:99%">
	                    <stripes:link href="/sparql" title="${bookmarkedQuery.queryString}">
	                        <stripes:param name="fillfrom" value="${bookmarkedQuery.subj}" />
	                        <stripes:param name="selectedBookmarkName" value="${bookmarkedQuery.label}" />
	                        <c:if test="${not empty actionBean.defaultGraphUris}">
	                            <c:forEach var="defaultGraphUri" items="${actionBean.defaultGraphUris}">
	                                <stripes:param name="default-graph-uri" value="${defaultGraphUri}" />
	                            </c:forEach>
	                        </c:if>
	                        <c:if test="${not empty actionBean.namedGraphUris}">
	                            <c:forEach var="namedGraphUri" items="${actionBean.namedGraphUris}">
	                                <stripes:param name="named-graph-uri" value="${namedGraphUri}" />
	                            </c:forEach>
	                        </c:if>
	                        <c:out value="${bookmarkedQuery.label}"/>
	                    </stripes:link>
	                </td>
	            </tr>
	            </c:forEach>
	            <tr>
	                <td colspan="2" align="right" style="padding-top: 5px">
	                    <stripes:submit name="deletePersonalBookmark" id="deletePersonalBookmark" value="Delete" title="Delete the bookmarked queries that you have selected below"/>
	                    <input type="button" name="selectAll" value="Select all" onclick="toggleSelectAll('bookmarkedQueriesForm');return false"/>
	                </td>
	            </tr>
	        </table>
	        </crfn:form>
	    </c:when>
	    <c:otherwise>
	        No bookmarked queries found.
	    </c:otherwise>
	</c:choose>
</c:if>

<c:if test="${actionBean.ajaxRequestId == 2}">
	<h1>Shared bookmarked SPARQL queries</h1>
	<c:choose>
	    <c:when test="${not empty actionBean.sharedBookmarkedQueries}">
	        <crfn:form id="sharedBookmarkedQueriesForm" action="/sparql" method="post">
	        <c:if test="${not empty actionBean.defaultGraphUris}">
	            <c:forEach var="defaultGraphUri" items="${actionBean.defaultGraphUris}">
	                <input type="hidden" name="default-graph-uri" value="${defaultGraphUri}" />
	            </c:forEach>
	        </c:if>
	        <c:if test="${not empty actionBean.namedGraphUris}">
	            <c:forEach var="namedGraphUri" items="${actionBean.namedGraphUris}">
	                <input type="hidden" name="named-graph-uri" value="${namedGraphUri}" />
	            </c:forEach>
	        </c:if>
	        <table>
	            <c:forEach items="${actionBean.sharedBookmarkedQueries}" var="bookmarkedQuery">
	            <tr>
	                <c:if test="${actionBean.sharedBookmarkPrivilege}">
	                <td style="width:1%"><stripes:checkbox value="${bookmarkedQuery.subj}" name="deleteQueries"/></td>
	                </c:if>
	                <td style="width:99%">
	                    <stripes:link href="/sparql" title="${bookmarkedQuery.queryString}">
	                        <stripes:param name="fillfrom" value="${bookmarkedQuery.subj}" />
	                        <stripes:param name="selectedBookmarkName" value="${bookmarkedQuery.label}" />
	                        <c:if test="${not empty actionBean.defaultGraphUris}">
	                            <c:forEach var="defaultGraphUri" items="${actionBean.defaultGraphUris}">
	                                <stripes:param name="default-graph-uri" value="${defaultGraphUri}" />
	                            </c:forEach>
	                        </c:if>
	                        <c:if test="${not empty actionBean.namedGraphUris}">
	                            <c:forEach var="namedGraphUri" items="${actionBean.namedGraphUris}">
	                                <stripes:param name="named-graph-uri" value="${namedGraphUri}" />
	                            </c:forEach>
	                        </c:if>
	                        <c:out value="${bookmarkedQuery.label}"/>
	                    </stripes:link>
	                </td>
	            </tr>
	            </c:forEach>
	            <c:if test="${actionBean.sharedBookmarkPrivilege}">
	            <tr>
	                <td colspan="2" align="right" style="padding-top: 5px">
	                    <stripes:submit name="deleteSharedBookmark" id="deleteSharedBookmark" value="Delete" title="Delete the bookmarked queries that you have selected below"/>
	                    <input type="button" name="selectAll" value="Select all" onclick="toggleSelectAll('sharedBookmarkedQueriesForm');return false"/>
	                </td>
	            </tr>
	            </c:if>
	        </table>
	        </crfn:form>
	    </c:when>
	    <c:otherwise>
	        No bookmarked queries found.
	    </c:otherwise>
	</c:choose>
</c:if>

<c:if test="${actionBean.ajaxRequestId == 3}">

	<h1>Bookmarked SPARQL queries in project folders</h1>
	<c:choose>
	    <c:when test="${not empty actionBean.projectBookmarkedQueries}">
	        <crfn:form id="projectBookmarkedQueriesForm" action="/sparql" method="post">
	        <c:if test="${not empty actionBean.defaultGraphUris}">
	            <c:forEach var="defaultGraphUri" items="${actionBean.defaultGraphUris}">
	                <input type="hidden" name="default-graph-uri" value="${defaultGraphUri}" />
	            </c:forEach>
	        </c:if>
	        <c:if test="${not empty actionBean.namedGraphUris}">
	            <c:forEach var="namedGraphUri" items="${actionBean.namedGraphUris}">
	                <input type="hidden" name="named-graph-uri" value="${namedGraphUri}" />
	            </c:forEach>
	        </c:if>
	        <table>
	<tr>
	   <th>Project</th>
	   <th>Bookmark</th>
	</tr>
	            <c:forEach items="${actionBean.projectBookmarkedQueries}" var="bookmarkedQuery">
	            <tr>
	                <!-- td width="1%"><stripes:checkbox value="${bookmarkedQuery.subj}" name="deleteQueries"/></td-->
	                <td style="width:40%"><c:out value="${bookmarkedQuery.proj}"/></td>
	                <td style="width:60%">
	                    <stripes:link href="/sparql" title="${bookmarkedQuery.queryString}">
	                        <stripes:param name="fillfrom" value="${bookmarkedQuery.subj}" />
	                        <stripes:param name="selectedBookmarkName" value="${bookmarkedQuery.label}" />
	                        <c:if test="${not empty actionBean.defaultGraphUris}">
	                            <c:forEach var="defaultGraphUri" items="${actionBean.defaultGraphUris}">
	                                <stripes:param name="default-graph-uri" value="${defaultGraphUri}" />
	                            </c:forEach>
	                        </c:if>
	                        <c:if test="${not empty actionBean.namedGraphUris}">
	                            <c:forEach var="namedGraphUri" items="${actionBean.namedGraphUris}">
	                                <stripes:param name="named-graph-uri" value="${namedGraphUri}" />
	                            </c:forEach>
	                        </c:if>
	                        <c:out value="${bookmarkedQuery.label}"/>
	                    </stripes:link>
	                </td>
	            </tr>
	            </c:forEach>
	            <!-- tr>
	                <td colspan="2" align="right" style="padding-top: 5px">
	                    <stripes:submit name="deletePersonalBookmark" id="deletePersonalBookmark" value="Delete" title="Delete the bookmarked queries that you have selected below"/>
	                    <input type="button" name="selectAll" value="Select all" onclick="toggleSelectAll('bookmarkedQueriesForm');return false"/>
	                </td>
	            </tr-->
	        </table>
	        </crfn:form>
	    </c:when>
	    <c:otherwise>
	        No bookmarked queries found.
	    </c:otherwise>
	</c:choose>

</c:if>
