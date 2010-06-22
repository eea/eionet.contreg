<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-definition>

	<c:choose>
		<c:when test="${not empty actionBean.history}">

			<display:table name="${actionBean.history}" class="sortable"
				pagesize="20" sort="list" id="history" htmlId="historylist"
				requestURI="${actionBean.urlBinding}" style="width:100%">
				<display:column title="Date" sortable="false" style="width:150px;">${history.lastOperation}
									</display:column>
				<display:column title="URL" sortable="false">
					<stripes:link href="/factsheet.action">${history.url}
											<stripes:param name="uri" value="${history.url}" />
					</stripes:link>
				</display:column>
			</display:table>
		</c:when>
		<c:otherwise>
			<p>No history found.</p>
		</c:otherwise>
	</c:choose>
</stripes:layout-definition>