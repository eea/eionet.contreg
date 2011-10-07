<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Post-harvest scripts">

    <stripes:layout-component name="contents">

    <h1 style="padding-bottom:10px">Post-harvest scripts</h1>

    <c:choose>
        <c:when test="${not empty sessionScope.crUser && sessionScope.crUser.administrator}">

            <div id="tabbedmenu">
                <ul>
                    <c:choose>
                        <c:when test='${actionBean.targetType=="HARVEST_SOURCE"}' >
                            <li id="currenttab"><span>Source-specific</span></li>
                            <li>
                                <stripes:link href="${actionBean.urlBinding}">
                                    <c:out value="Type-specific"/>
                                    <stripes:param name="targetType" value="RDF_TYPE"/>
                                </stripes:link>
                            </li>
                        </c:when>
                        <c:otherwise>
                            <li>
                                <stripes:link href="${actionBean.urlBinding}">
                                    <c:out value="Source-specific"/>
                                    <stripes:param name="targetType" value="HARVEST_SOURCE"/>
                                </stripes:link>
                            </li>
                            <li id="currenttab"><span>Type-specific</span></li>
                        </c:otherwise>
                    </c:choose>
                </ul>
            </div>

			<div id="operations" style="padding-top:10px">
				<ul>
                    <li>
					    <stripes:link href="/admin/postHarvestScript">
	                        <c:out value="Add new script"/>
	                        <stripes:param name="targetType" value="${actionBean.targetType}"/>
	                    </stripes:link>
					</li>
				</ul>
			</div>

            <display:table name="${actionBean.scripts}" class="datatable" id="script" requestURI="${actionBean.urlBinding}">

                <display:column property="uri" title="URL"/>
                <display:column property="numberOfQueries" title="Number of queries"/>
            </display:table>

        </c:when>
        <c:otherwise>
            <div class="error-msg">Access not allowed!</div>
        </c:otherwise>
    </c:choose>
    </stripes:layout-component>

</stripes:layout-render>
