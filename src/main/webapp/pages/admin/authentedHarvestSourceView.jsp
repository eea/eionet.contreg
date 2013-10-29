<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Home">

    <stripes:layout-component name="contents">

    <c:choose>
    <c:when test='${crfn:userHasPermission(pageContext.session, "/registrations", "u")}'>

	    <ul id="dropdown-operations">
	        <li><a href="#">Operations</a>
	            <ul>
	                <li>
	                    <stripes:link href="/source.action" event="editauthenticationdata">
	                        <c:out value="Edit data"/>
	                        <stripes:param name="urlAuthenticationId" value="${actionBean.urlAuthentication.id }"/>
	                    </stripes:link>
	                </li>
	                <li>
	                    <stripes:link href="/source.action" event="deleteauthentedurl" onclick="return confirm(\"Are you sure you want to delete authentication for this url?\"); return false;">
	                        <c:out value="Delete"/>
	                        <stripes:param name="urlAuthenticationId" value="${ actionBean.urlAuthentication.id }"/>
	                    </stripes:link>
	                </li>
	            </ul>
	        </li>
	    </ul>

	    <h1>Authentication details for source</h1>
	    <crfn:form id="filterForm" action="/source.action" method="post">
            <div id="searchForm" style="padding-bottom: 5px">
			    <table class="datatable">
			        <tr>
			            <th>Url starting with</th>
			            <td>${actionBean.urlAuthentication.urlBeginning }</td>
			        </tr>
			        <tr>
			            <th>Username</th>
			            <td>${actionBean.urlAuthentication.username }</td>
			        </tr>
			        <tr>
			            <th>Password</th>
			            <td>${actionBean.urlAuthentication.password }</td>
			        </tr>
                </table>
                <stripes:submit name="authentications" value="Return to list" />
	       </div>
        </crfn:form>

    </c:when>
    <c:otherwise>Access denied</c:otherwise>
    </c:choose>

    </stripes:layout-component>
</stripes:layout-render>