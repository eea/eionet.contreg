<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
            ( function($) {
                $(document).ready(
                    function(){

                        // Open folder creation dialog
                        $("#createFolderLink").click(function() {
                            $('#createFolderDialog').dialog('open');
                            return false;
                        });

                        // Folder creation dialog setup.
                        $('#createFolderDialog').dialog({
                            autoOpen: false,
                            width: 500
                        });

                        // Close folder creation dialog
                        $("#closeFolderDialog").click(function() {
                            $('#createFolderDialog').dialog("close");
                            return true;
                        });
                    });
            } ) ( jQuery );
        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <cr:tabMenu tabs="${actionBean.tabs}" />

        <br style="clear:left" />

        <div style="margin-top:20px">

            <c:if test='${(actionBean.usersFolder || crfn:userHasPermission(pageContext.session, actionBean.aclPath, "i"))}'>
                <ul id="dropdown-operations">
                    <li><a href="#">Operations</a>
                        <ul>
                            <li>
                                <a href="#" id="createFolderLink">Create folder</a>
                            </li>
                            <li>
                                <stripes:link href="/folder.action" event="uploadForm">
                                    <stripes:param name="uri" value="${actionBean.uri}"/>
                                    Upload file
                                </stripes:link>
                            </li>
                            <li>
                                <stripes:link href="/uploadCSV.action">
                                    <stripes:param name="folderUri" value="${actionBean.uri}"/>
                                    Upload CSV/TSV file
                                </stripes:link>
                            </li>
                            <c:if test='${(actionBean.usersFolder || crfn:userHasPermission(pageContext.session, actionBean.aclPath, "u"))}'>
                                <li>
                                    <stripes:link class="link-plain" href="/factsheet.action?edit=&uri=${actionBean.uri}" title="Edit folder properties">
                                    Edit folder
                                    </stripes:link>
                                </li>
                            </c:if>
                        </ul>
                    </li>
                </ul>
            </c:if>

            <h1>
                <c:choose>
                    <c:when test="${actionBean.projectFolder}">
                        Projects folder
                    </c:when>
                    <c:when test="${actionBean.homeFolder}">
                        Home folder (${actionBean.folder.name})
                    </c:when>
                    <c:otherwise>
                        ${actionBean.folder.name}
                        <c:if test="${not empty actionBean.folder.title}">
                            (${actionBean.folder.title})
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </h1>

            <crfn:form id="uploadsForm" action="/folder.action" method="post">

                <table class="datatable" style="width:100%">
                    <colgroup>
                        <c:if test='${actionBean.usersFolder || crfn:userHasPermission(pageContext.session, actionBean.aclPath, "d")}'>
                            <col style="width: 1em;" />
                        </c:if>
                        <col />
                        <col style="width: 12em;" />
                    </colgroup>
                    <c:if test="${!actionBean.homeFolder && !actionBean.projectFolder}">
                        <tr>
                            <c:if test='${actionBean.usersFolder || crfn:userHasPermission(pageContext.session, actionBean.aclPath, "d")}'>
                                <td></td>
                            </c:if>
                            <td class="upFolder">
                                [<stripes:link href="/view.action" title="Move to parent folder" style="background: none">
                                    <stripes:param name="uri" value="${actionBean.parentUri}"/>
                                    Parent Directory
                                </stripes:link>]
                            </td>
                            <td></td>
                        </tr>
                    </c:if>
                    <c:forEach var="item" items="${actionBean.folderItems}" varStatus="loop">
                        <c:choose>
                            <c:when test="${item.folder || item.reservedFolder}">
                                <c:set var="cssClass" value="folder" />
                            </c:when>
                            <c:otherwise>
                                <c:set var="cssClass" value="file" />
                            </c:otherwise>
                        </c:choose>

                        <tr>
                            <c:if test='${actionBean.usersFolder || crfn:userHasPermission(pageContext.session, actionBean.aclPath, "d")}'>
                                <c:set var="disabled" value="${item.reservedFolder || item.reservedFile}" />
                                <td>
                                    <stripes:checkbox name="selectedItems[${loop.index}].selected" disabled="${disabled}" />
                                    <stripes:hidden name="selectedItems[${loop.index}].uri" value="${item.uri}" />
                                    <stripes:hidden name="selectedItems[${loop.index}].type" value="${item.type}" />
                                    <stripes:hidden name="selectedItems[${loop.index}].name" value="${item.name}" />
                                </td>
                            </c:if>
                            <td class="${cssClass}">
                                <stripes:link href="/view.action">
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
                </table>

                <c:if test='${actionBean.usersFolder || crfn:userHasPermission(pageContext.session, actionBean.aclPath, "d")}'>
                    <div>
                        <stripes:submit name="delete" value="Delete" title="Delete selected files"/>
                        <stripes:submit name="renameForm" value="Rename" title="Rename selected file"/>
                        <input type="button" name="selectAll" value="Select all" onclick="toggleSelectAll('uploadsForm');return false"/>
                    </div>
                </c:if>

                <fieldset style="display:none">
                    <stripes:hidden name="uri" value="${actionBean.uri}" />
                </fieldset>

            </crfn:form>

        </div>

        <%-- Add folder dialog --%>
        <div id="createFolderDialog" title="Create new folder">
            <crfn:form action="/folder.action" method="post">
                <fieldset style="border: 0px;">
                    <label for="txtTitle" style="width: 200px; float: left;">New folder name*:</label>
                    <stripes:text id="txtTitle" name="title"/>
                </fieldset>
                <fieldset style="border: 0px;">
                    <label for="txtLabel" style="width: 200px; float: left;">Short description:</label>
                    <stripes:text id="txtLabel" name="label"/>
                </fieldset>
                <fieldset style="border: 0px;">
                  <stripes:submit name="createFolder" value="Create" title="Create new folder"/>
                  <input type="button" id="closeFolderDialog" value="Cancel"/>
                </fieldset>
                <fieldset style="display:none">
                    <stripes:hidden name="uri" value="${actionBean.uri}" />
                </fieldset>
            </crfn:form>
        </div>

    </stripes:layout-component>

</stripes:layout-render>
