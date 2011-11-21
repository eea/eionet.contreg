<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Bookmarks">
    <stripes:layout-component name="contents">

        <cr:tabMenu tabs="${actionBean.tabs}" />

        <br style="clear:left" />

        <c:if test="${actionBean.usersBookmarks}">
            <h1>My bookmarks</h1>
        </c:if>
        <c:if test="${!actionBean.usersBookmarks}">
            <h1>${actionBean.ownerName}'s bookmarks</h1>
        </c:if>
            <c:choose>
                <c:when test="${not empty actionBean.bookmarks}">
                    <crfn:form id="bookmarkForm"
                        action="/bookmarks.action"
                        method="post">
                        <stripes:hidden name="uri" value="${actionBean.uri}" />
                        <display:table name="${actionBean.bookmarks}" class="sortable" pagesize="20" sort="list" id="bookmark" htmlId="bookmarks" requestURI="${actionBean.urlBinding}" style="width:100%">
                            <c:if test="${actionBean.usersBookmarks}">
                                <display:column title="" sortable="false" style="width:50px;">
                                    <stripes:checkbox name="selectedBookmarks" value="${bookmark.bookmarkUrl}"/>
                                </display:column>
                            </c:if>
                            <display:column title="URL" sortable="true">
                                <stripes:link href="/factsheet.action">
                                    ${bookmark.bookmarkTitle}
                                    <stripes:param name="uri" value="${bookmark.bookmarkUrl}" />
                                </stripes:link>
                            </display:column>
                        </display:table>
                        <c:if test="${actionBean.usersBookmarks}">
                            <div><stripes:submit name="delete" value="Delete Bookmarks" title="Delete selected bookmarks" />
                                <input type="button" name="selectAll" value="Select all" onclick="toggleSelectAll('bookmarkForm');return false" />
                            </div>
                        </c:if>
                    </crfn:form>
                </c:when>
                <c:otherwise>
                    <p>No bookmarks found.</p>
                </c:otherwise>
            </c:choose>

    </stripes:layout-component>

</stripes:layout-render>
