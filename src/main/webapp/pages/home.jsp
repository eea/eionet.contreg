<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Home">

    <stripes:layout-component name="contents">
        <c:choose>
            <c:when test="${actionBean.userAuthorized}" >
                <div id="tabbedmenu">
                    <ul>
                        <c:forEach items="${actionBean.tabs}" var="tab">
                            <c:choose>
                                  <c:when test="${actionBean.section == tab.tabType}" >
                                    <li id="currenttab"><span><c:out value="${tab.title}"/></span></li>
                                </c:when>
                                <c:otherwise>
                                    <li>
                                        <stripes:link href="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/${tab.tabType}">
                                            <c:out value="${tab.title}"/>
                                        </stripes:link>
                                    </li>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </ul>
                </div>
                <br style="clear:left" />

                <h1>My ${actionBean.section}</h1>

                <div style="margin-top:10px">
                    <stripes:layout-render name="/pages/home/${actionBean.section}.jsp"/>
                </div>
            </c:when>
            <c:when test="${actionBean.section == 'registrations'}" >
                <h1>User  ${actionBean.attemptedUserName} ${actionBean.section}</h1>
                <div style="margin-top:10px">
                    <stripes:layout-render name="/pages/home/${actionBean.section}.jsp"/>
                </div>
            </c:when>
            <c:otherwise>
                    <div class="error-msg">
                    ${actionBean.authenticationMessage}
                    </div>
            </c:otherwise>
        </c:choose>
    </stripes:layout-component>
</stripes:layout-render>
