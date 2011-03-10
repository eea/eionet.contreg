<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource references">

    <stripes:layout-component name="contents">

        <c:choose>
            <c:when test="${!actionBean.noCriteria}">

                <div id="tabbedmenu">
                    <ul>
                        <li>
                            <c:choose>
                                <c:when test="${not empty actionBean.uri}">
                                    <stripes:link href="/factsheet.action">Resource properties
                                        <stripes:param name="uri" value="${actionBean.uri}"/>
                                    </stripes:link>
                                </c:when>
                                <c:otherwise>
                                    <stripes:link href="/factsheet.action">Resource properties
                                        <stripes:param name="uriHash" value="${actionBean.anonHash}"/>
                                    </stripes:link>
                                </c:otherwise>
                            </c:choose>
                        </li>
                        <li>
                            <c:choose>
                                <c:when test="${not empty actionBean.uri}">
                                    <stripes:link href="/references.action"  event="search">Resource references
                                        <stripes:param name="uri" value="${actionBean.uri}"/>
                                    </stripes:link>
                                </c:when>
                                <c:otherwise>
                                    <stripes:link href="/references.action"  event="search">Resource references
                                        <stripes:param name="uriHash" value="${actionBean.anonHash}"/>
                                    </stripes:link>
                                </c:otherwise>
                            </c:choose>
                        </li>
                        <li id="currenttab">
                            <span>Objects in Source</span>
                        </li>
                    </ul>
                </div>
                <br style="clear:left" />

                <c:if test="${param.search!=null}">
                    <stripes:layout-render name="/pages/common/subjectsResultList.jsp" tableClass="sortable"/>
                </c:if>

            </c:when>
            <c:otherwise>
                <div>&nbsp;</div>
            </c:otherwise>
        </c:choose>

    </stripes:layout-component>
</stripes:layout-render>
