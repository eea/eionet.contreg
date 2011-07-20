<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>


<%@page import="net.sourceforge.stripes.action.ActionBean"%><stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvesting Statistics">

    <stripes:layout-component name="contents">

    <c:choose>
        <c:when test="${actionBean.adminLoggedIn}">
            <h1>Administration pages</h1>
            <ul>
            <li><stripes:link href="/admin/harvestedurl">Harvested Urls</stripes:link></li>
            <li><stripes:link href="/admin/nhus">Next Harvest Urgency Score </stripes:link></li>
            <li><stripes:link href="/admin/bulkharvest">Bulk Add/Delete Sources</stripes:link></li>
            </ul>
        </c:when>
        <c:otherwise>
            <div class="error-msg">
            No Access
            </div>
        </c:otherwise>
    </c:choose>
    </stripes:layout-component>

</stripes:layout-render>
