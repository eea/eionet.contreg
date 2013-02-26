<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Add Harvesting Source">

    <stripes:layout-component name="contents">
        <h1>Add source</h1>
        <crfn:form action="/source.action" method="post">
            <table>
                <tr>
                    <td><label class="question required" for="harvesturl">URL</label></td>
                    <td><stripes:text id="harvesturl" name="harvestSource.url" size="80"/></td>
                </tr>
                <c:if test='${crfn:userHasPermission(pageContext.session, "/registrations", "u")}'>
                    <tr>
                        <td><label class="question" for="emails">E-mails</label></td>
                        <td><stripes:text id="emails" name="harvestSource.emails" size="80"/></td>
                    </tr>
                    <tr>
                        <td><label class="question" for="interval">Harvest interval</label></td>
                        <td>
                            <stripes:text id="interval" name="harvestSource.intervalMinutes" size="10" value="6"/>
                            <stripes:select name="intervalMultiplier" value="10080">
                                <c:forEach items="${actionBean.intervalMultipliers}" var="intervalMultiplier">
                                    <stripes:option value="${intervalMultiplier.key}" label="${intervalMultiplier.value}"/>
                                </c:forEach>
                            </stripes:select>
                        </td>
                    </tr>
                    <tr>
                        <td><label class="question" for="mediaType">Media type</label></td>
                        <td><stripes:select id="mediaType" name="harvestSource.mediaType" value="${actionBean.harvestSource.mediaType}">
                            <c:forEach items="${actionBean.mediaTypes}" var="type">
                                <stripes:option value="${type}" label="${type}"/>
                            </c:forEach>
                            </stripes:select>
                        </td>
                    </tr>
                    <tr>
                        <td><label class="question" for="schema">Is "Schema" source</label></td>
                        <td>
                            <stripes:checkbox name="schemaSource" id="schema"/>
                        </td>
                    </tr>
                    <tr>
                        <td><label class="question" for="priority">Is "Priority" source</label></td>
                        <td>
                            <stripes:checkbox name="harvestSource.prioritySource" id="priority"/>
                        </td>
                    </tr>
                    <tr>
                        <td><label class="question" for="chkSparqlEndpoint">Is SPARQL endpoint</label></td>
                        <td>
                            <stripes:checkbox name="harvestSource.sparqlEndpoint" id="chkSparqlEndpoint"/>
                        </td>
                    </tr>
                </c:if>
                <tr>
                    <td>
                        <stripes:submit name="add" value="Add"/>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${empty actionBean.harvestSource || actionBean.harvestSource.sparqlEndpoint == false}">
                                <input type="checkbox" name="dontHarvest" id="chkDontHarvest"/><label for="chkDontHarvest">Don't schedule urgent harvest</label>
                            </c:when>
                            <c:otherwise>&nbsp;<input type="hidden" name="dontHarvest" value="true"/></c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </table>
        </crfn:form>

    </stripes:layout-component>
</stripes:layout-render>
