<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-definition>
	<h1>View source</h1>
	<br />
	<crfn:form action="/source.action" focus="">
		<stripes:hidden name="harvestSource.sourceId" />
		<stripes:hidden name="harvestSource.url" />
		<table>
			<tr>
				<td>URL:</td>
				<td><a 
					href="/factsheet.action?uri=${crfn:urlEncode(actionBean.harvestSource.url)}"><c:out
					value="${actionBean.harvestSource.url}" /></a></td>
			</tr>
			<tr>
				<td>E-mails:</td>
				<td><c:out value="${actionBean.harvestSource.emails}" /></td>
			</tr>
			<tr>
				<td>Date created:</td>
				<td><c:out value="${actionBean.harvestSource.timeCreated}" /></td>
			</tr>
			<tr>
				<td>Harvest interval:</td>
				<td><c:out value="${actionBean.intervalMinutesDisplay}" /></td>
			</tr>
			<tr>
				<td>Last harvest:</td>
				<td><c:out value="${actionBean.harvestSource.lastHarvest}" /></td>
			</tr>
			<tr>
				<td>Urgency score:</td>
				<td>${actionBean.urgencyScoreFormatted}</td>
			</tr>
			<c:if test="${actionBean.harvestSource.unavailable}">
				<tr>
					<td colspan="2" class="warning-msg" style="color: #E6E6E6">The
					source has been unavailable for too many times!</td>
				</tr>
			</c:if>

			<tr>
				<td colspan="2" style="padding-top: 10px">
					<stripes:submit name="goToEdit" value="Edit" title="Edit this harvest source"/>
					<stripes:submit name="scheduleUrgentHarvest" value="Schedule urgent harvest" />
					<stripes:submit name="export" value="Export triples" />
				</td>
			</tr>
			
			
						    				
		</table>
	</crfn:form>
</stripes:layout-definition>
