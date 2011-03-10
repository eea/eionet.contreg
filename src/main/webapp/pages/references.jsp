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
                        <li id="currenttab">
                            <span>Resource references</span>
                        </li>
                        <c:if test="${actionBean.uriIsHarvestSource}">
                            <li>
                                <stripes:link href="/objectsInSource.action"  event="search">Objects in Source
                                    <c:choose>
                                        <c:when test="${not empty actionBean.uri}">
                                                <stripes:param name="uri" value="${actionBean.uri}"/>
                                        </c:when>
                                        <c:otherwise>
                                                <stripes:param name="uriHash" value="${actionBean.anonHash}"/>
                                        </c:otherwise>
                                    </c:choose>
                                </stripes:link>
                            </li>
                        </c:if>
                    </ul>
                </div>
                <br style="clear:left" />
                <div style="margin-top:20px">
                    <p>
                        References to
                        <c:choose>
                            <c:when test="${actionBean.anonHash!=0}">
                                <stripes:link href="/factsheet.action">this anonymous resource
                                    <stripes:param name="uriHash" value="${actionBean.anonHash}"/>
                                </stripes:link>
                            </c:when>
                            <c:when test="${actionBean.uriResolvable}">
                                <a class="link-external" href="${fn:escapeXml(actionBean.uri)}"><c:out value="${actionBean.uri}"/></a>
                            </c:when>
                            <c:otherwise>
                                <stripes:link href="/factsheet.action">this unresolvable resource
                                    <stripes:param name="uri" value="${actionBean.uri}"/>
                                </stripes:link>
                            </c:otherwise>
                        </c:choose>
                    </p>
                </div>

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
