<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="SPARQL endpoint">

    <stripes:layout-component name="head">
        <script type="text/javascript">
            var last_format = 1;
            function format_select(query_obg) {
                var query = query_obg.value;
                var format = query_obg.form.format;

                if ((query.match(/\bconstruct\b/i) || query.match(/\bdescribe\b/i)) && last_format == 1) {
                    for ( var i = format.options.length; i > 0; i--){
                        format.options[i] = null;
                    }
                    format.options[0] = new Option('HTML', 'text/html');
                    format.options[1] = new Option('RDF/XML', 'application/rdf+xml');
                    format.selectedIndex = 1;
                    last_format = 2;
                }

                if (!(query.match(/\bconstruct\b/i) || query.match(/\bdescribe\b/i)) && last_format == 2) {
                    for ( var i = format.options.length; i > 0; i--){
                        format.options[i] = null;
                    }
                    format.options[0] = new Option('HTML', 'text/html');
                    format.options[1] = new Option('XML', 'application/sparql-results+xml');
                    format.options[2] = new Option('JSON', 'application/sparql-results+json');
                    format.selectedIndex = 1;
                    last_format = 1;
                }
            }

            </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <h1>SPARQL endpoint</h1>

        <div>
            <crfn:form name="bookmarksForm" action="/sparql" method="get">
              <c:if test="${empty actionBean.bookmarkedQueries}">
                  <p>Type a SPARQL query, select output format and other options, and press Execute.</p>
              </c:if>
              <c:if test="${not empty actionBean.bookmarkedQueries}">
                  <p>Type a SPARQL query, or select one from bookmarks:</p>
                  <div>
                    <stripes:select name="fillfrom" onchange="document.bookmarksForm.submit()" style="width:500px">
                         <stripes:option value="" label="-- Select a bookmarked SPARQL query --" />
                             <c:forEach items="${actionBean.bookmarkedQueries}" var="bookmarkedQuery">
                                 <stripes:option value="${bookmarkedQuery.subj}" label="${bookmarkedQuery.label}" />
                             </c:forEach>
                    </stripes:select>
                    <noscript><div><stripes:submit name="" value="Go" id="goButton" /></div></noscript>
                    <br/><a href="sparql?deleteBookmarked=">Delete bookmarked queries ...</a>
                  </div>
              </c:if>
            </crfn:form>
        </div>

        <div style="margin-top: 15px">
                    <div style="float:right"><a href="documentation/sparqlfunctions">SPARQL Functions</a></div>
            <crfn:form name="mainForm" action="/sparql" method="get">
                <c:if test="${not empty actionBean.defaultGraphUris}">
                    <c:forEach var="defaultGraphUri" items="${actionBean.defaultGraphUris}">
                        <input type="hidden" name="default-graph-uri" value="${defaultGraphUri}" />
                    </c:forEach>
                </c:if>
                <c:if test="${not empty actionBean.namedGraphUris}">
                    <c:forEach var="namedGraphUri" items="${actionBean.namedGraphUris}">
                        <input type="hidden" name="named-graph-uri" value="${namedGraphUri}" />
                    </c:forEach>
                </c:if>
                <div>
                    <label for="queryText" class="question">Query:</label>
                    <textarea name="query" id="queryText" rows="8" cols="80" style="clear:right; display: block; width: 100%" onchange="format_select(this)" onkeyup="format_select(this)">
<c:if test="${empty actionBean.query}">PREFIX rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt;

SELECT DISTINCT * WHERE {
  _:subj a ?class .
  OPTIONAL { ?class rdfs:label ?label }
} LIMIT 50
</c:if>${actionBean.query}</textarea>
                </div>
                <div style="position: relative; margin-bottom: 30px">
                    <div style="position: absolute; top: 5px; left: 0px;">
                        <label for="format" class="question" style="width: 200px;">Output format:</label>
                            <stripes:select name="format" id="format">
                                <stripes:option value="text/html" label="HTML" />
                                <stripes:option value="text/html+" label="HTML+" />
                                <stripes:option value="application/sparql-results+json" label="JSON" />
                                <stripes:option value="application/sparql-results+xml" label="XML" />
                                <stripes:option value="application/x-ms-access-export+xml" label="XML with Schema" />
                            </stripes:select>
                    </div>
                    <div style="position: absolute; top: 5px; left: 250px;">
                        <stripes:label for="nrOfHits" class="question">Hits per page</stripes:label>
                        <stripes:text name="nrOfHits" size="2" id="nrOfHits" />
                    </div>
                    <div style="position: absolute; top: 5px; left: 410px;">
                        <stripes:label for="inferencing" class="question">Use CR inferencing</stripes:label>
                        <stripes:checkbox name="useInferencing" id="inferencing" />
                    </div>

                    <c:choose>
                        <c:when test="${actionBean.user != null}">
                            <div style="position: absolute; top: 5px; left: 590px;">
                                <stripes:submit name="execute" value="Execute" id="executeButton" />
                                <stripes:submit name="bookmark" value="Bookmark" id="bookmarkButton" />
                                <stripes:hidden name="bookmarkName" value="${actionBean.bookmarkName}"/>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div style="position: absolute; top: 5px; right: 0px;">
                                <stripes:submit name="execute" value="Execute" id="executeButton" />
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div style="clear:both">
                    <c:if test="${not empty actionBean.query && empty param.bookmark && empty param.fillfrom}">
                        <c:if test="${actionBean.result.allRowsReturned == 'false'}">
                            <div class="important-msg">The query result exceeded maximum allowed row count. Displaying only first rows. Please run a more detailed query.</div>
                        </c:if>
                        <c:choose>
                            <c:when test="${not empty actionBean.result && not empty actionBean.result.rows}">
                                <br />
                                <display:table name="${actionBean.result.rows}" class="datatable"
                                    pagesize="${actionBean.nrOfHits}" sort="list" id="listItem"
                                    htmlId="listItem" requestURI="/sparql"
                                    decorator="eionet.cr.web.sparqlClient.helpers.SparqlClientColumnDecorator">
                                    <c:forEach var="cl" items="${actionBean.result.cols}">
                                        <display:column property="map(${cl.property})"
                                            title="${cl.title}" sortable="${cl.sortable}" />
                                    </c:forEach>
                                </display:table>
                                <br/>Done. -- ${actionBean.executionTime} ms.
                            </c:when>
                            <c:when test="${actionBean.askQuery == 'true'}">
                                <br/><c:out value="${actionBean.resultAsk}"/>
                            </c:when>
                            <c:otherwise>
                                <div class="system-msg">The query gave no results!</div>
                            </c:otherwise>
                        </c:choose>
                    </c:if>

                    <c:if test="${empty actionBean.result || empty actionBean.result.rows}">
                        <div>
                            <h2>Useful namespaces</h2>
                            <pre>
PREFIX rdf: &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt;
PREFIX rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt;
PREFIX xsd: &lt;http://www.w3.org/2001/XMLSchema#&gt;
PREFIX owl: &lt;http://www.w3.org/2002/07/owl#&gt;
PREFIX dc: &lt;http://purl.org/dc/elements/1.1/&gt;
PREFIX dcterms: &lt;http://purl.org/dc/terms/&gt;
PREFIX foaf: &lt;http://xmlns.com/foaf/0.1/&gt;
PREFIX geo: &lt;http://www.w3.org/2003/01/geo/wgs84_pos#&gt;
PREFIX cr: &lt;http://cr.eionet.europa.eu/ontologies/contreg.rdf#&gt;
PREFIX rod: &lt;http://rod.eionet.europa.eu/schema.rdf#&gt;
                            </pre>
                        </div>
                    </c:if>
                </div>
            </crfn:form>
        </div>
    </stripes:layout-component>
</stripes:layout-render>
