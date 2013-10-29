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
                 </ul>
             </li>
         </ul>

        <c:if test="${ actionBean.urlAuthentications!=null && fn:length(actionBean.urlAuthentications)>0 }">

        <table id="urlAuthenticationsList" class="datatable">
		<thead>
		    <tr>
			    <th scope="col">Url starts with</th>
			    <th scope="col">Username</th>
			    <th scope="col">Password</th>
		    </tr>
		</thead>
		<tbody>
		    <c:forEach items="${actionBean.urlAuthentications}" var="resultListItem" varStatus="rowStatus">
		    <tr
		        <c:choose>
		            <c:when test="${rowStatus.count%2 != 0}">
		                 class="odd"
		            </c:when>
		            <c:otherwise>
		                class="even"
		            </c:otherwise>
		        </c:choose>>

		        <td>
                    <stripes:link href="/source.action" event="viewauthenticationdata">
                        <c:out value="${ resultListItem.urlBeginning }"/>
                        <stripes:param name="urlAuthenticationId" value="${resultListItem.id }"/>
                    </stripes:link>
		        </td>
		        <td>${ resultListItem.username }</td>
		        <td>${ resultListItem.password }</td>
		    </tr>
		</c:forEach>
		    </tbody>
		</table>

        </c:if>


    </c:when>
    <c:otherwise>Access denied</c:otherwise>
    </c:choose>

    </stripes:layout-component>
</stripes:layout-render>
