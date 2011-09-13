<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="${initParam.appDispName}">

    <stripes:layout-component name="contents">
<div style="float:right; background-color:white; width: 20em; padding-left: 1em;">
    <div class="action-box">
        <c:choose>
            <c:when test='${crfn:userHasPermission(pageContext.session, "/registrations", "u")}'>
                <stripes:link href="/registerUrl.action" style="font-weight:bold">Suggest a URL!</stripes:link>
            </c:when>
            <c:otherwise>
                <stripes:link title="Login" href="/login.action" event="login" style="font-weight:bold">Login to suggest a URL!</stripes:link>
            </c:otherwise>
        </c:choose>
        Help other researchers find the good datasets. Bookmark them on this site.
        <p>
    Now you can use a
    <a href="http://en.wikipedia.org/wiki/Bookmarklet" target="new">bookmarklet</a>
    to simplify the process of adding resources to the system.
    Follow this link to <a href="quickAddBookmark.action?installation">add the bookmarklet to your browser</a>.
    </p>
    </div>
    <div class="action-box">
        <stripes:layout-render name="/pages/tagcloud.jsp" />
    </div>
    <div class="action-box">
        <h2>Recently discovered files</h2>
        <c:choose>
            <c:when test="${empty actionBean.recentFiles}">
                <p class="system-msg">No recently discovered files found</p>
            </c:when>
            <c:otherwise>
                <ul class="menu">
                <c:forEach items="${actionBean.recentFiles}" var="recentFile">
                        <li>
                            <a href="factsheet.action?uri=${recentFile.left }" title="${recentFile.right }">
                            <c:choose>
                                <c:when test="${fn:length(recentFile.right) gt 38}">
                                    ${fn:substring(recentFile.right,0,35)}...
                                </c:when>
                                <c:otherwise>
                                    ${recentFile.right}
                                </c:otherwise>
                            </c:choose>
                            </a>
                        </li>
                </c:forEach>
                </ul>
            </c:otherwise>
        </c:choose>
    </div>
</div>
        <h1>What is ${initParam.appDispName}?</h1>
    <p>
        ${initParam.appDispName} is an object-oriented search engine where you can search for the content of data in Eionet.
        Being object-oriented means it understands what e.g. a measuring station is and can show what measurements
        it has made.
        Not all of the Eionet services are included, only those that have been specified by the administrators of this site.
    </p>
    <h2>Simple search</h2>
    <p>
        To quickly find content by text in any metadata element, you can start right here by using the text input below.
    </p>

    <crfn:form name="searchForm" action="/simpleSearch.action" method="get" focus="searchExpression" style="padding-bottom:20px">
        <table class="formtable">
            <tr>
            <td>
                <stripes:label for="expressionText">Expression:</stripes:label>
            </td>
            <td>
                <stripes:text name="searchExpression" id="expressionText" size="40"/>
            </td>
            <td>
                <stripes:submit name="search" value="Search" id="searchButton"/>
            </td>
            </tr>
            <tr>
            <td colspan="3">
                <stripes:radio id="anyObject" name="simpleFilter" value="anyObject" checked="anyObject" title="Any Object"/>
                <stripes:label for="anyObject">Full text search</stripes:label>

                <stripes:radio id="exactMatch" name="simpleFilter" value="exactMatch"/>
                <stripes:label for="exactMatch">Exact match</stripes:label>
            </td>
            </tr>
        </table>
        <stripes:text name="dummy" style="visibility:hidden;display:none" disabled="disabled" size="1"/>
    </crfn:form>

    <h2>Further searches</h2>
    <p>
        To the right you'll see two pre-cooked searches. The most popular tags used on resources and a list of
        recently discovered files.
        On the left of the screen you can choose between different search cases:
    </p>
    <ul>
        <li>
            <a href="simpleSearch.action">Simple search</a><br/>
            is the same quick-find search that is also displayed above.
        </li>
        <li>
            <a href="deliverySearch.action">Search Reportnet deliveries</a><br/>
            is meant for dataflow managers to observe specific dataflows in the dimensions of country and year.
        </li>
        <li>
            <a href="customSearch.action">Custom search</a><br/>
            enables you to choose the criteria you want to search by and offers picklists of existing values too.
        </li>
        <li>
            <a href="typeSearch.action">Type search</a><br/>
            finds all objects of the same type.
        </li>
        <li>
            <a href="recentUploads.action">Recent uploads</a><br/>
            displays the latest content that CR has discovered, classified by certain content types.
        </li>
        </ul>

        <h2>Support</h2>
        <p>
        If you experience any problem using ${initParam.appDispName}, please let the Eionet Helpdesk know immediately.
        The Helpdesk can be reached by phone on +37 2 508 4992 from Monday through Friday 9:00 to 17:00 CET.
        You can also email the helpdesk at any time: <a href="mailto:helpdesk@eionet.europa.eu">helpdesk@eionet.europa.eu</a>.
        Do not hesitate, we are here to help.
        </p>

    </stripes:layout-component>
</stripes:layout-render>
