<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource references">

    <stripes:layout-component name="contents">

        <c:choose>
            <c:when test="${!actionBean.noCriteria}">

                <cr:tabMenu tabs="${actionBean.tabs}" />

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
