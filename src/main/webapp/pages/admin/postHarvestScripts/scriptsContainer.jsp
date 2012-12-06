<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Post-harvest scripts">

    <stripes:layout-component name="contents">

    <h1 style="padding-bottom:10px">Post-harvest scripts</h1>

    <c:choose>
        <c:when test="${not empty sessionScope.crUser && sessionScope.crUser.administrator}">
        <c:if test="${actionBean.pastePossible}">
            <div class="tip-msg">
                There are ${fn:length(actionBean.clipBoardScripts)} script(s) in the clipboard.
            </div>
        </c:if>

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
