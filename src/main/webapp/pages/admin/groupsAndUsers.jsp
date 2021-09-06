<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>
<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Admin tools">

    <stripes:layout-component name="head">
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">
        <link rel="stylesheet" href="https://cdn.datatables.net/1.10.20/css/jquery.dataTables.min.css">
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
        <script>jQuery.noConflict();</script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>
        <script src="https://code.jquery.com/jquery-1.12.4.js"></script>
        <script src="https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js"></script>
        <script type="text/javascript" src="<c:url value="/scripts/groupsAndUsersTable.js"/>"></script>
        <script>
            jQuery.noConflict();
            jQuery(document).ready(function($){
                jQuery('#myBtn').click(function($){
                    jQuery('#myForm').toggle(500);
                });
            });

            jQuery(document).ready(function($){
                jQuery('#mySecondBtn').click(function($){
                    jQuery('#mySecondForm').toggle(500);
                });
            });
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">
        <table id="groupsAndUsers" class="table border">
            <thead>
            <tr>
                <th>User/Ldap group</th>
                <th>CR Group</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="crGroup" items="${actionBean.crGroups}" varStatus="loop">
                <c:forEach var="member" items="${actionBean.crGroupsAndUsers.get(crGroup)}">
                    <c:url var="removeUser" value="/v2/admintools/removeUser">
                        <c:param name="crGroupName" value="${crGroup}"/>
                        <c:param name="memberName" value="${member}"/>
                    </c:url>
                    <tr>
                        <c:choose>
                            <c:when test='${fn:startsWith(member, "cn=")}'>
                                <td>${member}</td>
                                <td>${crGroup}</td>
                                <td style="cursor: pointer;"><a class="text-info" style="text-decoration: underline"
                                                                href="${removeUser}">Remove</a> / <span
                                        class="details-control text-info"
                                        style="text-decoration: underline">Show users</span></td>
                            </c:when>
                            <c:otherwise>
                                <td>${member}</td>
                                <td>${crGroup}</td>
                                <td style="cursor: pointer;"><a class="text-info" style="text-decoration: underline"
                                                                href="${removeUser}">Remove</a></td>
                            </c:otherwise>
                        </c:choose>
                    </tr>
                </c:forEach>
            </c:forEach>
            </tbody>
        </table>
        <button id="myBtn" class="btn btn-info" style="margin-bottom:5px">Add User</button>
        </br></br>
<%--        <form:form id="myForm" style="display:none" action="${pageContext.request.contextPath}/v2/admintools/addUser"--%>
<%--                   modelAttribute="groupDetails" method="post">--%>
<%--            <form:select path="groupNameOptionOne" items="${crGroups}"/>--%>
<%--            <form:input path="userName" placeholder="Enter user name"/>--%>
<%--            <input type="submit" class="btn btn-info" name="submit" value="Submit"/>--%>
<%--        </form:form>--%>
<%--        <button id="mySecondBtn" class="btn btn-info" style="margin-bottom:5px">Add LDAP Group</button>--%>
<%--        </br></br>--%>
<%--        <form:form id="mySecondForm" style="display:none"--%>
<%--                   action="${pageContext.request.contextPath}/v2/admintools/addUser" modelAttribute="groupDetails"--%>
<%--                   method="post">--%>
<%--            <form:select path="groupNameOptionTwo" items="${crGroups}"/>--%>
<%--            <form:input id="ch" path="ldapGroupName" placeholder="Enter LDAP group name"/>--%>
<%--            <input type="submit" class="btn btn-info" name="submit" value="Submit"/>--%>
<%--        </form:form>--%>

    </stripes:layout-component>
</stripes:layout-render>