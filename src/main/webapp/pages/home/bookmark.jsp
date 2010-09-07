<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-render name="/pages/common/template.jsp" pageTitle="User bookmarks">

<stripes:layout-component name="contents">
	<c:if test="${ actionBean.userAuthorized}">
		<h1>My bookmarks</h1>
	</c:if>
	<c:if test="${ !actionBean.userAuthorized}">
		<h1>${actionBean.attemptedUserName}'s bookmarks</h1>
	</c:if>
		<c:choose>
			<c:when test="${not empty actionBean.bookmarks}">
				<crfn:form id="bookmarkForm"
					action="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/bookmark?deletebookmarks=1"
					method="post">
					<display:table name="${actionBean.bookmarks}" class="sortable"
						pagesize="20" sort="list" id="bookmark" htmlId="bookmarks"
						requestURI="${actionBean.urlBinding}" style="width:100%">
						<c:if test="${ actionBean.userAuthorized}">
							<display:column title="" sortable="false" style="width:50px;">
							<input type="checkbox"
									value="${ bookmark.bookmarkUrlHtmlFormatted }" name='bookmarkUrl'></input>
							</display:column>
						</c:if>
						<display:column title="URL" sortable="false">
							<stripes:link href="/factsheet.action">${bookmark.bookmarkUrl}
								<stripes:param name="uri" value="${bookmark.bookmarkUrl}" />
							</stripes:link>
						</display:column>
					</display:table>
					<c:if test="${ actionBean.userAuthorized}">
						<div><stripes:submit name="delete" value="Delete Bookmarks"
						title="Delete selected bookmarks" /> <input type="button"
						name="selectAll" value="Select all"
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