<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-definition>
    <h1>View source</h1>
    <br />

	<c:if test="${actionBean.harvestSource.permanentError}">
        <div class="warning-msg"><c:out value="The source is marked with permanent error!"/></div>
	</c:if>

    <crfn:form action="/source.action" focus="">
        <stripes:hidden name="harvestSource.sourceId" />
        <stripes:hidden name="harvestSource.url" />
        <table class="datatable">
			<tr>
				<th scope="row">URL</th>
				<td>
					<stripes:link href="/factsheet.action">
						<c:out value="${actionBean.harvestSource.url}"/>
						<stripes:param name="uri" value="${actionBean.harvestSource.url}"/>
					</stripes:link>
				</td>
			</tr>
			<c:if test="${not empty actionBean.harvestSource.emails}">
	            <tr>
	                <th scope="row">E-mails</th>
	                <td><c:out value="${actionBean.harvestSource.emails}" /></td>
	            </tr>
            </c:if>
            <tr>
                <th scope="row">Date created</th>
                <td><fmt:formatDate value="${actionBean.harvestSource.timeCreated}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
            </tr>
            <tr>
                <th scope="row">Harvest interval</th>
                <td>
                    <c:choose>
	                    <c:when test="${empty actionBean.harvestSource.intervalMinutes || actionBean.harvestSource.intervalMinutes<=0}">
	                        <c:out value="not to be batch-harvested"/>
	                    </c:when>
	                    <c:otherwise>
	                        <c:out value="${actionBean.intervalMinutesDisplay}"/>
	                    </c:otherwise>
                    </c:choose>
				</td>
            </tr>
            <tr>
                <th scope="row">Last harvest</th>
                <td>
                    <c:choose>
                        <c:when test="${empty actionBean.harvestSource.lastHarvest}">
                            <c:out value="hasn't been harvested yet"/>
                        </c:when>
                        <c:otherwise>
                            <fmt:formatDate value="${actionBean.harvestSource.lastHarvest}" pattern="yyyy-MM-dd HH:mm:ss"/>
                            <c:if test="${actionBean.harvestSource.lastHarvestFailed && not actionBean.harvestSource.permanentError}">
                                <span style="color:red"><c:out value="(last harvest failed!)"/></span>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
            <tr>
                <th scope="row">Statements</th>
                <td><c:out value="${actionBean.harvestSource.statements}"/></td>
            </tr>
            <c:if test="${not empty actionBean.harvestSource.intervalMinutes && actionBean.harvestSource.intervalMinutes>0}">
	            <tr>
	                <th scope="row">Urgency score</th>
	                <td>
	                    <c:choose>
	                        <c:when test="${actionBean.harvestSource.harvestUrgencyScore<=0}">
	                            <c:out value="cannot be calculated"/>
	                        </c:when>
	                        <c:otherwise>
	                            <fmt:formatNumber value="${actionBean.harvestSource.harvestUrgencyScore}" pattern="#.####"/>
	                        </c:otherwise>
	                    </c:choose>
	                </td>
	            </tr>
            </c:if>
            <c:if test="${not empty actionBean.harvestSource.mediaType}">
                <tr>
                    <th scope="row">Media type</th>
                    <td><c:out value="${actionBean.harvestSource.mediaType}"/></td>
                </tr>
            </c:if>
            <tr>
                <th scope="row">"Schema" source</th>
                <td>
	                <c:choose>
	                    <c:when test="${actionBean.schemaSource}">
	                        <c:out value="yes"/>
	                    </c:when>
	                    <c:otherwise>
	                        <c:out value="no"/>
	                    </c:otherwise>
	                </c:choose>
                </td>
            </tr>
            <tr>
                <th scope="row">"Priority" source</th>
                <td>
                    <c:choose>
                        <c:when test="${actionBean.harvestSource.prioritySource}">
                            <c:out value="yes"/>
                        </c:when>
                        <c:otherwise>
                            <c:out value="no"/>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
            <c:if test="${actionBean.harvestSource.unavailable}">
                <tr>
                    <td colspan="2" class="warning-msg" style="color: #E6E6E6">
                        <c:out value="The source has been unavailable for too many times!"/>
                    </td>
                </tr>
            </c:if>

            <tr>
                <td colspan="2" style="padding-top: 10px">
                    <c:if test='${crfn:userHasPermission(pageContext.session, "/registrations", "u")}'>
                        <stripes:submit name="goToEdit" value="Edit" title="Edit this harvest source"/>
                        <stripes:submit name="scheduleUrgentHarvest" value="Schedule urgent harvest" />
                    </c:if>
                    <stripes:submit name="export" value="Export triples" />
                </td>
            </tr>

        </table>
    </crfn:form>
</stripes:layout-definition>
