<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

    <stripes:layout-component name="contents">

        <cr:tabMenu tabs="${actionBean.tabs}" />

        <br style="clear:left" />

        <div style="margin-top:20px">

            <c:if test="${actionBean.usersFolder}">
            <ul id="dropdown-operations">
                <li><a href="#">Operations</a>
                    <ul>
                        <li>
                            <stripes:link href="/folder.action" event="uploadForm">
                                <stripes:param name="uri" value="${actionBean.uri}"/>
                                Upload file
                           </stripes:link>
                        </li>
                        <li>
                            <stripes:link href="/uploadCSV.action">
                                <stripes:param name="uri" value="${actionBean.uri}"/>
                                Upload CSV/TSV file
                            </stripes:link>
                        </li>
                        <li>
                            <stripes:link class="link-plain" href="/factsheet.action?edit=&uri=${actionBean.user.homeUri}" title="Edit your home url properties">
                            Edit folder
                            </stripes:link>
                        </li>
                    </ul>
                </li>
            </ul>
            </c:if>

            <h1>
                ${actionBean.parentFolder.name}
                <c:if test="${not empty actionBean.parentFolder.title}">
                    (${actionBean.parentFolder.title})
                </c:if>
            </h1>

            <crfn:form id="uploadsForm" action="/folder.action" method="post">
                <stripes:hidden name="uri" value="${actionBean.uri}" />
                <table>
                    <tbody>
                    <c:forEach var="item" items="${actionBean.folderItems}">
                        <c:choose>
                            <c:when test="${item.folder}">
                                <c:set var="cssClass" value="folder" />
                            </c:when>
                            <c:when test="${item.reservedFolder}">
                                <c:set var="cssClass" value="reservedFolder" />
                            </c:when>
                            <c:otherwise>
                                <c:set var="cssClass" value="file" />
                            </c:otherwise>
                        </c:choose>

                        <tr>
                            <c:if test="${actionBean.usersFolder}">
                                <td><input type="checkbox" value="${item.uri}" name="subjectUris" /></td>
                            </c:if>
                            <td class="${cssClass}" style="width: 100%">
                                <stripes:link href="factsheet.action">
                                    <stripes:param name="uri" value="${item.uri}"/>
                                    ${item.name}
                                </stripes:link>
                                <c:if test="${not empty item.title}">
                                    (${item.title})
                                </c:if>
                            </td>
                            <td>
                                ${item.lastModified}
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>

                <c:if test="${actionBean.usersFolder}">
                    <br />
                    <div>
                        <stripes:submit name="delete" value="Delete" title="Delete selecetd files"/>
                        <stripes:submit name="renameForm" value="Rename" title="Rename selecetd file"/>
                        <input type="button" name="selectAll" value="Select all" onclick="toggleSelectAll('uploadsForm');return false"/>
                    </div>
                </c:if>

            </crfn:form>

        </div>

    </stripes:layout-component>

</stripes:layout-render>
