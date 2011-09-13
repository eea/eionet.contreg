<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Other SPARQL systems">

    <stripes:layout-component name="contents">

        <h1>Simple SPARQL client</h1>

        <p>
            Type a SPARQL SELECT query and select an endpoint on which you want to execute it.
            Then press Execute.
        </p>

        <div style="margin-top:15px">
            <stripes:form action="/sparqlclient" method="get">
                <div>
                            <label for="endpointSelect" class="question">SPARQL endpoint:</label>
                            <stripes:select name="endpoint" id="endpointSelect">
                                   <c:forEach var="endpoint" items="${actionBean.endpoints}">
                                    <stripes:option value="${endpoint}" label="${endpoint}"/>
                                   </c:forEach>
                               </stripes:select>
                </div>
                <div>
                            <label for="queryText" class="question">Query:</label>
                            <textarea name="query" id="queryText" rows="8" cols="80" style="display:block; width:100%"><c:if test="${empty actionBean.query}">PREFIX rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt;

SELECT DISTINCT ?class ?label WHERE {
  _:subj a ?class .
  OPTIONAL { ?class rdfs:label ?label }
} LIMIT 50 OFFSET 0</c:if>${actionBean.query}</textarea>
                            <stripes:submit name="execute" value="Execute" id="executeButton"/>
                </div>

                <c:if test="${not empty actionBean.query || not empty actionBean.explore}">
                    <c:choose>
                        <c:when test="${not empty actionBean.result && not empty actionBean.result.rows}">

                            <table class="datatable">
                                <thead>
                                    <c:forEach items="${actionBean.result.variables}" var="variable">
                                        <th><c:out value="${variable}"/></th>
                                    </c:forEach>
                                </thead>
                                <tbody>
                                    <c:forEach items="${actionBean.result.rows}" var="resultRow">
                                        <tr>
                                            <c:forEach items="${actionBean.result.variables}" var="variable">
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty resultRow[variable]}">
                                                            <c:choose>
                                                                <c:when test="${resultRow[variable].literal}">
                                                                    <c:out value="${resultRow[variable].value}"/>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <stripes:link href="/sparqlclient"><c:out value="${resultRow[variable].value}"/>
                                                                        <stripes:param name="explore" value="${fn:escapeXml(resultRow[variable].value)}"/>
                                                                        <stripes:param name="endpoint" value="${actionBean.endpoint}"/>
                                                                        <c:if test="${not empty actionBean.query}">
                                                                            <stripes:param name="query" value="${actionBean.query}"/>
                                                                        </c:if>
                                                                    </stripes:link>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </c:when>
                                                        <c:otherwise>
                                                            &nbsp;
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </c:forEach>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:when>
                        <c:otherwise>
                            <div class="system-msg">The query gave no results!</div>
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </stripes:form>
        </div>

    </stripes:layout-component>
</stripes:layout-render>
