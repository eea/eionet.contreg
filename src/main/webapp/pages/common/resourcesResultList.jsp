<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-definition>

	<div style="margin-top:20px">
		<c:choose>
			<c:when test="${actionBean.resultList!=null && fn:length(actionBean.resultList)>0}">
			
				<table id="resourcesResultList" class="${tableClass}">
					<thead>
						<c:forEach items="${actionBean.columns}" var="col">
							<th scope="col">
								<c:choose>
									<c:when test="${col.sortable}">							
										<a title="${col.title}" href="${crfn:sortUrl(actionBean,pageContext.request,col.property)}">
											${col.title}
										</a>
									</c:when>
									<c:otherwise>
										${col.title}
									</c:otherwise>
								</c:choose>
							</th>
						</c:forEach>
					</thead>
					<tbody>
						<c:forEach items="${actionBean.resultList}" var="resultListItem" varStatus="rowStatus">
							<tr
								<c:choose>
									<c:when test="${rowStatus.count%2 != 0}">
							 			class="zebraodd"
									</c:when>
									<c:otherwise>
										class="zebraeven"
									</c:otherwise>
								</c:choose>>
								<c:forEach items="${actionBean.columns}" var="col">
									<td>${crfn:formatPredicateObjects(resultListItem, col.property)}</td>
								</c:forEach>
								<td>
									<stripes:link href="/factsheet.action">
										<img src="${pageContext.request.contextPath}/images/view2.gif" title="View factsheet" alt="View factsheet"/>
										<stripes:param name="uri" value="${resultListItem.uri}" />
									</stripes:link>									
								</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
				
			</c:when>
			<c:otherwise>
				<p>No results found!</p>
			</c:otherwise>
		</c:choose>
	</div>
	
</stripes:layout-definition>