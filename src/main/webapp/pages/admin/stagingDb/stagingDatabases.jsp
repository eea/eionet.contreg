<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Staging databases">

    <stripes:layout-component name="contents">

        <%-- Drop-down operations --%>

        <ul id="dropdown-operations">
            <li><a href="#">Operations</a>
                <ul>
                    <li>
                        <stripes:link href="/admin">Back to admin actions</stripes:link>
                    </li>
                    <li>
                        <stripes:link beanclass="${actionBean.databaseActionBeanClass.name}" event="add">
                            <c:out value="Load new database"/>
                        </stripes:link>
                    </li>
                </ul>
            </li>
        </ul>

        <%-- The page's heading --%>

        <h1>Staging databases</h1>

        <%-- The section that displays the databases list. --%>

        <c:if test="${not empty actionBean.databases}">
            <div style="width:75%;padding-top:20px">
            <display:table name="${actionBean.databases}" class="sortable" id="database" sort="list" requestURI="${actionBean.urlBinding}" style="width:100%">
                <display:column property="name" title="Name" sortable="true" style="width:50%"/>
                <display:column title="Created" sortable="true" style="width:25%">
                    <fmt:formatDate value="${database.created}" pattern="yyyy-MM-dd HH:mm:ss" />
                </display:column>
                <display:column property="creator" title="Creator" sortable="true" style="width:25%"/>
            </display:table>
            </div>
        </c:if>

        <%-- Message if no databases found. --%>

        <c:if test="${empty actionBean.databases}">
            <div class="system-msg">No staging databases found! Use operations menu to add one.</div>
        </c:if>

    </stripes:layout-component>
</stripes:layout-render>
