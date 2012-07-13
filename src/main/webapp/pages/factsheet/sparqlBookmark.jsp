<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

    <stripes:layout-component name="contents">

        <cr:tabMenu tabs="${actionBean.tabs}" />

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
                        <stripes:param name="query" value="${actionBean.spqrqlQuery}" />
                        Execute query
                    </stripes:link>
                </div>
            </c:when>
       </c:choose>

    </stripes:layout-component>

</stripes:layout-render>