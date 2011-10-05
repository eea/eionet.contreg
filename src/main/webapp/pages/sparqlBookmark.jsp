<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

    <stripes:layout-component name="contents">

        <div id="tabbedmenu">
            <ul>
            <li>
                <stripes:link href="/factsheet.action">Bookmarked SPARQL
                    <stripes:param name="uri" value="${actionBean.uri}"/>
                </stripes:link>
            </li>
            <li>
                <c:choose>
                    <c:when test="${not empty actionBean.subject && not empty actionBean.subject.uri && !actionBean.subject.anonymous}">
                        <stripes:link href="/references.action" event="search">Resource references
                            <stripes:param name="uri" value="${actionBean.subject.uri}"/>
                        </stripes:link>
                    </c:when>
                    <c:when test="${not empty actionBean.uri}">
                        <stripes:link href="/references.action" event="search">Resource references
                            <stripes:param name="uri" value="${actionBean.uri}"/>
                        </stripes:link>
                    </c:when>
                    <c:otherwise>
                        <stripes:link href="/references.action" event="search">Resource references
                            <stripes:param name="anonHash" value="${actionBean.uriHash}"/>
                        </stripes:link>
                    </c:otherwise>
                </c:choose>
            </li>
            <c:if test="${actionBean.uriIsHarvestSource}">
                <li>
                    <stripes:link href="/objectsInSource.action" event="search">Objects in Source
                    <c:choose>
                        <c:when test="${not empty actionBean.subject && not empty actionBean.subject.uri && !actionBean.subject.anonymous}">
                            <stripes:param name="uri" value="${actionBean.subject.uri}"/>
                        </c:when>
                        <c:when test="${not empty actionBean.uri}">
                            <stripes:param name="uri" value="${actionBean.uri}"/>
                        </c:when>
                        <c:otherwise>
                            <stripes:param name="anonHash" value="${actionBean.uriHash}"/>
                        </c:otherwise>
                    </c:choose>
                    </stripes:link>
                </li>
            </c:if>
            <c:if test="${actionBean.mapDisplayable}">
                   <li>
                    <stripes:link class="link-plain" href="/factsheet.action" event="showOnMap">Show on Map
                    <stripes:param name="uri" value="${actionBean.subject.uri}"/>
                    <stripes:param name="latitude" value="${actionBean.latitude}" />
                    <stripes:param name="longitude" value="${actionBean.longitude}" />
                    </stripes:link>
                   </li>
            </c:if>
            <c:if test="${actionBean.sparqlBookmarkType}">
                <li id="currenttab"><span>Resource properties</span></li>
            </c:if>
            </ul>
        </div>
        <br style="clear:left" />

        <c:choose>
            <c:when test="${actionBean.subject!=null}">
                <c:set var="subjectUrl" value="${actionBean.subject.url}"/>

                <p>Resource URL: <a class="link-external" href="${fn:escapeXml(subjectUrl)}"><c:out value="${subjectUrl}"/></a>
                    <c:if test ="${actionBean.subjectIsUserBookmark}">(Bookmarked)</c:if>
                </p>

                <div class="databox">
                    <span style="white-space: pre-wrap;"><c:out value="${actionBean.spqrqlQuery}" /></span>
                </div>
                <br />
                <div align="right">
                    <stripes:link beanclass="eionet.cr.web.action.SPARQLEndpointActionBean" event="execute">
                        <stripes:param name="format" value="text/html" />
                        <stripes:param name="nrOfHits" value="20" />
                        <stripes:param name="query" value="${actionBean.spqrqlQuery}" />
                        Execute query
                    </stripes:link>
                </div>
            </c:when>
       </c:choose>

    </stripes:layout-component>

</stripes:layout-render>