<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="SPARQL endpoint" bodyAttribute="onLoad=\"format_select()\"">

    <stripes:layout-component name="head">

        <style type="text/css">
	        .close-link{
                display: block;
                float: right;
                position: absolute;
                top:13px;
                right: 5px;
                color: #404040;
                font-weight: 900;
                font-family: "Arial Black", Gadget, sans-serif;
                font-size: 0.8em;
                cursor: pointer;
                border: 1px solid #7A7A7A;
                border-radius: 3px;
                background-color: #EBEBEB;
                padding-left: 5px;
                padding-right: 5px;
            }
        </style>

        <script type="text/javascript" src="<c:url value="/scripts/useful_namespaces.js"/>"></script>

        <script type="text/javascript">
        // <![CDATA[
            var last_format = 1;

            function format_select() {
                var query_obg = document.getElementById('queryText');
                var query = query_obg.value;
                var format = query_obg.form.format;

                if ((query.match(/\bconstruct\b/i) || query.match(/\bdescribe\b/i)) && last_format == 1) {
                    for ( var i = format.options.length; i > 0; i--){
                        format.options[i] = null;
                    }
                    format.options[0] = new Option('HTML', 'text/html');
                    format.options[1] = new Option('RDF/XML', 'application/rdf+xml');
                    format.options[2] = new Option('Turtle', 'text/turtle');
                    format.options[3] = new Option('N3', 'text/n3');
                    format.options[4] = new Option('N-Triples', 'text/plain');
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
                        // Open bookmarked queries dialog
                        $("#bookmarksLink").click(function() {
                            $('#bookmarksDialog').dialog('open');
                            $('#bookmarksDialog').load( "${pageContext.request.contextPath}/sparql?ajaxrequest=true&ajaxRequestId=1" );
                            return false;
                        });

                        // Bookmarked queries dialog setup
                        $('#bookmarksDialog').dialog({
                            autoOpen: false,
                            width: 600
                        });

                        // Close dialog
                        $("#deleteBookmarked").click(function() {
                            $('#bookmarksDialog').dialog("close");
                            return true;
                        });

                        // Open shared bookmarked queries dialog
                        $("#sharedBookmarksLink").click(function() {
                            $('#sharedBookmarksDialog').dialog('open');
                            $('#sharedBookmarksDialog').load( "${pageContext.request.contextPath}/sparql?ajaxrequest=true&ajaxRequestId=2" );
                            return false;
                        });

                        // Shared bookmarked queries dialog setup
                        $('#sharedBookmarksDialog').dialog({
                            autoOpen: false,
                            width: 600
                        });


                        // Open project bookmarked queries dialog
                        $("#projectBookmarksLink").click(function() {
                            $('#projectBookmarksDialog').dialog('open');
                            $('#projectBookmarksDialog').load( "${pageContext.request.contextPath}/sparql?ajaxrequest=true&ajaxRequestId=3" );
                            return false;
                        });

                        // Project bookmarked queries dialog setup
                        $('#projectBookmarksDialog').dialog({
                            autoOpen: false,
                            width: 600
                        });

                        // Close dialog
                        $("#deleteSahredBookmarked").click(function() {
                            $('#sharedBookmarksDialog').dialog("close");
                            return true;
                        });

                        // Open prefixes dialog
                        $("#prefixesLink").click(function() {
                            $('#prefixesDialog').dialog('open');
                            return false;
                        });

                        // Prefixes dialog setup
                        $('#prefixesDialog').dialog({
                            autoOpen: false,
                            width: 600
                        });

                        // Close prefixes dialog
                        $("#closePrefixesDialog").click(function() {
                            $('#prefixesDialog').dialog("close");
                            return true;
                        });

                        // On-click handler for the "close div" links.
                        $(".close-link").click(function() {
                            $(this).parent().hide();
                        });

                        // The handling of useful namespaces
                        <c:forEach items="${actionBean.usefulNamespaces}" var="usefulNamespace" varStatus="usefulNamespacesLoop">
                        $("#prefix${usefulNamespacesLoop.index}").click(function() {
                            return handlePrefixClick("PREFIX ${usefulNamespace.key}: <${fn:escapeXml(usefulNamespace.value)}>");
                            });
                        </c:forEach>

                        $('form[name="mainForm"]').bind('submit', function() {
                            var queryLength = $('#queryText').val().length;
                            var $frm = $(this);
                            var maxQueryLength = 2 * 1024;
                            
                            if (queryLength > maxQueryLength) {
                                $frm.attr('method', 'post');
                            }
                        });
                    });
            } ) ( jQuery );
            // ]]>
        </script>
        <c:if test="${actionBean.userLoggedIn}">
            <script type="text/javascript">
                // <![CDATA[
                ( function($) {
                $(document).ready(
                    function(){
                        $("#executeButton").click(function() {
                            var query = document.getElementById('queryText').value;
                            var format = document.getElementById('format').value;

                            if (format == 'application/rdf+xml' && query.match(/\bconstruct\b/i)) {
                                $('#constructDialog').dialog('open');
                                document.getElementById('constructQuery').value = query;
                                document.getElementById('constructFormat').value = format;
                                return false;
                            } else {
                                return true;
                            }
                        });

                        // Construct queriy dialog setup
                        $('#constructDialog').dialog({
                            autoOpen: false,
                            width: 500
                        });

                        //Hide div w/id extra
                        if ($("input[name=exportType]:checked").val() != "HOMESPACE") {
                            $("#homespace_data").css("display","none");
                        }

                        // Add onclick handler to checkbox w/id checkme
                        $('input[name=exportType]').click(function(){
                            $("#homespace_data").toggle();
                        });

                        // Close dialog
                        $("#executeConstruct").click(function() {
                            $('#constructDialog').dialog("close");
                            return true;
                        });

                        // Toggle bulk action buttons
                        $("#bulkActionsLink").click(function() {
                            $("#bulkActions").toggle();
                            var isVisible = $("#bulkActions").is(":visible");
                            $("#input_displayBulkActions").val(isVisible ? "true" : "false");
                            return false;
                        });
                    });
                } ) ( jQuery );

                // ]]>
            </script>
        </c:if>

    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <ul id="dropdown-operations">
            <li><a href="#" onclick="return false;">Operations</a>
                <ul>
                    <li>
                        <a href="#" id="sharedBookmarksLink">Shared bookmarked queries</a>
                    </li>
                    <c:if test="${actionBean.user != null}">
                        <li>
                            <a href="#" id="bookmarksLink">My bookmarked queries</a>
                        </li>
                    </c:if>
                    <c:if test="${not empty(actionBean.projectBookmarkedQueries)}">
                        <li>
                            <a href="#" id="projectBookmarksLink">Bookmarks in projects</a>
                        </li>
                    </c:if>
                    <li>
                        <a href="#" id="prefixesLink">Useful namespaces</a>
                    </li>

                    <c:if test="${actionBean.bulkActionsAllowed}">
                       <li>
                           <a href="#" id="bulkActionsLink">Show/hide bulk actions</a>
                       </li>
                    </c:if>

                </ul>
            </li>
        </ul>

        <h1>SPARQL endpoint</h1>
        <c:if test="${not empty actionBean.selectedBookmarkName}">
            <h2>Bookmarked query: <c:out value="${actionBean.selectedBookmarkName}" /></h2>
        </c:if>

        <div style="margin-top: 15px">
            <div style="float:right">
                <a href="documentation/sparqlfunctions" title="Press Ctrl and click to open help on SPARQL Functions in a new window">SPARQL Functions</a>
            </div>
            <crfn:form name="mainForm" action="/sparql" method="get">
                <div>
                <stripes:hidden name="selectedBookmarkName" />
                </div>
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
                        <c:if test="${not empty actionBean.defaultGraphUris or not empty actionBean.namedGraphUris}">
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
                        </c:if>
                    </div>
                    <div class="expandingArea">
                    <pre><span></span><br /></pre>
                    <textarea name="query" id="queryText" rows="8" cols="80" style="clear:right; display: block; width: 100%" onchange="format_select()" onkeyup="format_select()"><c:if test="${empty actionBean.query}">
PREFIX rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt;
PREFIX cr: &lt;http://cr.eionet.europa.eu/ontologies/contreg.rdf#&gt;

SELECT *
WHERE {
  ?bookmark a cr:SparqlBookmark ;
            rdfs:label ?label
} LIMIT 50</c:if>${crfn:escapeHtml(actionBean.query)}</textarea>
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
                        <label for="format" class="question">Output format:</label>
                        <stripes:select name="format" id="format">
                            <stripes:option value="text/html" label="HTML" />
                            <stripes:option value="text/html+" label="HTML+" />
                            <stripes:option value="application/sparql-results+json" label="JSON" />
                            <stripes:option value="application/sparql-results+xml" label="XML" />
                            <stripes:option value="application/x-ms-access-export+xml" label="XML with Schema" />
                            <stripes:option value="text/csv" label="CSV" title="data in the CSV is separated by semicolons"/>
                            <stripes:option value="text/tab-separated-values" label="TSV" />
                        </stripes:select>
                    </div>
                    <div style="position: absolute; top: 5px; left: 250px;">
                        <stripes:label for="nrOfHits" class="question">Hits per page</stripes:label>
                        <stripes:text name="nrOfHits" size="2" id="nrOfHits" />
                    </div>
                    <div style="position: absolute; top: 5px; left: 410px;">
                        <stripes:label for="owlsameas" class="question">Use owl:SameAs</stripes:label>
                        <stripes:checkbox name="useOwlSameAs" id="owlsameas" />
                    </div>

                    <c:choose>
                        <c:when test="${actionBean.user != null}">
                            <div style="position: absolute; top: 5px; left: 590px; display:block;">
                                <stripes:submit name="execute" value="Execute" id="executeButton" />
                                <stripes:submit name="bookmark" value="Bookmark" id="bookmarkButton" />
                                <stripes:hidden name="bookmarkName" value="${actionBean.bookmarkName}"/>
                                <stripes:hidden name="displayBulkActions" id="input_displayBulkActions" value="${actionBean.displayBulkActions}"/>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div style="position: absolute; top: 5px; right: 0px;">
                                <stripes:submit name="execute" value="Execute" id="executeButton" />
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <c:if test="${actionBean.bulkActionsAllowed}">
                    <fieldset id="bulkActions" style="position: relative; width:95%; padding:10px; border: 1px solid black; background-color:#F8F8F8;${actionBean.displayBulkActions ? '' : 'display: none;'}">
                        <legend>Bulk actions</legend>
                        <a class="close-link">X</a>
                        <stripes:submit name="executeAddSources" value="Schedule urgent batch harvest..." title="Schedule all URLs returned in the first result set column for an urgent batch harvest." onclick="return confirm('All URLs returned in the first result set column will now be scheduled for urgent batch harvest. They will be picked up by the batch harvester when it runs. Are you sure you want to continue?');"/>
                        <stripes:submit name="executeDeleteSources" value="Schedule background deletion..." title="Schedule all URLs returned in the first result set column for background deletion." onclick="return confirm('All URLs returned in the first result set column will now be scheduled for background deletion. Are you sure you want to continue?');"/>
                    </fieldset>
                </c:if>

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
                        <br/>
                        <div class="advice-msg">
                            On this page you can execute various SPARQL queries against the backend that CR is running on.
                            For a more convenient use, you can insert common namespace prefixs into your query by selecting
                            "Useful namespaces" in the Operations menu and clicking the namespaces in the opening pop-up window.
                            The Operations menu also provides a list of shared bookmarked queries which you can select to
                            pre-fill your query.<br/><br/>
                            The output format of the query depends on the one you select from the above select box. In order
                            to make the query use owl:SameAs rule, turn on the relevant checkbox. A link to common useful
                            SPARQL functions is also available below the Operations menu.
                        </div>
                    </c:if>
                </div>
            </crfn:form>
        </div>

            <c:if test="${actionBean.userLoggedIn}">
                <div id="constructDialog" title="Save result to ">
                    <crfn:form name="constructForm" action="/sparql" method="get">
                        <div>
                        <stripes:hidden name="query" id="constructQuery"/>
                        <stripes:hidden name="format" id="constructFormat"/>
                        </div>
                        <table class="formtable">
                            <tr>
                                <td style="width:120px"><stripes:label for="toFile">To file</stripes:label></td>
                                <td>
                                    <stripes:radio name="exportType" value="FILE" checked="FILE" title="To file" id="toFile"/>
                                </td>
                            </tr>
                            <tr>
                                <td><stripes:label for="toHomespace">To homespace</stripes:label></td>
                                <td>
                                    <stripes:radio name="exportType" value="HOMESPACE" id="toHomespace"/>
                                </td>
                            </tr>
                        </table>
                        <div id="homespace_data">
                            <table class="formtable">
                                <tr>
                                    <td style="width:120px"><label for="datasetName">Dataset name</label></td>
                                    <td>
                                        <stripes:text name="datasetName" id="datasetName" style="width: 350px;"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td><label for="folder">Folder</label></td>
                                    <td>
                                        <stripes:select name="folder" id="folder" style="width: 355px;">
                                            <c:forEach items="${actionBean.folders}" var="f" varStatus="loop">
                                                <stripes:option value="${f}" label="${crfn:extractFolder(f)}" />
                                            </c:forEach>
                                        </stripes:select>
                                    </td>
                                </tr>
                                <tr>
                                    <td></td>
                                    <td>
                                        <stripes:checkbox name="overwriteDataset" id="overwriteDataset"/>
                                        <stripes:label for="overwriteDataset">Overwrite if file/dataset already exists</stripes:label>
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <table class="formtable">
                            <tr>
                                <td align="right">
                                    <stripes:submit name="execute" value="Execute" id="executeConstruct"/>
                                </td>
                            </tr>
                        </table>
                    </crfn:form>
                </div>
            </c:if>

            <%-- Bookmarked queries dialog --%>

            <div id="bookmarksDialog" title="My bookmarked queries">
                Loading bookmarked queries ...
            </div>

            <%-- Shared bookmarked queries dialog --%>
            <div id="sharedBookmarksDialog" title="Shared bookmarked queries">
                Loading bookmarked queries ...
            </div>

           <%-- project Bookmarked queries dialog --%>
            <div id="projectBookmarksDialog" title="Bookmarks in projects">
                Loading bookmarked queries ...
            </div>

            <%-- The "Useful namespaces" dialog, hidden by default --%>
            <div id="prefixesDialog" title="Useful namespaces">
                <c:if test="${empty actionBean.usefulNamespaces}">
                    <p>None found!</p>
                </c:if>
                <c:if test="${not empty actionBean.usefulNamespaces}">
                    <ul>
                    <c:forEach items="${actionBean.usefulNamespaces}" var="usefulNamespace" varStatus="usefulNamespacesLoop">
                        <li><span id="prefix${usefulNamespacesLoop.index}" class="shadowHover">PREFIX <c:out value="${usefulNamespace.key}"/>: &lt;<c:out value="${usefulNamespace.value}"/>&gt;</span></li>
                    </c:forEach>
                    </ul>

                </c:if>
            </div>

    </stripes:layout-component>
</stripes:layout-render>
