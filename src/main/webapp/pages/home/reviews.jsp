<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="QA Raports">

<stripes:layout-component name="contents">

	<c:choose>
		<c:when test="${not empty param.add}">
			<h1>Add new QA Raport</h1>
			<crfn:form action="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/reviews" method="post">
				<table>
					<col style="width:10em"/>
					<col style="width:100%"/>
					<tr>
						<td><label class="question required" for="title">Title</label></td>
						<td><stripes:text id="title" name="review.title" size="80"/></td>
					</tr>
					<tr>
						<td><label class="question required" for="objecturl">Object URL</label></td>
						<td><stripes:text id="objecturl" name="review.objectUrl" size="80"/></td>
					</tr>
					<tr>
						<td><label class="question" for="reviewcontent">Review content</label></td>
						<td><stripes:textarea id="reviewcontent" name="review.reviewContent" cols="80" rows="10"></stripes:textarea></td>
					</tr>
					<tr>
						<td colspan="2">
							<stripes:submit name="save" value="Save"/>       
						</td>
					</tr>
				</table>
			</crfn:form>
			
			
			
		</c:when>
		<c:otherwise>
		
		
		<h1>My ${actionBean.section}</h1>

		<c:if test="${not empty param.save}">
		<p>Saved Review: ${actionBean.review.title}, ${actionBean.review.objectUrl}, ${actionBean.review.reviewContent} </p>
		</c:if>

		
		<div id="operations">
				<ul>
					<li>
						<stripes:link href="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/reviews?add=Add">Add new Review</stripes:link>
					</li>
				</ul>
			</div>
		
			<c:choose>
				<c:when test="${not empty actionBean.raportsListing}">
					<display:table name="">
						<display:column title="Date" sortable="false" style="width:150px;"></display:column>
					</display:table>
				</c:when>
				<c:otherwise>
					<p>No Reviews found.</p>
				</c:otherwise>
			</c:choose>
		</c:otherwise>
	</c:choose>
	
</stripes:layout-component>


</stripes:layout-render>