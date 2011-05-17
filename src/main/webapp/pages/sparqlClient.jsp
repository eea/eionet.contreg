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
                    for ( var i = format.options.length; i > 0; i--){}
                        format.options[i] = null;
                    }
                    format.options[1] = new Option('HTML', 'text/html');
                    format.options[2] = new Option('RDF/XML', 'application/rdf+xml');
                    format.selectedIndex = 1;
                    last_format = 2;
                }

                if (!(query.match(/\bconstruct\b/i) || query.match(/\bdescribe\b/i)) && last_format == 2) {
                    for ( var i = format.options.length; i > 0; i--){}
                        format.options[i] = null;
                    }
                    format.options[1] = new Option('HTML', 'text/html');
                    format.options[2] = new Option('XML', 'application/sparql-results+xml');
                    format.options[3] = new Option('JSON', 'application/sparql-results+json');
                    format.selectedIndex = 1;
                    last_format = 1;
                }
            }
        </script>
    </stripes:layout-component>
    
    <stripes:layout-component name="contents">

        <h1>SPARQL endpoint</h1>

        <div>
            <stripes:form name="bookmarksForm" action="/sparql" method="get">
                <p>
                    <c:if test="${empty actionBean.bookmarkedQueries}">
                        Type a SPARQL query, select output format and other options, and press Execute.
                    </c:if>
                    <c:if test="${not empty actionBean.bookmarkedQueries}">
                        Type a SPARQL query, or select one from bookmarks:
                        <stripes:select name="fillfrom" onchange="document.bookmarksForm.submit()" style="width:500px">
                             <stripes:option value="" label="-- Select a bookmarked SPARQL query --" />
                                 <c:forEach items="${actionBean.bookmarkedQueries}" var="bookmarkedQuery">
                                     <stripes:option value="${bookmarkedQuery.key}" label="${bookmarkedQuery.value}" />
                                 </c:forEach>
                        </stripes:select>
                        <noscript><stripes:submit name="" value="Go" id="goButton" /></noscript>
                    </c:if>
                </p>
            </stripes:form>
        </div>

        <div style="margin-top: 15px">
            <stripes:form name="mainForm" action="/sparql" method="get">
                <div>
                    <label for="queryText" class="question">Query:</label>
                    <textarea name="query" id="queryText" rows="8" cols="80" style="display: block; width: 100%" onchange="format_select(this)" onkeyup="format_select(this)">
<c:if test="${empty actionBean.query}">PREFIX rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt;

SELECT DISTINCT * WHERE {
  _:subj a ?class .
  OPTIONAL { ?class rdfs:label ?label }
} LIMIT 50
</c:if>${actionBean.query}
                    </textarea>
                </div>
	            <div style="position: relative; margin-bottom: 30px">
		            <div style="position: absolute; top: 5px; left: 0px;">
	                    <label for="format" class="question">Output format:</label>
	                        <stripes:select name="format" id="format">
		                        <stripes:option value="text/html" label="HTML" />
								<stripes:option value="application/sparql-results+json" label="JSON" />
								<stripes:option value="application/sparql-results+xml" label="XML" />
	                        </stripes:select>
	                </div>
                    <div style="position: absolute; top: 5px; left: 180px;">
                        <stripes:label for="nrOfHits" class="question">Hits per page</stripes:label>
                        <stripes:text name="nrOfHits" size="2" id="nrOfHits" />
                    </div>
                    <div style="position: absolute; top: 5px; left: 340px;">
                        <stripes:label for="inferencing" class="question">Use CR inferencing</stripes:label>
                        <stripes:checkbox name="useInferencing" id="inferencing" />
                    </div>
                    
                    <c:choose>
                        <c:when test="${actionBean.user != null}">
                            <div style="position: absolute; top: 5px; left: 520px;">
	                            <stripes:submit name="execute" value="Execute" id="executeButton" />
	                            <stripes:submit name="bookmark" value="Bookmark with name:" id="bookmarkButton" />
	                            <stripes:text name="bookmarkName" size="20" id="bookmarkName" />
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div style="position: absolute; top: 5px; right: 0px;">
                                <stripes:submit name="execute" value="Execute" id="executeButton" />
                            </div>
                        </c:otherwise>
                    </c:choose>
		        </div>
		            
                <div>
	                <c:if test="${not empty actionBean.query && empty param.bookmark && empty param.fillfrom}">
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
		                        <br />Done. -- ${actionBean.executionTime} ms.
	                        </c:when>
	                        <c:when test="${actionBean.askQuery == 'true'}">
	                            <br />
	                            ${actionBean.resultAsk}
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
            </stripes:form>
        </div>
    </stripes:layout-component>
</stripes:layout-render>
