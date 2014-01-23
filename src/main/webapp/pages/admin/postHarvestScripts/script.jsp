<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-definition>

    <ul id="dropdown-operations">
        <li><a href="#">Operations</a>
            <ul>
                <li>
                    <a href="#" id="prefixesLink">Useful namespaces</a>
                </li>
            </ul>
        </li>
    </ul>

    <br/>
    <c:choose>
        <c:when test="${actionBean.id>0}">
            <h4>Edit script:</h4>
        </c:when>

        <c:when test="${actionBean.bulkPaste}">
            <h4>Paste from clipboard to a source:</h4>
        </c:when>

        <c:otherwise>
            <h4>Create script:</h4>
        </c:otherwise>
    </c:choose>

    <c:if test="${not actionBean.bulkPaste}">
        <div class="advice-msg" style="font-size:0.8em">
            Hints:
            <ul>
                <li>
                    in the script, use <span style="font-family:Courier;font-size:1.2em">?${actionBean.harvestedSourceVariable}</span> to denote the harvested source
                    <c:if test="${actionBean.targetType=='TYPE'}"> and <span style="font-family:Courier;font-size:1.2em">?${actionBean.associatedTypeVariable}</span> to denote the associated type</c:if>
                </li>
                <li>
                    the Test button returns the result of a CONSTRUCT query derived from your script
                </li>
                <li>
                    the ${actionBean.targetType=='SOURCE'? 'Target' : 'Test'} source will be used as replacer for ?${actionBean.harvestedSourceVariable} when you run Test
                </li>
                <li>
                    type at least 4 characters for suggestions in the ${actionBean.targetType=='SOURCE'? 'Target' : 'Test'} source.
                </li>
            </ul>
        </div>
    </c:if>

    <c:if test="${actionBean.pastePossible && not actionBean.bulkPaste}">
        <div class="advice-msg" style="font-size:0.8em">
            There are ${fn:length(actionBean.clipBoardScripts)} script(s) in the clipboard:
            <c:forEach items="${actionBean.clipBoardScripts}" var="clipboardItem">

                <stripes:link href="/admin/postHarvestScript">
                    <c:out value="${clipboardItem.title}"/>
                    <stripes:param name="id" value="${actionBean.id}"/>
                    <stripes:param name="clipboardItemId" value="${clipboardItem.id}"/>
                    <stripes:param name="backToTargetUrl" value="${actionBean.backToTargetUrl}"/>
                    <stripes:param name="targetType" value="${actionBean.targetType}"/>
                    <stripes:param name="targetUrl" value="${actionBean.targetUrl}"/>
                </stripes:link><span>; </span>
            </c:forEach>
        </div>
    </c:if>

        <crfn:form action="${actionBean.urlBinding}" focus="first" method="post" style="padding-top:0.8em" onsubmit="formSubmit()">

            <table>
                <c:if test="${not empty actionBean.targetType}">
                    <tr>
                        <td style="vertical-align:top;padding-right:0.3em;text-align:right">
                            <label for="${actionBean.targetType=='SOURCE' ? 'harvestSource' : 'typeSelect'}" class="${empty actionBean.targetUrl ? 'required ' : ''}question">Target ${fn:toLowerCase(actionBean.targetType)}:</label>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${not empty actionBean.targetUrl}">
                                    <stripes:link href="/factsheet.action">
                                        <c:out value="${actionBean.targetUrl}"/>
                                        <stripes:param name="uri" value="${actionBean.targetUrl}"/>
                                    </stripes:link>
                                </c:when>
                                <c:otherwise>
                                    <c:choose>
                                        <c:when test="${actionBean.targetType=='SOURCE'}">
                                            <stripes:text name="targetUrl" id="harvestSource" value="${actionBean.targetUrl}" size="80"/>
                                        </c:when>
                                        <c:otherwise>
                                            <stripes:select name="targetUrl" id="typeSelect">
                                                <stripes:option value="" label="-- select a type --"/>
                                                <c:forEach items="${actionBean.typeUris}" var="typeUri">
                                                    <stripes:option value="${typeUri}" label="${typeUri}"/>
                                                </c:forEach>
                                            </stripes:select>
                                        </c:otherwise>
                                    </c:choose>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:if>

                <c:choose>
                <c:when test="${actionBean.bulkPaste}">
                     <tr>
                     <td colspan="2" style="vertical-align:top;padding-right:0.3em;text-align:left">
                        <display:table name="${actionBean.clipBoardScripts}" class="datatable" id="script" sort="list" pagesize="20" requestURI="${actionBean.urlBinding}" style="width:80%">

                            <display:setProperty name="paging.banner.item_name" value="script"/>
                            <display:setProperty name="paging.banner.items_name" value="scripts"/>
                            <display:setProperty name="paging.banner.all_items_found" value='<div class="pagebanner">{0} {1} found.</div>'/>
                            <display:setProperty name="paging.banner.onepage" value=""/>

                            <display:column  style="text-align:left" title='<span title="Title assigned to the script">Scripts at the clipboard</span>'>
                                <stripes:link href="/admin/postHarvestScript" title="View, edit and test this script">
                                    <c:out value="${script.title}"/>
                                    <stripes:param name="id" value="${script.id}"/>
                                </stripes:link>
                            </display:column>

                        </display:table>
                    </td>
                    </tr>

                </c:when>
                <c:otherwise>
                    <tr>
                        <td style="vertical-align:top;padding-right:0.3em;text-align:right">
                            <label for="titleText" class="required question">Title:</label>
                        </td>
                        <td>
                            <stripes:text name="title" id="titleText" size="80"/>
                        </td>
                    </tr>
                    <tr>
                        <td style="vertical-align:top;padding-right:0.3em;text-align:right">
                            <label for="queryText" class="required question">Script:</label>
                        </td>
                        <td>
                            <div class="expandingArea">
                               <pre><span></span><br /></pre>
                               <stripes:textarea name="script" id="queryText" cols="80" rows="5"/>
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
                        </td>
                    </tr>
                    <tr>
                        <td style="vertical-align:top;padding-right:0.3em;text-align:right">
                            <label for="activeCheckbox" class="question">Active:</label>
                        </td>
                        <td>
                            <stripes:checkbox name="active" id="activeCheckbox"/>
                        </td>
                    </tr>
    <!--                <tr>
                        <td style="vertical-align:top;padding-right:0.3em;text-align:right">
                            <label for="runOnceCheckbox" class="question" title="If set to false, harvester will execute the script until the returned update count is 0.">
                                Run once:
                            </label>
                        </td>
                        <td>
                            <stripes:checkbox name="runOnce" id="runOnceCheckbox"/>
                        </td>
                    </tr> -->
                    <c:if test="${empty actionBean.targetType || actionBean.targetType=='TYPE'}">
                        <tr>
                            <td style="vertical-align:top;padding-right:0.3em;text-align:right">
                                <label for="harvestSource" class="question">Test source:</label>
                            </td>
                            <td>
                                <stripes:text name="testSourceUrl" id="harvestSource" value="${actionBean.testSourceUrl}" size="80"/>
                            </td>
                        </tr>
                    </c:if>

                    </c:otherwise>
                </c:choose>

                <tr>
                    <td>&nbsp;</td>
                    <td>
                        <c:choose>
	                        <c:when test="${actionBean.bulkPaste}">
	                           <stripes:submit name="addFromBulkPaste" value="Add clipboard scripts to target ${fn:toLowerCase(actionBean.targetType)}"/>
	                           <stripes:hidden name="bulkPaste" value="true"/>
	                        </c:when>
	                        <c:otherwise>

		                        <stripes:submit name="save" value="Save"/>
		                        <stripes:submit name="save" value="Save & close"/>
		                        <stripes:submit name="test" value="Test"/>
	                        </c:otherwise>
                        </c:choose>
                        <stripes:submit name="cancel" value="Cancel"/>
                    </td>
                </tr>



                <c:if test="${not empty actionBean.targetType && not empty actionBean.targetUrl}">
                    <tr>
                        <th colspan="2">&nbsp;</th>
                    </tr>
                    <tr>
                        <th colspan="2" style="vertical-align:top;padding-left:0; padding-bottom: 0.5em;text-align:left">Use script template:</th>
                    </tr>

                    <tr>
                        <td style="vertical-align:top;padding-right:0.3em;text-align:right">
                            <label for="scriptTemplateId" title="Linking script." class="question">Linking script</label></td>
                        <td>
                            <stripes:select name="scriptTemplateId" id="scriptTemplateId" value="${actionBean.scriptTemplateId}" style="max-width:100%;">
                                <stripes:options-collection collection="${actionBean.scriptTemplates}" value="id" label="name" />
                            </stripes:select>
                        </td>
                    </tr>
                    <c:choose>
                        <c:when test="${actionBean.targetType=='SOURCE'}">
                            <tr>
                                <td style="vertical-align:top;padding-right:0.3em;text-align:right">
                                    <label for="scriptPredicate" title="Predicate" class="question">Predicate</label>
                                </td>
                                <td>
                                    <stripes:select name="scriptPredicate" id="scriptPredicate">
                                         <stripes:option value="" label=""/>
                                         <c:forEach items="${actionBean.sourceAllDistinctPredicates}" var="scriptPredicate">
                                             <stripes:option value="${scriptPredicate}" label="${scriptPredicate}"/>
                                         </c:forEach>
                                     </stripes:select>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>

                           <tr>
                                <td style="vertical-align:top;padding-right:0.3em;text-align:right">
                                    <label for="scriptPredicate" title="Predicate" class="question">Predicate</label>
                                </td>
                                <td>
                                    <stripes:select name="scriptPredicate" id="scriptPredicate">
                                         <stripes:option value="" label=""/>
                                         <c:forEach items="${actionBean.typeAllDistinctPredicates}" var="scriptPredicate">
                                             <stripes:option value="${scriptPredicate}" label="${scriptPredicate}"/>
                                         </c:forEach>
                                     </stripes:select>
                                </td>
                            </tr>

                        </c:otherwise>
                    </c:choose>
                    <tr>
	                    <td>&nbsp;</td>
	                    <td>
                            <stripes:submit name="useTemplate" value="Update script using template"/>
                        </td>
                    </tr>

                </c:if>

                <c:if test="${not empty actionBean.targetType && empty actionBean.targetUrl}">
                    <tr>
                        <td colspan="2" style="vertical-align:middle;padding-left:0; padding-bottom: 0.5em;padding-top: 1.5em;text-align:left">To use script templates, insert a valid target URL and load predicates: <stripes:submit name="loadTemplatePredicates" value="Load predicates"/></td>
                    </tr>
                </c:if>

            </table>
        <div>
            <stripes:hidden name="id"/>
            <c:if test="${not empty actionBean.targetType}">
                <stripes:hidden name="targetType"/>
            </c:if>
            <c:if test="${not empty actionBean.targetUrl}">
                <stripes:hidden name="targetUrl"/>
            </c:if>
            <c:if test="${not empty actionBean.backToTargetUrl}">
                <stripes:hidden name="backToTargetUrl"/>
            </c:if>
            <c:if test="${not empty actionBean.cancelUrl}">
                <stripes:hidden name="cancelUrl"/>
            </c:if>
            <input type="hidden" name="ignoreMalformedSparql" value="${actionBean.ignoreMalformedSparql}"/>
        </div>
        </crfn:form>

        <c:if test="${not empty param.test && not empty actionBean.executedTestQuery}">

            <div>
                Test query executed:
                <pre style="font-size:0.7em"><c:out value="${actionBean.executedTestQuery}"/></pre>
            </div>
            <c:if test="${not empty actionBean.testError}">
                <div>
                Received test error:
                <pre style="font-size:0.7em"><c:out value="${actionBean.testError}"/></pre>
                </div>
            </c:if>
            <c:if test="${not empty actionBean.testResults}">
                <display:table name="${actionBean.testResults}" class="datatable" id="testResultRow" sort="list" pagesize="15" requestURI="${actionBean.urlBinding}" style="width:80%">

                    <display:setProperty name="paging.banner.item_name" value="row"/>
                    <display:setProperty name="paging.banner.items_name" value="rows"/>
                    <display:setProperty name="paging.banner.all_items_found" value='<span class="pagebanner">{0} {1} found.</span>'/>
                    <display:setProperty name="paging.banner.onepage" value=""/>

                    <c:forEach items="${actionBean.testResultColumns}" var="testResultColumn">
                        <c:set var="columnValue" value="${testResultRow[testResultColumn]}"/>
                        <display:column title="${testResultColumn}">
                            <c:choose>
                                <c:when test="${not empty columnValue && columnValue.literal==false}">
                                    <stripes:link href="/factsheet.action">
                                        <c:out value="${columnValue}"/>
                                        <stripes:param name="uri" value="${columnValue}"/>
                                    </stripes:link>
                                </c:when>
                                <c:otherwise>
                                    <c:out value="${columnValue}"/>
                                </c:otherwise>
                            </c:choose>
                        </display:column>
                    </c:forEach>

                </display:table>
            </c:if>
            <c:if test="${empty actionBean.testError && empty actionBean.testResults}">
                <div>No test results found!</div>
            </c:if>
        </c:if>

    <%-- The "Useful namesoaces" dialog, hidden by default --%>

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
        <button id="closePrefixesDialog">Close</button>

    </div>

</stripes:layout-definition>
