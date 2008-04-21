<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-definition>

	<div style="margin-top:20px">
		<c:choose>
			<c:when test="${actionBean.resultList!=null && fn:length(actionBean.resultList)>0}">
				<display:table name="${actionBean.resultList}" class="${tableClass}" pagesize="${(pageSize==null || empty pageSize) ? 20 : pageSize}" requestURI="${actionBean.urlBinding}" id="resultListItem" htmlId="resourcesResultList" sort="list">
					<c:forEach var="col" items="${actionBean.columns}">
						<display:column property="${col.propertyKey}" title="${col.title}" sortable="${col.sortable}"/>
					</c:forEach>
					<display:column>
						<stripes:link href="/factsheet.action">
							<img src="${pageContext.request.contextPath}/images/view2.gif" title="View factsheet" alt="View factsheet"/>
							<stripes:param name="uri" value="${resultListItem.resourceUri}" />
						</stripes:link>
					</display:column>
				</display:table>
			</c:when>
			<c:otherwise>
				<p>No results found!</p>
			</c:otherwise>
		</c:choose>
	</div>
	
</stripes:layout-definition>