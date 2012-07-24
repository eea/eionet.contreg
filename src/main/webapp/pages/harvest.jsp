<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Harvest">

    <stripes:layout-component name="contents">

        <h1>Harvest</h1>

        <table>
            <tr>
                <td>Harvest source:</td>
                <td>
                    <stripes:link href="/source.action" event="preViewHarvestSource">
                        <c:out value="${actionBean.harvestSourceDTO.url}"/>
                        <stripes:param name="harvestSource.sourceId" value="${actionBean.harvestSourceDTO.sourceId}"/>
                    </stripes:link>
                </td>
            </tr>
            <tr>
                <td>Type:</td>
                <td><c:out value="${actionBean.harvestDTO.harvestType}"/></td>
            </tr>
            <tr>
                <td>User:</td>
                <td><c:out value="${actionBean.harvestDTO.user}"/></td>
            </tr>
            <tr>
                <td>Status:</td>
                <td>
                    <c:out value="${actionBean.harvestDTO.status}"/>
                </td>
            </tr>
             <tr>
                <td>Response code:</td>
                <td>
                    <c:out value="${actionBean.harvestDTO.responseCodeString}"/>
                </td>
            </tr>
            <tr>
                <td>Started:</td>
                <td><fmt:formatDate value="${actionBean.harvestDTO.datetimeStarted}" pattern="dd MMM yy HH:mm:ss"/></td>
            </tr>
            <tr>
                <td>Finished:</td>
                <td><fmt:formatDate value="${actionBean.harvestDTO.datetimeFinished}" pattern="dd MMM yy HH:mm:ss"/></td>
            </tr>
            <tr>
                <td>Triples:</td>
                <td><c:out value="${actionBean.harvestDTO.totalStatements}"/></td>
            </tr>
        </table>
        <br/><br/>
        <c:choose>
            <c:when test="${(empty actionBean.fatals) && (empty actionBean.errors) && (empty actionBean.warnings) && (empty actionBean.infos)}">
                <strong>No messages found for this harvest.</strong>
            </c:when>
            <c:otherwise>
                <c:if test="${!(empty actionBean.fatals)}">
                    <strong>Fatal errors:</strong>
                    <table class="datatable">
                        <thead>
                            <tr>
                                <th scope="col">Message</th>
                                <th scope="col">StackTrace</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${actionBean.fatals}" var="msg" varStatus="loop">
                                <tr>
                                    <td><c:out value="${msg.message}"/></td>
                                    <td>${crfn:formatStackTrace(fn:escapeXml(msg.stackTrace))}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:if>
                <c:if test="${!(empty actionBean.errors)}">
                    <strong>Errors:</strong>
                    <table class="datatable">
                        <thead>
                            <tr>
                                <th scope="col">Message</th>
                                <th scope="col">StackTrace</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${actionBean.errors}" var="msg" varStatus="loop">
                                <tr>
                                    <td><c:out value="${msg.message}"/></td>
                                    <td>${crfn:formatStackTrace(fn:escapeXml(msg.stackTrace))}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:if>
                <c:if test="${!(empty actionBean.warnings)}">
                    <strong>Warnings:</strong>
                    <table class="datatable">
                        <thead>
                            <tr>
                                <th scope="col">Message</th>
                                <th scope="col">StackTrace</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${actionBean.warnings}" var="msg" varStatus="loop">
                                <tr>
                                    <td><c:out value="${msg.message}"/></td>
                                    <td>${crfn:formatStackTrace(fn:escapeXml(msg.stackTrace))}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:if>
                <c:if test="${!(empty actionBean.infos)}">
                    <strong>Info messages:</strong>
                    <ul>
                        <c:forEach items="${actionBean.infos}" var="info" varStatus="loop">
                            <li>
                                <c:out value="${info.message}"/></td>
                            </li>
                        </c:forEach>
                    </ul>
                </c:if>
            </c:otherwise>
        </c:choose>

    </stripes:layout-component>
</stripes:layout-render>
