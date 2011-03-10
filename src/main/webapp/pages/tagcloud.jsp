<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-definition>
    <h2><a href="tagSearch.action" title="Tag search">Tag cloud</a></h2>
    <div id ="cloud">
    <c:choose>
        <c:when test="${empty actionBean.tagCloud}">
            <p class="system-msg">No tags found</p>
        </c:when>
        <c:otherwise>
            <c:forEach items="${actionBean.tagCloud}" var="tagEntry">
                <c:set var="tagClass" value="size${tagEntry.scale}" />
                <a class="tag ${tagClass}" href="tagSearch.action?search=Search&amp;searchTag=${tagEntry.tag }">${tagEntry.tag}</a>
            </c:forEach>
        </c:otherwise>
    </c:choose>
    </div>
</stripes:layout-definition>
