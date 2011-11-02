<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="SPARQL endpoint">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
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

            ( function($) {
                $(document).ready(
                    function(){

                        // Open delete bookmarked queries dialog
                        $("#bookmarksLink").click(function() {
                            $('#bookmarksDialog').dialog('open');
                            return false;
                        });

                        // Delete bookmarked queries dialog setup
                        $('#bookmarksDialog').dialog({
                            autoOpen: false,
                            width: 600
                        });

                        // Close dialog
                        $("#deleteBookmarked").click(function() {
                            $('#bookmarksDialog').dialog("close");
                            return true;
                        });

                    });
            } ) ( jQuery );
            // ]]>
        </script>
        <style type="text/css" media="screen">
/*<![CDATA[*/
div#workarea textarea, 
div#workarea pre {
  margin: 0;
  padding: 0;
  outline: 0;
  border: 0;
}

/* The containing element has the expandingArea
   class, define any border or inset box-shadow
   etc. here that you want to style your textarea.
   I've just added a simple 1px solid mid-grey
   border. You can also set a min-height property
   here if you want and it works as expected.
 */
div#workarea .expandingArea {
  position: relative;
  border: 1px solid #888;
  background: #fff;
}
/* Any padding should go in here; it needs to be
   the same for both the pre and the textarea. You
   also need to make sure both have exactly the
   same font and line height properties, although
   the exact values can of course be anything you
   want.
 */
div#workarea .expandingArea > textarea,
div#workarea .expandingArea > pre {
  padding: 5px;
  background: transparent;
  font: 400 13px/16px helvetica, arial, sans-serif;
  /* Make the text soft-wrap */
  white-space: pre-wrap;
  word-wrap: break-word;
}

div#workarea .expandingArea > textarea {
  /* The border-box box model is used to allow
     padding whilst still keeping the overall width
     at exactly that of the containing element.
   */
  -webkit-box-sizing: border-box;
     -moz-box-sizing: border-box;
      -ms-box-sizing: border-box;
          box-sizing: border-box;
  /* This height is used when JS is disabled */
  height: 100px;
  width: 100%;
}

div#workarea .expandingArea.active > textarea {
  /* Hide any scrollbars */
  overflow: hidden;
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  /* Remove WebKit user-resize widget */
  resize: none;
}

div#workarea .expandingArea > pre {
  display: none;
}
div#workarea .expandingArea.active > pre {
  display: block;
  /* We don't actually want to see this text, it's just for sizing */
  visibility: hidden;
}
/*]]>*/
        </style>

    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <h1>SPARQL endpoint</h1>

        <div style="margin-top: 15px">
            <div style="float:right">
                <a href="documentation/sparqlfunctions" title="Press Ctrl and click to open help on SPARQL Functions in a new window">SPARQL Functions</a>
            </div>
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
                    <div>
                        <ul>
                        <c:if test="${not empty actionBean.defaultGraphUris}">
                            <c:forEach var="defaultGraphUri" items="${actionBean.defaultGraphUris}">
                                <li><b>default-graph-uri:</b> <c:out value="${defaultGraphUri}" /></li>
                            </c:forEach>
                        </c:if>
                        <c:if test="${not empty actionBean.namedGraphUris}">
                            <c:forEach var="namedGraphUri" items="${actionBean.namedGraphUris}">
                                <li><b>named-graph-uri:</b> <c:out value="${namedGraphUri}" /></li>
                            </c:forEach>
                        </c:if>
                        </ul>
                    </div>
                    <div class="expandingArea">
                    <pre><span></span><br /></pre>
                    <textarea name="query" id="queryText" rows="8" cols="80" style="clear:right; display: block; width: 100%" onchange="format_select(this)" onkeyup="format_select(this)">
<c:if test="${empty actionBean.query}">PREFIX rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt;

SELECT DISTINCT * WHERE {
  _:subj a ?class .
  OPTIONAL { ?class rdfs:label ?label }
} LIMIT 50
</c:if>${actionBean.query}</textarea>
                    </div>
                    <script type="text/javascript">
// <![CDATA[
function makeExpandingArea(container) {
 var area = container.querySelector('textarea');
 var span = container.querySelector('span');
 if (area.addEventListener) {
   area.addEventListener('input', function() {
     span.textContent = area.value;
   }, false);
   span.textContent = area.value;
 } else if (area.attachEvent) {
   // IE8 compatibility
   area.attachEvent('onpropertychange', function() {
     span.innerText = area.value;
   });
   span.innerText = area.value;
 }
 // Enable extra CSS
 container.className += ' active';
}

var areas = document.querySelectorAll('.expandingArea');
var l = areas.length;

while (l--) {
 makeExpandingArea(areas[l]);
}
// ]]>
                    </script>
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
                                <c:if test="${not empty actionBean.bookmarkedQueries}">
                                    <button id="bookmarksLink">Bookmarked queries</button>
                                </c:if>
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

            <%-- Bookmarked queries dialog --%>
            <div id="bookmarksDialog" title="Bookmarked queries">
                <div>
                    <h1>Select one of SPARQL queries</h1>
                    <crfn:form name="bookmarksForm" action="/sparql" method="get">
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

                        <stripes:select name="fillfrom" onchange="document.bookmarksForm.submit()" style="width:500px">
                             <stripes:option value="" label="-- Select a bookmarked SPARQL query --" />
                                 <c:forEach items="${actionBean.bookmarkedQueries}" var="bookmarkedQuery">
                                     <stripes:option value="${bookmarkedQuery.subj}" label="${bookmarkedQuery.label}" />
                                 </c:forEach>
                        </stripes:select>
                        <noscript><div><stripes:submit name="" value="Go" id="goButton" /></div></noscript>
                    </crfn:form>
                </div>
                <br />
                <br />
                <div>
                    <h1>Delete bookmarked SPARQL queries</h1>

                    <c:if test="${not empty actionBean.bookmarkedQueries}">
                        <div style="margin-top: 15px">
                            <c:url var="deleteBookmarkUrl" value="/sparql">
                                <c:if test="${not empty actionBean.defaultGraphUris}">
                                    <c:forEach var="defaultGraphUri" items="${actionBean.defaultGraphUris}">
                                        <c:param name="default-graph-uri" value="${defaultGraphUri}" />
                                    </c:forEach>
                                </c:if>
                                <c:if test="${not empty actionBean.namedGraphUris}">
                                    <c:forEach var="namedGraphUri" items="${actionBean.namedGraphUris}">
                                        <c:param name="named-graph-uri" value="${namedGraphUri}" />
                                    </c:forEach>
                                </c:if>
                            </c:url>
                            <crfn:form id="bookmarkedQueriesForm" action="${deleteBookmarkUrl}" method="post">
                                <div>
                                    <stripes:submit name="deleteBookmarked" id="deleteBookmarked" value="Delete" title="Delete the bookmarked queries that you have selected below"/>
                                    <input type="button" name="selectAll" value="Select all" onclick="toggleSelectAll('bookmarkedQueriesForm');return false"/>
                                </div>
                                <table>
                                    <c:forEach items="${actionBean.bookmarkedQueries}" var="bookmarkedQuery">
                                        <tr>
                                            <td>
                                                <stripes:checkbox value="${bookmarkedQuery.subj}" name="deleteQueries"/>
                                            </td>
                                            <td>
                                                <span title="${bookmarkedQuery.queryString}"><c:out value="${bookmarkedQuery.label}"/></span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </table>
                            </crfn:form>
                        </div>
                    </c:if>
                    <c:if test="${empty actionBean.bookmarkedQueries}">
                        <div class="note-msg">No bookmarked queries currently found!</div>
                    </c:if>
                </div>
            </div>

        </div>

    </stripes:layout-component>
</stripes:layout-render>
