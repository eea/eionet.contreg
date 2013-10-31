<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Home">

    <stripes:layout-component name="contents">
    <c:choose>
    <c:when test='${crfn:userHasPermission(pageContext.session, "/registrations", "u")}'>

        <h1>Harvest source authentication data</h1>
         <ul id="dropdown-operations">
             <li><a href="#">Operations</a>
                 <ul>
                     <li>
                         <stripes:link href="/source.action" event="editauthenticationdata">
                             <c:out value="Add new login data"/>
                         </stripes:link>
                     </li>
                     <li>
                         <stripes:link href="/sources.action" event="search">
                             <c:out value="Return to harvest sources"/>
                         </stripes:link>
                     </li>
                 </ul>
             </li>
         </ul>

        <c:if test="${not empty actionBean.urlAuthentications}">
            <div style="margin-top:20px">
                <crfn:form id="urlAuthenticationsForm" beanclass="${actionBean.class.name}" method="post">
                    <display:table name="${actionBean.urlAuthentications}" class="datatable" id="authentication" sort="list" pagesize="20" requestURI="${actionBean.urlBinding}" style="width:80%">

                        <display:setProperty name="paging.banner.item_name" value="Url authentication"/>
                        <display:setProperty name="paging.banner.items_name" value="Url authentications"/>
                        <display:setProperty name="paging.banner.all_items_found" value='<div class="pagebanner">{0} {1} found.</div>'/>
                        <display:setProperty name="paging.banner.onepage" value=""/>

                        <display:column style="width:2em;text-align:center">
                            <input type="checkbox" name="selectedUrlAuthenticationIds" value="${authentication.id}" title="Select this authentication"/>
                        </display:column>

                        <display:column style="width:12em;text-align:left" title='<span title="Url starts with">Url starts with</span>'>
                            <stripes:link href="/source.action" event="viewauthenticationdata">
                                <c:out value="${ authentication.urlBeginning }"/>
                                <stripes:param name="urlAuthenticationId" value="${authentication.id }"/>
                            </stripes:link>
                        </display:column>

                        <display:column style="width:12em;text-align:left" title='<span title="Username">Username</span>'>
                            <c:out value="${authentication.username}"/>
                        </display:column>

                        <display:column style="width:12em;text-align:left" title='<span title="Password">Password</span>'>
                            <c:out value="${authentication.password}"/>
                        </display:column>
                    </display:table>

                    <div>
                        <stripes:submit name="deleteSelectedUrlAuthentications" onclick="confirm('Are you sure you want to delete the selected url authentications?');" value="Delete" title="Delete selected url authentications"/>
                        <input type="button" onclick="toggleSelectAll('urlAuthenticationsForm');return false" value="Select all" name="selectAll"/>
                    </div>

                </crfn:form>
            </div>
        </c:if>



    </c:when>
    <c:otherwise>Access denied</c:otherwise>
    </c:choose>

    </stripes:layout-component>
</stripes:layout-render>
