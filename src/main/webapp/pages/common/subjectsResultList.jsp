<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-definition>

	<div style="margin-top:20px">
		<c:choose>
			<c:when test="${actionBean.resultList!=null && fn:length(actionBean.resultList)>0}">
				<c:if test="${actionBean.matchCount>0}">
					<c:set var="pagination" value="${actionBean.pagination}"/>
					<span>
						${actionBean.matchCount} matches found
						<c:if test="${pagination!=null}">						
							<c:choose>
								<c:when test="${pagination.rowsFrom==pagination.rowsTo}">, displaying last one</c:when>
								<c:otherwise>, displaying ${pagination.rowsFrom} to ${pagination.rowsTo}</c:otherwise>
							</c:choose>			
						</c:if>
					</span>
					<c:if test="${pagination!=null}">
						<span>
							<c:choose>
								<c:when test="${pagination.curPageNum==1}">
									[First/Prev]
								</c:when>
								<c:otherwise>
									[<a href="${pagination.first.href}">First</a>/<a href="${pagination.prev.href}">Prev</a>]
								</c:otherwise>
							</c:choose>
							<c:forEach items="${pagination.group}" var="numberedPage" varStatus="paginationGroupStatus">
								<c:choose>
									<c:when test="${numberedPage.selected}">
										<c:if test="${paginationGroupStatus.count>1}">, </c:if><strong>${numberedPage.number}</strong>
									</c:when>
									<c:otherwise>
										<c:if test="${paginationGroupStatus.count>1}">, </c:if><a href="${numberedPage.href}">${numberedPage.number}</a>
									</c:otherwise>
								</c:choose>			
							</c:forEach>
							<c:choose>
								<c:when test="${pagination.curPageNum==pagination.numOfPages}">
									[Next/Last]
								</c:when>
								<c:otherwise>
									[<a href="${pagination.next.href}">Next</a>/<a href="${pagination.last.href}">Last</a>]
								</c:otherwise>
							</c:choose>
						</span>
					</c:if>
				</c:if>
				<table id="resourcesResultList" class="${tableClass}">
					<thead>
						<c:forEach items="${actionBean.columns}" var="col">
							<th scope="col">
								<c:choose>
									<c:when test="${col.sortable}">							
										<a title="${col.title}" href="${crfn:sortUrl(actionBean,col.predicateUri)}">
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
									<td>${crfn:formatPredicateObjects(resultListItem, col.predicateUri)}</td>
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
				<p>No matches found!</p>
			</c:otherwise>
		</c:choose>
	</div>
	
</stripes:layout-definition>