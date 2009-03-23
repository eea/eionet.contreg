<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

	<stripes:layout-component name="contents">
	
        <h1>Resource factsheet</h1>
        
	    <c:choose>
		    <c:when test="${actionBean.subject!=null}">
		    	<c:set var="subjectUrl" value="${actionBean.subject.url}"/>
		    	<div style="margin-top:20px">
	    			<c:if test="${subjectUrl!=null}">
	    				<p>Resource URL: <a href="${subjectUrl}">${subjectUrl}</a></p>
	    			</c:if>
		    		<c:if test="${actionBean.subject.predicates!=null && fn:length(actionBean.subject.predicates)>0}">
				    	<table class="datatable" width="100%" cellspacing="0" summary="">
				    		<thead>
								<th scope="col" class="scope-col">Property</th>
								<th scope="col" class="scope-col">Value</th> 
								<th scope="col" class="scope-col">Source</th>
							</thead>
				    		<tbody>
						    	<c:forEach var="predicate" items="${actionBean.subject.predicates}">
						    		<c:forEach items="${predicate.value}" var="object" varStatus="objectsStatus">
							    		<tr>
							    			<th scope="row" class="scope-row" title="${predicate.key}">
							    				<c:choose>
							    					<c:when test="${objectsStatus.count==1}">
							    						${crfn:getPredicateLabel(actionBean.predicateLabels, predicate.key)}
							    					</c:when>
							    					<c:otherwise>&nbsp;</c:otherwise>
							    				</c:choose>
							    			</th>
							    			<td>${object.value}</td>
							    			<td>
							    				<c:choose>
							    					<c:when test="${object.sourceSmart!=null}">
										    			<stripes:link href="/factsheet.action">
										    				<img src="${pageContext.request.contextPath}/images/harvest_source.png" title="${object.sourceSmart}" alt="${object.sourceSmart}"/>
										    				<stripes:param name="uri" value="${object.sourceSmart}"/>
														</stripes:link>
													</c:when>
													<c:otherwise>&nbsp;</c:otherwise>
												</c:choose>
											</td>
							    		</tr>
							    	</c:forEach>
						    	</c:forEach>
						    </tbody>
				    	</table>
				    </c:if>
			    </div>				    
		    </c:when>
		    <c:otherwise>
				Nothing is known about this resource! 
			</c:otherwise>
		</c:choose>
				
	</stripes:layout-component>
</stripes:layout-render>
