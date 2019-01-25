<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Home">

    <stripes:layout-component name="contents">


    <c:choose>
    <c:when test='${crfn:userHasPermission(pageContext.session, "/registrations", "u")}'>

	    <c:choose>
	    <c:when test="${ actionBean.urlAuthentication.id > 0}">
	       <h1>Edit authentication details for source</h1>
	    </c:when>
	    <c:otherwise>
	       <h1>Add authentication details for source</h1>
	    </c:otherwise>
	    </c:choose>

	    <crfn:form id="filterForm" action="/source.action" method="post">
	        <div id="searchForm" style="padding-bottom: 5px">
	            <table>
	                <tr>
	                    <td><stripes:label for="urlStarting" class="question">Url starting with</stripes:label></td>
	                    <td><stripes:text name="urlAuthentication.urlBeginning" id="urlStarting" size="40" value="${actionBean.urlAuthentication.urlBeginning }"/></td>
	                </tr>

	                <tr>
	                    <td><stripes:label for="username" class="question">Username</stripes:label></td>
	                    <td><stripes:text name="urlAuthentication.username" id="username" size="20" value="${actionBean.urlAuthentication.username }"/></td>
	                </tr>

	                <tr>
	                    <td><stripes:label for="password" class="question">Password</stripes:label></td>
	                    <td><stripes:text name="urlAuthentication.password" id="password" size="20" value="${actionBean.urlAuthentication.password }"/></td>
	                </tr>

	                <tr>
	                    <td><stripes:hidden name="urlAuthentication.id" value="${actionBean.urlAuthentication.id }"/></td>
	                    <td>
	                    <c:choose>
                            <c:when test="${ actionBean.urlAuthentication.id > 0}">
                                <stripes:hidden name="urlAuthenticationId" value="${actionBean.urlAuthentication.id }"/>
                               <stripes:submit name="viewauthenticationdata" value="Cancel" />
                            </c:when>
                            <c:otherwise>
                               <stripes:submit name="authentications" value="Cancel" />
                            </c:otherwise>
                        </c:choose>


	                    <stripes:submit name="saveauthentedurl" value="Save" /></td>
	                </tr>
	            </table>
	        </div>
	    </crfn:form>




    </c:when>
    <c:otherwise>Access denied</c:otherwise>
    </c:choose>

    </stripes:layout-component>
</stripes:layout-render>