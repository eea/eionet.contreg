<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Post-harvest scripts">

    <stripes:layout-component name="contents">

    <h1 style="padding-bottom:10px">Post-harvest scripts</h1>

    <c:choose>
        <c:when test="${not empty sessionScope.crUser && sessionScope.crUser.administrator}">

            <c:if test="${empty actionBean.targetType}">
	            <div id="tabbedmenu">
	                <ul>
	                    <c:forEach items="${actionBean.tabs}" var="tab">
	                        <li <c:if test="${tab.selected}">id="currenttab"</c:if>>
	                            <stripes:link href="${tab.href}" title="${tab.hint}"><c:out value="${tab.title}"/></stripes:link>
	                        </li>
	                    </c:forEach>
	                </ul>
	            </div>
            </c:if>

            <ul id="dropdown-operations">
                <li><a href="#">Operations</a>
                    <ul>
	                    <li>
	                        <stripes:link href="/admin/postHarvestScript" title="${empty actionBean.targetType ? 'Add new all-source script' : 'Add new script to this '}${fn:toLowerCase(actionBean.targetType)}">
	                            <c:out value="Add new script"/>
	                            <c:if test="${not empty actionBean.targetType}">
                                    <stripes:param name="targetType" value="${actionBean.targetType}"/>
                                </c:if>
                                <c:if test="${not empty actionBean.targetUrl}">
                                    <stripes:param name="targetUrl" value="${actionBean.targetUrl}"/>
                                </c:if>
	                        </stripes:link>
	                    </li>
                    </ul>
                </li>
            </ul>

            <c:if test="${not empty actionBean.targetType && not empty actionBean.targetUrl}">
                <div>
                    <span>Displaying scripts of ${fn:toLowerCase(actionBean.targetType)}:</span><br/>
                    <stripes:link href="/factsheet.action" title="Go to the factsheet of this ${fn:toLowerCase(actionBean.targetType)}">
                        <c:out value="${actionBean.targetUrl}"/>
                        <stripes:param name="uri" value="${actionBean.targetUrl}"/>
                    </stripes:link>
                </div>
            </c:if>

            <div style="float:left;width:100%;padding-top:1.2em;">
	            <crfn:form id="scriptsForm" action="/admin/postHarvestScripts" method="post">

	                <display:table name="${actionBean.scripts}" class="datatable" id="script" sort="list" pagesize="20" requestURI="${actionBean.urlBinding}" style="width:80%">

                        <display:setProperty name="basic.msg.empty_list" value="Found no scripts to display. Use operations menu to add one."/>
                        <display:setProperty name="paging.banner.item_name" value="script"/>
                        <display:setProperty name="paging.banner.items_name" value="scripts"/>
                        <display:setProperty name="paging.banner.all_items_found" value='<span class="pagebanner">{0} {1} found.</span>'/>
                        <display:setProperty name="paging.banner.onepage" value=""/>

	                    <display:column style="width:2em;text-align:center">
	                        <input type="checkbox" name="selectedIds" value="${script.id}" title="Select this script"/>
	                    </display:column>
	                    <display:column title='<span title="Title assigned to the script">Title</span>'>
		                    <stripes:link href="/admin/postHarvestScript" title="View, edit and test this script">
		                        <c:out value="${script.title}"/>
		                        <stripes:param name="id" value="${script.id}"/>
		                    </stripes:link>
	                    </display:column>
	                    <display:column style="width:3em;text-align:center" title='<span title="Indicates whether the script is currently turned off">Active</span>'>
	                       <c:out value="${script.active ? 'Yes' : 'No'}"/>
	                    </display:column>
	                </display:table>

                    <c:if test="${not empty actionBean.scripts && fn:length(actionBean.scripts)>0}">
		                <div>
	                        <stripes:submit name="delete" value="Delete" title="Delete selected scripts"/>
	                        <stripes:submit name="activateDeactivate" value="Activate/deactivate" title="Activate/deactivate (i.e. turn on/off) selected scripts"/>
                            <input type="button" onclick="toggleSelectAll('scriptsForm');return false" value="Select all" name="selectAll">
		                </div>
	                </c:if>

                    <c:if test="${not empty actionBean.targetType}">
                        <stripes:hidden name="targetType"/>
                    </c:if>
                    <c:if test="${not empty actionBean.targetUrl}">
                        <stripes:hidden name="targetUrl"/>
                    </c:if>

	            </crfn:form>
            </div>

        </c:when>
        <c:otherwise>
            <div class="error-msg">Access not allowed!</div>
        </c:otherwise>
    </c:choose>
    </stripes:layout-component>

</stripes:layout-render>
