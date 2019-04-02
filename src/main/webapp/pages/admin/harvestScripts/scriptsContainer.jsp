<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvest scripts">

    <stripes:layout-component name="head">

        <script type="text/javascript" src="<c:url value="/scripts/useful_namespaces.js"/>"></script>

        <script type="text/javascript">
            // <![CDATA[
            ( function($) {
                $(document).ready(
                        function(){


                            $("#scriptTypeSelect").change(function() {
                                if($(this).val() !='PUSH') {
                                    $("#serviceSelect").val('');
                                    $("#serviceParamsText").val('');

                                    $("#serviceLabelSpan").hide();
                                    $("#serviceSpan").hide();

                                    $("#phaseLabelSpan").show();
                                    $("#phaseSpan").show();
                                    
                                    
                                } else {
                                    $("#serviceLabelSpan").show();
                                    $("#serviceSpan").show();

                                    $("#phaseLabelSpan").hide();
                                    $("#phaseSpan").hide();

                                }
                                return false;
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

                            // The handling of useful namespace clicks
                            <c:forEach items="${actionBean.usefulNamespaces}" var="usefulNamespace" varStatus="usefulNamespacesLoop">
                            $("#prefix${usefulNamespacesLoop.index}").click(function() {
                                return handlePrefixClick("PREFIX ${usefulNamespace.key}: <${fn:escapeXml(usefulNamespace.value)}>");
                            });
                            </c:forEach>

                        });
            } ) ( jQuery );
            // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

        <h1 style="padding-bottom:10px">Harvest scripts</h1>

        <c:choose>
            <c:when test="${not empty sessionScope.crUser && sessionScope.crUser.administrator}">
                <div id="tabbedmenu">
                    <ul>
                        <c:forEach items="${actionBean.tabs}" var="tab">
                            <li <c:if test="${tab.selected}">id="currenttab"</c:if>>
                                <stripes:link href="${tab.href}" title="${tab.hint}"><c:out value="${tab.title}"/></stripes:link>
                            </li>
                        </c:forEach>
                    </ul>
                </div>

                <stripes:layout-render name="${actionBean.pageToRender}"/>

            </c:when>
            <c:otherwise>
                <div class="error-msg">Access not allowed!</div>
            </c:otherwise>
        </c:choose>
    </stripes:layout-component>

</stripes:layout-render>
