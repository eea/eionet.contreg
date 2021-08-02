<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Custom search">

    <stripes:layout-component name="contents">

        <div id="workarea">
            <table id="groupsAndUsers" class="table border">
                <thead>
                <tr>
                    <th>User/Ldap group</th>
                    <th>DD Group</th>
                    <th>Action</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="ddGroup" items="${ddGroups}" varStatus="loop">
                    <c:forEach var="member" items="${ddGroupsAndUsers.get(ddGroup)}">
                        <c:url var="removeUser" value="/v2/admintools/removeUser">
                            <c:param name="ddGroupName" value="${ddGroup}" />
                            <c:param name="memberName" value="${member}" />
                        </c:url>
                        <tr>
                            <c:choose>
                                <c:when test='${fn:startsWith(member, "cn=")}'>
                                    <td>${member}</td>
                                    <td>${ddGroup}</td>
                                    <td style="cursor: pointer;"><a class="text-info" style="text-decoration: underline" href="${removeUser}">Remove</a> / <span class="details-control text-info" style="text-decoration: underline">Show users</span></td>
                                </c:when>
                                <c:otherwise>
                                    <td>${member}</td>
                                    <td>${ddGroup}</td>
                                    <td style="cursor: pointer;"><a class="text-info" style="text-decoration: underline" href="${removeUser}">Remove</a></td>
                                </c:otherwise>
                            </c:choose>
                        </tr>
                    </c:forEach>
                </c:forEach>
                </tbody>
            </table>
            <button id="myBtn" class="btn btn-info" style="margin-bottom:5px">Add User</button></br></br>
            <form:form id="myForm" style="display:none" action="${pageContext.request.contextPath}/v2/admintools/addUser" modelAttribute="groupDetails" method="post">
                <form:select path="groupNameOptionOne" items="${ddGroups}"/>
                <form:input path="userName" placeholder="Enter user name"/>
                <input type="submit" class="btn btn-info" name="submit" value="Submit"/>
            </form:form>
            <button id="mySecondBtn" class="btn btn-info" style="margin-bottom:5px">Add LDAP Group</button></br></br>
            <form:form id="mySecondForm" style="display:none" action="${pageContext.request.contextPath}/v2/admintools/addUser" modelAttribute="groupDetails" method="post">
                <form:select path="groupNameOptionTwo" items="${ddGroups}"/>
                <form:input id="ch" path="ldapGroupName" placeholder="Enter LDAP group name"/>
                <input type="submit" class="btn btn-info" name="submit" value="Submit"/>
            </form:form>
        </div> <!-- workarea -->

    </stripes:layout-component>
</stripes:layout-render>