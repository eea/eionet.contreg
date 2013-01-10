<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp"
  pageTitle="Bookmarks">
  <stripes:layout-component name="contents">

    <cr:tabMenu tabs="${actionBean.tabs}" />

    <br style="clear: left" />

    <c:if test="${actionBean.usersBookmarks}">
      <h1>My bookmarks</h1>
    </c:if>

    <c:if test="${!actionBean.usersBookmarks}">
      <c:choose>
        <c:when test="${actionBean.projectBookmarks}">
          <h1>${actionBean.projectName} project bookmarks</h1>
        </c:when>
        <c:otherwise>
          <h1>${actionBean.ownerName}'s bookmarks</h1>
        </c:otherwise>
      </c:choose>
    </c:if>
    <c:choose>
      <c:when test="${not empty actionBean.bookmarks}">
        <crfn:form id="bookmarkForm" action="/bookmarks.action"
          method="post">
          <stripes:hidden name="uri" value="${actionBean.uri}" />
          <display:table name="${actionBean.bookmarks}" class="sortable"
            pagesize="20" sort="list" id="bookmark" htmlId="bookmarks"
            requestURI="${actionBean.urlBinding}" style="width:100%">
            <c:if test="${actionBean.deletePermission}">
              <display:column title="" sortable="false" style="width:50px;">
                <stripes:checkbox name="selectedBookmarks"
                  value="${bookmark.uri}" />
              </display:column>
            </c:if>
            <display:column title="Title" sortable="true">
              <c:choose>
                <c:when test="${not empty bookmark.bookmarkUrl}">
                  <stripes:link href="/factsheet.action"
                    title="${bookmark.query}">
                                            ${bookmark.bookmarkTitle}
                                            <stripes:param name="uri"
                      value="${bookmark.bookmarkUrl}" />
                  </stripes:link>
                </c:when>
                <c:otherwise>
                  <stripes:link href="/factsheet.action"
                    title="${bookmark.query}">
                                            ${bookmark.bookmarkTitle}
                                            <stripes:param name="uri"
                      value="${bookmark.uri}" />
                  </stripes:link>
                </c:otherwise>
              </c:choose>
            </display:column>
            <display:column title="Type" sortable="true">
              <c:out value="${bookmark.typeLabel}" />
            </display:column>
          </display:table>
          <c:if test="${actionBean.deletePermission}">
            <div>
              <stripes:submit name="delete" value="Delete Bookmarks"
                title="Delete selected bookmarks" />
              <input type="button" name="selectAll" value="Select all"
                onclick="toggleSelectAll('bookmarkForm');return false" />
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
