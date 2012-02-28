<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Tag search">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
        ( function($) {
            $(document).ready(function() {
                // Open delete bookmarked queries dialog
                $("#queryDialogLink").click(function() {
                    $('#queryDialog').dialog('open');
                    return false;
                });

                // Dialog setup
                $('#queryDialog').dialog({
                    autoOpen: false,
                    width: 500
                });

                // Close dialog
                $("#closeQueryDialog").click(function() {
                    $('#queryDialog').dialog("close");
                    return true;
                });
            });

        } ) ( jQuery );

        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

    <c:if test="${not empty actionBean.queryString}">
    <ul id="dropdown-operations">
        <li><a href="#">Operations</a>
            <ul>
                <li>
                    <a href="#" id="queryDialogLink">Search query</a>
                </li>
            </ul>
        </li>
    </ul>
    </c:if>

    <h1>Tag search</h1>
    <p>
        A <strong>tag</strong> is a non-hierarchical keyword or term assigned to a piece of information (such as an Internet bookmark, digital image, or computer file). This kind of metadata  helps describe an item and allows it to be found again by browsing or searching. Tags are generally chosen informally and personally by the item's creator or by its viewer.
        <strong>Tag cloud</strong> is a list of tags where size reflects popularity.
    </p>
    <crfn:form action="/tagSearch.action" method="get">
        <stripes:label for="tagText" class="question">
            <c:choose>
                <c:when test="${empty actionBean.selectedTags}">Tag</c:when>
                <c:otherwise>Type another tag</c:otherwise>
            </c:choose>
        </stripes:label>
        <stripes:text name="searchTag" id="tagText" size="50"/>
        <stripes:submit name="addTag" value="Search" id="searchButton"/>
        <c:if test="${not empty actionBean.selectedTags}">
            <p>
            <stripes:label for="tagText" class="question">Inserted tags </stripes:label>
            <c:forEach items="${actionBean.selectedTags}" var="selectedTag">
                > <a href="tagSearch.action?search=Search&searchTag=${selectedTag }" class="tag" >${selectedTag }</a>
                <a href="tagSearch.action?removeTag=Remove&searchTag=${selectedTag }"><img src="${pageContext.request.contextPath}/images/delete_small.gif" title="remove tag" alt="remove tag"/></a>
            </c:forEach>
            </p>
        </c:if>
    </crfn:form>

    <c:choose>
        <c:when test="${not empty actionBean.selectedTags}">
            <stripes:layout-render name="/pages/common/subjectsResultList.jsp" tableClass="sortable"/>
        </c:when>
        <c:otherwise>
            <p>Click a tag below or type in a word above</p>

            <h2>Tag cloud</h2>
            <div id="sort">Sort:
                <c:choose>
                      <c:when test="${actionBean.cloudSorted == 'count'}" >
                        <a href="tagSearch.action?sortByName">Alphabetically</a> | By popularity
                      </c:when>
                      <c:otherwise>
                        Alphabetically | <a href="tagSearch.action?sortByCount">By popularity</a>
                      </c:otherwise>
                  </c:choose>
            </div>
            <div id="cloud">
                <c:choose>
                    <c:when test="${empty actionBean.tagCloud}">
                        <p class="system-msg">No tags found</p>
                    </c:when>
                    <c:otherwise>
                        <c:forEach items="${actionBean.tagCloud}" var="tagEntry">
                            <c:url var="tagClass" value="size${tagEntry.scale}" />
                            <a href="tagSearch.action?search=Search&searchTag=${tagEntry.tag }" class="${tagClass}" >${tagEntry.tag }</a>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:otherwise>
    </c:choose>

    <div id="queryDialog" title="Search query">
        <c:if test="${not empty actionBean.queryString}">
            <pre><c:out value="${actionBean.queryString}" /></pre>
            <crfn:form action="/sparql" method="get">
                <stripes:hidden name="query" value="${actionBean.queryString}" />
                <br />
                <br />
                <stripes:submit name="noEvent" value="Edit SPARQL query" />
            </crfn:form>
        </c:if>
    </div>
    </stripes:layout-component>
</stripes:layout-render>
