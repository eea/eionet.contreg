<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-definition>
    <c:choose>
        <c:when test="${not empty actionBean.harvests}">
            <table class="datatable">
                <caption>Last 10 harvests:</caption>
                <thead>
                    <tr>
                        <th scope="col">Type</th>
                        <th scope="col">User</th>
                        <th scope="col">Started</th>
                        <th scope="col">Finished</th>
                        <th scope="col">Triples</th>
                        <th scope="col">Subjects</th>
                        <th scope="col"></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${actionBean.harvests}" var="harv"
                        varStatus="loop">
                        <tr>
                            <td><c:out value="${harv.harvestType}" /></td>
                            <td><c:out value="${harv.user}" /></td>
                            <td><fmt:formatDate value="${harv.datetimeStarted}"
                                pattern="dd-MM-yy HH:mm:ss" /></td>
                            <td><fmt:formatDate value="${harv.datetimeFinished}"
                                pattern="dd-MM-yy HH:mm:ss" /></td>
                            <td><c:out value="${harv.totalStatements}" /></td>
                            <td><c:out value="${harv.totalResources}" /></td>
                            <td><stripes:link href="/harvest.action">
                                <img src="${pageContext.request.contextPath}/images/view2.gif"
                                    title="View" alt="View" />
                                <c:if
                                    test="${(!(empty harv.hasFatals) && harv.hasFatals) || (!(empty harv.hasErrors) && harv.hasErrors)}">
                                    <img src="${pageContext.request.contextPath}/images/error.png"
                                        title="Errors" alt="Errors" />
                                </c:if>
                                <c:if test="${!(empty harv.hasWarnings) && harv.hasWarnings}">
                                    <img
                                        src="${pageContext.request.contextPath}/images/warning.png"
                                        title="Warnings" alt="Warnings" />
                                </c:if>
                                <stripes:param name="harvestDTO.harvestId"
                                    value="${harv.harvestId}" />
                            </stripes:link></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <div class="important-msg">No history found!</div>
        </c:otherwise>
    </c:choose>

</stripes:layout-definition>
