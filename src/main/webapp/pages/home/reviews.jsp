<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="QA Raports">

<stripes:layout-component name="contents">

	<c:choose>
		<c:when test="${actionBean.reviewView}">
		
		<h1>Review #${actionBean.reviewId}</h1>
		
		<table>
			<col style="width:100em"/>
			<col style="width:300em"/>
			<tr>
				<td><label><b>Title</b></label></td>
				<td><label>${actionBean.review.title}</label></td>
			</tr>
			<tr>
				<td><label><b>Object URL</b></label></td>
				<td><label>${actionBean.review.objectUrl}</label></td>
			</tr>
			<tr>
				<td><label><b>Review content</b></label></td>
				<td><label>Not implemented yet</label></td>
			</tr>
		</table>
		
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${not empty param.add}">
					<h1>Adding new review</h1>
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
								<td><stripes:text id="objecturl" name="review.objectUrl" size="80">${ param.addUrl }</stripes:text></td>
							</tr>
							<tr>
								<td><label class="question" for="reviewcontent">Review content</label></td>
								<td><stripes:textarea id="reviewcontent" name="review.reviewContent" cols="80" rows="10" disabled="true">Saving this field is not implemented yet</stripes:textarea></td>
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
		
				<div id="operations">
						<ul>
							<li>
								<stripes:link href="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/reviews?add=Add">Add new Review</stripes:link>
							</li>
						</ul>
					</div>
				
					<c:choose>
						<c:when test="${not empty actionBean.reviews}">
						<crfn:form id="reviewList"
						action="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/reviews?delete=1"
						method="post">
						<display:table name="${actionBean.reviews}" class="sortable"
							pagesize="20" sort="list" id="review" htmlId="reviews"
							requestURI="${actionBean.urlBinding}" style="width:100%">
							<display:column title="" sortable="false" style="width:50px;">
								<input type="checkbox"
									value="${ review.reviewSubjectHtmlFormatted }" name='reviewSubjectUrls'></input>
							</display:column>
							<display:column title="Title" sortable="false" style="width:300px;">
								<stripes:link href="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/reviews/${review.reviewID}">
									${review.title}
								</stripes:link>
							</display:column>
							<display:column title="URL" sortable="false">
								<stripes:link href="/factsheet.action">${review.objectUrl}
									<stripes:param name="uri" value="${review.objectUrl}" />
								</stripes:link>
							</display:column>
							
						</display:table>
						<div><stripes:submit name="delete" value="Delete Reviews"
							title="Delete selected reviews" /> <input type="button"
							name="selectAll" value="Select all"
							onclick="toggleSelectAll('reviewList');return false" /></div>
					</crfn:form>
						</c:when>
						<c:otherwise>
							<p>No Reviews found.</p>
						</c:otherwise>
					</c:choose>
				</c:otherwise>
			</c:choose>
		</c:otherwise>
	</c:choose>
	
</stripes:layout-component>


</stripes:layout-render>