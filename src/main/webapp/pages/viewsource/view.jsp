<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-definition>
    <h1>View source</h1>
    <br />
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
			</tr>            <tr>
                <th scope="row">E-mails</th>
                <td><c:out value="${actionBean.harvestSource.emails}" /></td>
            </tr>
            <tr>
                <th scope="row">Date created</th>
                <td><c:out value="${actionBean.harvestSource.timeCreated}" /></td>
            </tr>
            <tr>
                <th scope="row">Harvest interval</th>
                <td>
					<c:out value="${actionBean.intervalMinutesDisplay}"/>
					<c:if test="${actionBean.intervalMinutesDisplay}=='0'">&nbsp;(i.e not to be batch-harvested)</c:if>
				</td>
            </tr>
            <tr>
                <th scope="row">Last harvest</th>
                <td><c:out value="${actionBean.harvestSource.lastHarvest}" /></td>
            </tr>
            <tr>
                <th scope="row">Urgency score</th>
                <td>${actionBean.urgencyScoreFormatted}</td>
            </tr>
            <c:if test="${not empty actionBean.harvestSource.mediaType}">
                <tr>
                    <th scope="row">Media type</th>
                    <td>${actionBean.harvestSource.mediaType}</td>
                </tr>
            </c:if>
            <tr>
                <th scope="row">"Schema" source</th>
                <td>${actionBean.schemaSourceDisplay}</td>
            </tr>
            <tr>
                <th scope="row">"Priority" source</th>
                <td>${actionBean.prioritySourceDisplay}</td>
            </tr>
            <c:if test="${actionBean.harvestSource.unavailable}">
                <tr>
                    <td colspan="2" class="warning-msg" style="color: #E6E6E6">The
                    source has been unavailable for too many times!</td>
                </tr>
            </c:if>
            
            <c:if test='${sessionScope.crUser!=null}'>
                <tr>
                    <td colspan="2" style="padding-top: 10px">
                        <c:if test='${crfn:hasPermission(sessionScope.crUser.userName, "/", "u")}'>
                            <stripes:submit name="goToEdit" value="Edit" title="Edit this harvest source"/>
                        </c:if>
                        <stripes:submit name="scheduleUrgentHarvest" value="Schedule urgent harvest" />
                        <stripes:submit name="export" value="Export triples" />
                    </td>
                </tr>
            </c:if>


        </table>
    </crfn:form>
</stripes:layout-definition>
