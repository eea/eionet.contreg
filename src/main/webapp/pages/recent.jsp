<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Recent uploads">

    <stripes:layout-component name="contents">

        <h1>Recent uploads</h1>

        <p>This page displays the <c:out value="${actionBean.maxResults}"/> most recently added resources of the type denoted by the selected tab.
        If less than <c:out value="${actionBean.maxResults}"/> are displayed, it means the addition time is known only for the resources displayed.
        If none are displayed, it means addition time is known for none.
    </p>

        <div id="tabbedmenu">
            <ul>
                <c:forEach items="${actionBean.types}" var="loopType">
                    <c:choose>
                          <c:when test="${actionBean.type==loopType.uri}" >
                            <li id="currenttab"><span><c:out value="${loopType.title}"/></span></li>
                        </c:when>
                        <c:otherwise>
                            <li>
                                <stripes:link href="/recentUploads.action">
                                    <c:out value="${loopType.title}"/>
                                    <stripes:param name="type" value="${loopType.uri}"/>
                                </stripes:link>
                            </li>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </ul>
        </div>
        <br style="clear:left" />
        <stripes:layout-render name="/pages/common/subjectsResultList.jsp" tableClass="datatable" pageSize="0"/>

    </stripes:layout-component>
</stripes:layout-render>
