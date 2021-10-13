<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>


<%@page import="net.sourceforge.stripes.action.ActionBean"%><stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvesting Statistics">

    <stripes:layout-component name="contents">

    <c:choose>
        <c:when test="${actionBean.adminLoggedIn or actionBean.crAdmin or actionBean.sdsAdmin}">
            <h1>Administration pages</h1>
            <ul>
                <li><stripes:link href="/admin/harvestedurl">Harvested URLs</stripes:link></li>
                <li><stripes:link href="/admin/harveststats">Last 100 harvest statistics</stripes:link></li>
                <li><stripes:link href="/admin/nhus">Next harvest urgency score</stripes:link></li>
                <li><stripes:link href="/admin/sourceBulkActions">Bulk add/delete/check sources</stripes:link></li>
                <li><stripes:link href="/admin/harvestScripts">Harvest scripts</stripes:link></li>
                <li><stripes:link href="/admin/endpointQueries.action">SPARQL endpoint harvest queries</stripes:link></li>
                <li><stripes:link href="/admin/sourceDeletions.action">Monitor source deletions</stripes:link></li>
                <li><stripes:link href="/admin/stagingDbs.action">Staging databases</stripes:link>&nbsp;<span style="color:#FF0000">(work in progress!)</span></li>
                <c:if test="${!actionBean.eeaTemplate}">
                    <li><a href="/v2/admintools/list">Admin tools</a></li>
                </c:if>
            </ul>

            <div style="padding: 11em 0em;position: absolute;color: black">
                Current LDAP admin role used: ${actionBean.userLdapRole}. Members of this LDAP role can access the
                Admin
                options in this application.
            </div>
        </c:when>
        <c:otherwise>
            <div class="error-msg">Access not allowed!</div>
        </c:otherwise>
    </c:choose>
    </stripes:layout-component>

</stripes:layout-render>
