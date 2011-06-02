<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="User Folders">
<stripes:layout-component name="contents">

    <h1>User Folders</h1>
    <c:choose>
        <c:when test="${not empty actionBean.resultList}">
            <p>User folder can contain files and sub-folders. There are 4 special folders registered under each user's home folder: registrations, bookmarks, reviews, history.</p>
            <display:table name="${actionBean.resultList}" class="sortable"
                pagesize="20" sort="list" id="folder" htmlId="resultList"
                requestURI="${actionBean.urlBinding}" style="width:100%">
                    <display:column title="User home folders" sortable="true">
                        <stripes:link href="${folder.url}">
                           ${folder.label}
                        </stripes:link>  (${fn:length(folder.subFiles)} files, ${fn:length(folder.subFolders)} folders),
                        <stripes:link href="${folder.url}/bookmarks">
                           Bookmarks
                        </stripes:link>,
                        <stripes:link href="${folder.url}/registrations">
                           Registrations
                        </stripes:link>,
                        <stripes:link href="${folder.url}/history">
                           History
                        </stripes:link>,
                        <stripes:link href="${folder.url}/reviews">
                           Reviews
                        </stripes:link>
                    </display:column>
            </display:table>
        </c:when>
        <c:otherwise>
            <p>Users have not registered any folders yet.</p>
        </c:otherwise>
    </c:choose>
</stripes:layout-component>
</stripes:layout-render>
