<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
            ( function($) {
                $(document).ready(function(){

                    // Open dialog
                    $("#filtersLink").click(function() {
                        $('#filtersDialog').dialog('open');
                        return false;
                    });

                    // Dialog setup
                    $('#filtersDialog').dialog({
                        autoOpen: false,
                        width: 500
                    });

                    // Close dialog
                    $("#closeFiltersDialog").click(function() {
                        $('#filtersDialog').dialog("close");
                        return false;
                    });
                });

             } ) ( jQuery );
             // ]]>
         </script>
     </stripes:layout-component>

    <stripes:layout-component name="contents">

        <cr:tabMenu tabs="${actionBean.tabs}" />

        <c:set var="operationsAvailable" value="${actionBean.isUserDataset}" />

        <br style="clear:left" />
        <br />

        <ul id="dropdown-operations">
            <li><a href="#">Operations</a>
                <ul>
                    <c:if test="${operationsAvailable}">
                        <li>
                            <stripes:link class="link-plain" href="/compiledDataset.action" event="reload">
                                <stripes:param name="uri" value="${actionBean.uri}" />
                                Recompile
                            </stripes:link>
                        </li>
                    </c:if>
                    <li>
                        <stripes:link class="link-plain" href="#" id="filtersLink">
                            Find more deliveries
                        </stripes:link>
                    </li>
                    <li>
                        <stripes:link class="link-plain" href="/sparql">
                            <stripes:param name="default-graph-uri" value="${actionBean.uri}" />
                            SPARQL endpoint
                        </stripes:link>
                    </li>
                </ul>
             </li>
         </ul>

        <h1>Compiled dataset sources</h1>

        <c:if test="${actionBean.currentlyReloaded}">
            <div class="advice-msg">The dataset is being compiled!</div>
        </c:if>

        <crfn:form action="/compiledDataset.action" method="post">
        <stripes:hidden name="uri" value="${actionBean.uri}" />
        <display:table name="${actionBean.sources}" class="sortable" id="item" sort="list" requestURI="${actionBean.urlBinding}">
            <c:if test="${operationsAvailable}">
                <display:column>
                    <stripes:checkbox name="selectedFiles" value="${item.uri}" />
                </display:column>
            </c:if>
            <display:column title="URL" sortable="true">
                <stripes:link href="/factsheet.action">
                    <stripes:param name="uri" value="${item.uri}" />
                    <c:out value="${item.uri}" />
                </stripes:link>
            </display:column>
            <display:column title="Last modified" sortable="true">
                <c:if test="${not empty item.lastModifiedDate}">
                    <fmt:formatDate value="${item.lastModifiedDate}" pattern="yyyy-MM-dd"/>T<fmt:formatDate value="${item.lastModifiedDate}" pattern="HH:mm:ss"/>
                </c:if>
            </display:column>
        </display:table>
        <c:if test="${operationsAvailable && !actionBean.sourcesEmpty}">
            <br />
            <div>
                <stripes:submit name="removeFiles" value="Remove" title="Remove files from dataset"/>
            </div>
        </c:if>
        </crfn:form>

        <div id="filtersDialog" title="Search filters used to compile the dataset">
            <ul>
            <c:forEach var="filter" items="${actionBean.filters}">
                <li>
                    <stripes:link href="/deliverySearch.action" event="datasetFilterSearch">
                        <stripes:param name="obligation" value="${filter.obligation}" />
                        <stripes:param name="locality" value="${filter.locality}" />
                        <stripes:param name="year" value="${filter.year}" />
                        <stripes:param name="datasetFilter" value="true" />
                        <c:out value="${filter.label}" />
                    </stripes:link>
                </li>
            </c:forEach>
            </ul>
            <button id="closeFiltersDialog">Cancel</button>
        </div>

    </stripes:layout-component>

</stripes:layout-render>