<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-definition>

	<c:choose>
		<c:when test="${not empty actionBean.registrations}">
			<display:table name="${actionBean.registrations}" class="datatable"
				pagesize="20" sort="list" id="registrations" htmlId="registratioinslist"
				requestURI="${actionBean.urlBinding}" style="width:100%">
					<display:column title="Subject" sortable="false">
						<stripes:link href="/factsheet.action">${registrations.subject}
							<stripes:param name="uri" value="${registrations.subject}" />
						</stripes:link>
					</display:column>
					<display:column title="Predicate" sortable="false">
						<stripes:link href="/factsheet.action">${registrations.predicate}
							<stripes:param name="uri" value="${registrations.predicate}" />
						</stripes:link>
					</display:column>
					<display:column title="Object" sortable="false">
						${registrations.object}
					</display:column>
			</display:table>
		</c:when>
		<c:otherwise>
			<p>No registrations found.</p>
		</c:otherwise>
	</c:choose>
</stripes:layout-definition>