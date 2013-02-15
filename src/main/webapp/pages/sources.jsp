<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvesting Sources">

    <stripes:layout-component name="contents">

            <c:if test='${crfn:userHasPermission(pageContext.session, "/registrations", "u")}'>
                <div id="operations">
                    <ul>
                        <li>
                            <stripes:link href="/source.action" event="add">
                                <c:out value="Add new source"/>
                                <c:if test="${actionBean.type != null && actionBean.type eq 'endpoints'}">
                                    <stripes:param name="harvestSource.sparqlEndpoint" value="true"/>
                                </c:if>
                            </stripes:link>
                        </li>
                    </ul>
                </div>
            </c:if>

            <h1>Harvesting sources</h1>
            <p></p>
            <crfn:form id="filterForm" action="${actionBean.urlBinding}" method="get">
                <div id="searchForm" style="padding-bottom: 5px">
                    <stripes:label for="filterString" class="question">Filter expression</stripes:label>
                    <stripes:text name="searchString" id="filterString" size="40"/>
                    <stripes:hidden name="type" value="${actionBean.type }"/>
                    <stripes:submit name="view" value="Filter" />
                    <c:if test="${!empty actionBean.searchString }">
                        <stripes:link href="${actionBean.urlBinding}?view">
                            <stripes:param name="type" value="${actionBean.type }"/>
                            <img src="${pageContext.request.contextPath}/images/delete_small.gif" title="Remove filter" alt="Remove filter"/>
                        </stripes:link>
                    </c:if>
                </div>
            </crfn:form>
            <div id="tabbedmenu">
                <ul>
                    <c:forEach items="${actionBean.sourceTypes}" var="loopItem">
                        <c:choose>
                            <c:when test="${actionBean.type==loopItem.left}" >
                                <li id="currenttab"><span><c:out value="${loopItem.right}"/></span></li>
                            </c:when>
                            <c:otherwise>
                                <li>
                                    <stripes:link href="${actionBean.urlBinding}">
                                        <c:out value="${loopItem.right}"/>
                                        <stripes:param name="type" value="${loopItem.left}"/>
                                        <stripes:param name="searchString" value="${actionBean.searchString }"/>
                                    </stripes:link>
                                </li>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </ul>
            </div>
            <br style="clear:left" />
            <crfn:form id="resultSetForm" action="${actionBean.urlBinding}" method="post">
                <div style="margin-top:20px;margin-bottom:5px">
                    <stripes:layout-render name="/pages/common/subjectsResultList.jsp" tableClass="sortable"/>
                </div>
                <c:if test='${crfn:userHasPermission(pageContext.session, "/registrations", "u")}'>
                    <div>
                        <stripes:submit name="delete" value="Delete" title="Delete selected sources"/>
                        <stripes:submit name="harvest" value="Schedule urgent harvest" title="Schedule urgent harvest of selected sources"/>
                        <input type="button" name="selectAll" value="Select all" onclick="toggleSelectAll('resultSetForm');return false"/>
                    </div>
                </c:if>
            </crfn:form>
    </stripes:layout-component>
</stripes:layout-render>
