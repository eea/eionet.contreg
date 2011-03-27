<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Simple SPARQL client">

    <stripes:layout-component name="contents">

        <h1>Simple SPARQL client</h1>

        <p>
            Type a SPARQL SELECT query, select output format and press Execute.
        </p>

        <div style="margin-top:15px">
            <stripes:form action="/sparql" method="get">
                <div>
                            <label for="queryText" class="question">Query:</label>
                            <textarea name="query" id="queryText" rows="8" cols="80" style="display:block; width:100%"><c:if test="${empty actionBean.query}">PREFIX rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt;

SELECT DISTINCT ?class ?label WHERE {
  _:subj a ?class .
  OPTIONAL { ?class rdfs:label ?label }
} LIMIT 50 OFFSET 0</c:if>${actionBean.query}</textarea>
                </div>
                <div style="position: relative;">
            		<div style="position: absolute; top:5px; left:0px;">
                        <label for="format" class="question">Display result as:</label>
                        <stripes:select name="format" id="format">
                           	<stripes:option value="text/html" label="HTML"/>
                           	<stripes:option value="application/sparql-results+xml" label="XML"/>
                           	<stripes:option value="application/sparql-results+json" label="JSON"/>
                        </stripes:select>
                    </div>
                    <div style="position:absolute; top:5px; left:200px;">
                    	<stripes:label for="nrOfHits" class="question">Number of hits per page</stripes:label>
                    	<stripes:text name="nrOfHits" size="2" id="nrOfHits"/>
                    </div>
                    <div style="position: absolute; top:5px; right:0px;">
                    	<stripes:submit name="execute" value="Execute" id="executeButton"/>
                    </div>
                </div>
				<div style="margin-top:30px">
                <c:if test="${not empty actionBean.query}">
                    <c:choose>
                        <c:when test="${not empty actionBean.result && not empty actionBean.result.rows}">
                            <br/>
                            <display:table name="${actionBean.result.rows}" class="datatable" pagesize="${actionBean.nrOfHits}" sort="list" id="listItem" htmlId="listItem" requestURI="/sparql" decorator="eionet.cr.web.sparqlClient.helpers.SparqlClientColumnDecorator">
                                <c:forEach var="cl" items="${actionBean.result.cols}">
                                      <display:column property="map(${cl.property})" title="${cl.title}" sortable="${cl.sortable}"/>
                                </c:forEach>
                              </display:table>
                              <br/>Done. -- ${actionBean.executionTime} ms.
                        </c:when>
                        <c:otherwise>
                            <div class="system-msg">The query gave no results!</div>
                        </c:otherwise>
                    </c:choose>
                </c:if>
                </div>
            </stripes:form>
        </div>
    </stripes:layout-component>
</stripes:layout-render>
