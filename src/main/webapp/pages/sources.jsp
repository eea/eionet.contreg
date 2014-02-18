<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvesting Sources">

    <stripes:layout-component name="contents">

            <c:if test='${crfn:userHasPermission(pageContext.session, "/registrations", "u")}'>
                <ul id="dropdown-operations">
                    <li><a href="#">Operations</a>
                        <ul>
                            <li>
                                <stripes:link href="/source.action" event="add">
                                    <c:out value="Add new source"/>
                                    <c:if test="${actionBean.type != null && actionBean.type eq 'ENDPOINT'}">
                                        <stripes:param name="harvestSource.sparqlEndpoint" value="true"/>
                                    </c:if>
                                </stripes:link>
                            </li>
                            <li>
                                <stripes:link href="/source.action" event="authentications">
                                    <c:out value="Manage login data"/>
                                </stripes:link>
                            </li>
                        </ul>
                    </li>
                </ul>
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
                    <c:forEach items="${actionBean.sourceTypes}" var="sourceType">
                        <c:choose>
                            <c:when test="${actionBean.type eq sourceType}" >
                                <li id="currenttab"><span><c:out value="${sourceType.title}"/></span></li>
                            </c:when>
                            <c:otherwise>
                                <li>
                                    <stripes:link beanclass="${actionBean.class.name}">
                                        <c:out value="${sourceType.title}"/>
                                        <stripes:param name="type" value="${sourceType}"/>
                                        <stripes:param name="searchString" value="${actionBean.searchString}"/>
                                    </stripes:link>
                                </li>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </ul>
            </div>
            <br style="clear:left" />

            <c:if test="${actionBean.paginatedList != null && not empty actionBean.paginatedList.list}">

                <c:set var="userCanUpdate" value="${actionBean.userAllowedUpdates}"/>

                <crfn:form id="resultSetForm" action="${actionBean.urlBinding}" method="post">

                    <div style="margin-top:20px;margin-bottom:5px">
                       <display:table name="${actionBean.paginatedList}" id="harvestSource" class="sortable" sort="external" pagesize="${actionBean.resultListPageSize}" requestURI="${actionBean.urlBinding}" style="width:100%">

                            <c:if test="${userCanUpdate}">
                                <display:column style="width:5%">
                                    <stripes:checkbox name="sourceUrl" value="${harvestSource.url}" /><c:if test="${harvestSource.prioritySource}"><img src="${pageContext.request.contextPath}/images/light_red_flag_10x10.png" title="This is a priority source!" title="This is a priority source!"/></c:if>
                                </display:column>
                            </c:if>
                            <display:column title="URL" style="width:80%" sortable="true" sortProperty="URL">
                                <stripes:link beanclass="${actionBean.harvestSourceBeanClass.name}" event="view" title="${fn:escapeXml(harvestSource.url)}">
                                    <stripes:param name="harvestSource.url" value="${harvestSource.url}"/>
                                    <c:out value="${crfn:cutAtFirstLongToken(harvestSource.url, 100)}"/>
                                </stripes:link>
                            </display:column>
                            <display:column title="Last harvest" style="width:15%" sortable="true" sortProperty="LAST_HARVEST">
                                <fmt:formatDate value="${harvestSource.lastHarvest}" pattern="dd.MM.yy HH:mm:ss" />
                            </display:column>

                       </display:table>
                    </div>

                    <c:if test='${crfn:userHasPermission(pageContext.session, "/registrations", "u")}'>
                        <div>
                            <stripes:submit name="delete" value="Delete" title="Delete selected sources"/>
                            <stripes:submit name="harvest" value="Schedule urgent harvest" title="Schedule urgent harvest of selected sources"/>
                            <input type="button" name="selectAll" value="Select all" onclick="toggleSelectAll('resultSetForm');return false"/>
                        </div>
                    </c:if>
                </crfn:form>

            </c:if>

            <c:if test="${actionBean.paginatedList == null || empty actionBean.paginatedList.list}">
                <div class="system-msg" style="margin-top:20px;margin-bottom:5px">No sources found!</div>
            </c:if>

    </stripes:layout-component>
</stripes:layout-render>
