<%@ include file="/pages/common/taglibs.jsp"%>
<stripes:layout-definition>
	<c:choose>
		<c:when test="${not empty actionBean.sampleTriples}">
			<table id="sampletriples" class="datatable">
				<caption>Sample triples:</caption>
				<col style="width: 30%" />
				<col style="width: 30%" />
				<col style="width: 40%" />
				<thead>
					<tr>
						<th scope="col">Subject</th>
						<th scope="col">Predicate</th>
						<th scope="col">Object</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach items="${actionBean.sampleTriples}" var="sampleTriple"
						varStatus="loop">
						<tr
							<c:if test="${not empty sampleTriple.objectDerivSource}">class="derived"</c:if>>
							<td><c:choose>
								<c:when test="${sampleTriple.subject!=null}">
									<c:out
										value="${crfn:cutAtFirstLongToken(sampleTriple.subject, 100)}" />
								</c:when>
								<c:otherwise>
				        							Anonymous resource
				        						</c:otherwise>
							</c:choose></td>
							<td><c:out
								value="${crfn:cutAtFirstLongToken(sampleTriple.predicate, 100)}" /></td>
							<td><c:out
								value="${crfn:cutAtFirstLongToken(sampleTriple.object, 100)}" /></td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</c:when>
		<c:otherwise>
			<div class="important-msg">No sample triples found!</div>
		</c:otherwise>
	</c:choose>
</stripes:layout-definition>