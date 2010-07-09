<%@ include file="/pages/common/taglibs.jsp"%>	
<stripes:layout-definition>
	<div style="font-size:1.2em;font-weight:bold">Tag Cloud</div>
	<div id ="cloud">
	<c:choose>
		<c:when test="${empty actionBean.tagCloud}">
			<p class="system-msg">No tags found</p>
		</c:when>
		<c:otherwise>
			<c:forEach items="${actionBean.tagCloud}" var="tagEntry">
				<c:url var="tagClass" value="size${tagEntry.scale}" />
				<a href="tagSearch.action?search=Search&searchTag=${tagEntry.tag }" class="tag ${tagClass}" title="search by tags">${tagEntry.tag }</a> 
			</c:forEach>
		</c:otherwise>
	</c:choose>
	</div>
</stripes:layout-definition>