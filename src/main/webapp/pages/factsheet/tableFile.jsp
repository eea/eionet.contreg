<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="CSV/TSV file contents">
    <stripes:layout-component name="contents">

        <cr:tabMenu tabs="${actionBean.tabs}" />

        <ul id="dropdown-operations">
            <li><a href="#">Operations</a>
                <ul>
                    <li>
                        <stripes:link beanclass="eionet.cr.web.action.SPARQLEndpointActionBean" event="execute">
                            <stripes:param name="format" value="text/html" />
                            <stripes:param name="nrOfHits" value="20" />
                            <stripes:param name="query" value="${actionBean.spqrqlQuery}" />
                            Edit query
                        </stripes:link>
                    </li>
                </ul>
            </li>
        </ul>

        <br style="clear:left" />
        <h1>CSV/TSV file contents</h1>

        <c:choose>
            <c:when test="${not empty actionBean.queryResult && not empty actionBean.queryResult.rows}">
                <br />
                <display:table name="${actionBean.queryResult.rows}" class="datatable"
                    sort="list" id="listItem"
                    htmlId="listItem" requestURI="/tableFile.action"
                    decorator="eionet.cr.web.sparqlClient.helpers.SparqlClientColumnDecorator">
                    <c:forEach var="cl" items="${actionBean.queryResult.cols}">
                        <display:column property="map(${cl.property})"
                            title="${cl.title}" sortable="${cl.sortable}" />
                    </c:forEach>
                </display:table>
            </c:when>
            <c:otherwise>
                <div class="system-msg">The query gave no results!</div>
            </c:otherwise>
        </c:choose>
    </stripes:layout-component>
</stripes:layout-render>
