<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Resource properties">

	<stripes:layout-component name="contents">
	
        <h1>Resource factsheet</h1>
        
	    <c:choose>
		    <c:when test="${actionBean.subject!=null}">
		    	<c:set var="subjectUrl" value="${actionBean.subject.url}"/>
		    	<div style="margin-top:20px">
		    		<c:choose>
		    			<c:when test="${subjectUrl!=null}">
		    				<p>Click <a href="${subjectUrl}">here</a> to go to the resource's original location.</p>
		    			</c:when>
		    			<c:otherwise>
		    				<p>Link to the resource's original location was not found.</p>
		    			</c:otherwise>
		    		</c:choose>
			    	<table class="datatable" width="100%" cellspacing="0" summary="">
			    		<tbody>
					    	<c:forEach var="predicate" items="${actionBean.subject.predicates}">
					    		<c:forEach items="${predicate.value}" var="object" varStatus="objectsStatus">
						    		<tr>
						    			<th>
						    				<c:choose>
						    					<c:when test="${objectsStatus.count==1}">${predicate.key}</c:when>
						    					<c:otherwise>&nbsp;</c:otherwise>
						    				</c:choose>
						    			</th>
						    			<td>${object.value}</td>
						    		</tr>
						    	</c:forEach>
					    	</c:forEach>
					    </tbody>
			    	</table>
			    </div>				    
		    </c:when>
		    <c:otherwise>
				No such resource found! 
			</c:otherwise>
		</c:choose>
				
	</stripes:layout-component>
</stripes:layout-render>
