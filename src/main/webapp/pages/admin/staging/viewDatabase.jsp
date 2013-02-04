<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Staging databases">

    <c:if test="${actionBean.dbDTO != null}">
        <stripes:layout-component name="head">
            <script type="text/javascript">
            // <![CDATA[
                ( function($) {
                    $(document).ready(
                        function(){

                            // onClick handler for links that open database import log popups
                            $("[id^=importLogLink]").click(function() {
                                $('#importLogDialog').dialog('open');
                                $('#importLogDialog').append('<img src="${pageContext.request.contextPath}/images/wait.gif" alt="Wait clock"/>');
                                $('#importLogDialog').load($(this).attr("href"));
                                return false;
                            });

                            // Setup of the modal dialog that displays a database's import log
                            $('#importLogDialog').dialog({
                                autoOpen: false,
                                height: 500,
                                width: 700,
                                maxHeight: 800,
                                maxWidth: 800,
                                modal: true,
                                closeOnEscape: true
                            });
                        });
                } ) ( jQuery );
            // ]]>
            </script>
        </stripes:layout-component>
    </c:if>

    <stripes:layout-component name="contents">

        <c:if test="${actionBean.dbDTO != null}">

            <%-- Drop-down operations --%>

            <ul id="dropdown-operations">
                <li><a href="#">Operations</a>
                    <ul>
                        <li>
                            <stripes:link beanclass="${actionBean.exportRDFActionBeanClass.name}" title="Run RDF export from this database">
                                <c:out value="Export RDF"/>
                                <stripes:param name="dbName" value="${actionBean.dbDTO.name}"/>
                            </stripes:link>
                        </li>
                        <li>
                            <stripes:link id="importLogLink_1" href="${actionBean.stagingDatabasesActionBeanUrlBinding}" event="openLog" title="View the import log">
                                <c:out value="View import log"/>
                                <stripes:param name="databaseId" value="${actionBean.dbDTO.id}"/>
                            </stripes:link>
                        </li>
                    </ul>
                </li>
            </ul>

            <%-- The page's heading --%>

            <h1>Staging database detailed view</h1>

            <div style="margin-top:20px">
                This page displays the details known about the below staging database, and provides operations in the upper right menu.
            </div>

            <%-- The table with the database's details. --%>

            <div style="padding-top:20px">
                <table class="datatable" style="width:75%">
                    <colgroup>
                        <col width="20%">
                        <col width="80%">
                    </colgroup>
                    <tr>
                        <th class="question" style="text-align:right" title="Name of the database">Name:</th>
                        <td><c:out value="${actionBean.dbDTO.name}"/></td>
                    </tr>
                    <tr>
                        <th class="question" style="text-align:right" title="The user who created this database">Creator:</th>
                        <td><c:out value="${actionBean.dbDTO.creator}"/></td>
                    </tr>
                    <tr>
                        <th class="question" style="text-align:right" title="The date and time the database was created">Created:</th>
                        <td><fmt:formatDate value="${actionBean.dbDTO.created}" pattern="yyyy-MM-dd HH:mm:ss" /></td>
                    </tr>
                    <tr>
                        <th class="question" style="text-align:right" title="The database's import status">Import status:</th>
                        <td>
                            <stripes:link id="importLogLink_2" href="${actionBean.stagingDatabasesActionBeanUrlBinding}" event="openLog" title="View the import log">
                                <c:out value="${actionBean.dbDTO.importStatus}"/>
                                <stripes:param name="databaseId" value="${actionBean.dbDTO.id}"/>
                            </stripes:link>
                        </td>
                    </tr>
                    <tr>
                        <th class="question" style="text-align:right" title="Description of the database's purpose and meaning">Description:</th>
                        <td><c:out value="${actionBean.dbDTO.description}"/></td>
                    </tr>
                </table>
                <div style="width:75%;padding-top:20px;background-color:#F0F0F0;vertical-align:middle;align:center">
                    Here will probably be the list of tables in this database, and their columns.
                </div>
            </div>

            <%-- The div that is invisible by default, but will be displayed and filled once the "open import log" operation is invoked. --%>
            <div id="importLogDialog" title="Import log"></div>

        </c:if>

    </stripes:layout-component>
</stripes:layout-render>
