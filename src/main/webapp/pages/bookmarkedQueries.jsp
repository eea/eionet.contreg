<%@page contentType="text/html;charset=UTF-8"%>
<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Delete bookmarked SPARQL queries">

    <stripes:layout-component name="contents">

        <h1>Delete bookmarked SPARQL queries</h1>

        <c:if test="${not empty actionBean.bookmarkedQueries}">
            <div style="margin-top: 15px">
                <crfn:form id="bookmarkedQueriesForm" action="/sparql" method="post">
                    <div>
                    <stripes:submit name="deleteBookmarked" value="Delete" title="Delete the bookmarked queries that you have selected below"/>
                    <input type="button" name="selectAll" value="Select all" onclick="toggleSelectAll('bookmarkedQueriesForm');return false"/>
                    </div>
                    <table>
                        <c:forEach items="${actionBean.bookmarkedQueries}" var="bookmarkedQuery">
                            <tr>
                                <td>
                                    <stripes:checkbox value="${bookmarkedQuery.subj}" name="deleteQueries"/>
                                </td>
                                <td>
                                    <ul style="list-style:none">
                                        <li style="font-weight:bold"><c:out value="${bookmarkedQuery.label}"/></li>
                                        <li style="font-size:0.8em"><c:out value="${bookmarkedQuery.queryString}"/></li>
                                    </ul>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </crfn:form>
            </div>
        </c:if>
        <c:if test="${empty actionBean.bookmarkedQueries}">
            <div class="note-msg">No bookmarked queries currently found!</div>
        </c:if>
    </stripes:layout-component>
</stripes:layout-render>
