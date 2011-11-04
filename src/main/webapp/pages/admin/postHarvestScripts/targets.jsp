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
                    <c:forEach items="${actionBean.tabs}" var="tab">
                        <li <c:if test="${tab.selected}">id="currenttab"</c:if>>
                            <stripes:link href="${tab.href}" title="${tab.hint}"><c:out value="${tab.title}"/></stripes:link>
                        </li>
                    </c:forEach>
                </ul>
            </div>

            <ul id="dropdown-operations">
                <li><a href="#">Operations</a>
                    <ul>
                        <li>
                            <stripes:link href="/admin/postHarvestScript" title="Add new ${fn:toLowerCase(actionBean.targetType)}-specific script">
                                <c:out value="Add new script"/>
                                <c:if test="${not empty actionBean.targetType}">
                                    <stripes:param name="targetType" value="${actionBean.targetType}"/>
                                </c:if>
                            </stripes:link>
                        </li>
                    </ul>
                </li>
            </ul>

            <div style="float:left;width:100%;padding-top:1.2em;">

	            <display:table name="${actionBean.targets}" class="datatable" id="target" sort="list" pagesize="20" requestURI="${actionBean.urlBinding}" style="width:80%">

	                <display:setProperty name="basic.msg.empty_list" value="Found no ${fn:toLowerCase(actionBean.targetType)}-specific scripts. Use operations menu to add one."/>
	                <display:setProperty name="paging.banner.item_name" value="${fn:toLowerCase(actionBean.targetType)} with scripts"/>
	                <display:setProperty name="paging.banner.items_name" value="${fn:toLowerCase(actionBean.targetType)}s with scripts"/>
	                <display:setProperty name="paging.banner.all_items_found" value='<span class="pagebanner">{0} {1} found.</span>'/>
	                <display:setProperty name="paging.banner.onepage" value=""/>

	                <display:column title="${actionBean.targetType=='SOURCE' ? 'Source' : 'Type'}">
	                    <stripes:link href="/admin/postHarvestScripts">
	                        <c:out value="${target.left}"/>
	                        <stripes:param name="targetType" value="${actionBean.targetType}"/>
	                        <stripes:param name="targetUrl" value="${target.left}"/>
	                    </stripes:link>
	                </display:column>
	                <display:column property="right" title="Number of scripts" style="width:9em;text-align:center"/>
	            </display:table>
            </div>

        </c:when>
        <c:otherwise>
            <div class="error-msg">Access not allowed!</div>
        </c:otherwise>
    </c:choose>
    </stripes:layout-component>

</stripes:layout-render>
