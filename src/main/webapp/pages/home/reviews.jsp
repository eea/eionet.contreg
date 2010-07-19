<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="QA Raports">

<stripes:layout-component name="contents">

	<c:choose>
		<c:when test="${actionBean.reviewView}">
			
			<c:if test="${ actionBean.review != null }">
				<c:choose>
					<c:when test="${not empty param.edit}">
						<h1>Edit review #${actionBean.reviewId}</h1>
							<crfn:form action="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/reviews/${actionBean.reviewId}" method="post">	
							<table>
								<col style="width:10em"/>
								<col style="width:100%"/>
								<tr>
									<td><label class="question required" for="title">Title</label></td>
									<td><stripes:text id="title" name="review.title" size="80">${ actionBean.review.title }</stripes:text></td>
								</tr>
								<tr>
									<td><label class="question required" for="objecturl">Object URL</label></td>
									<td><stripes:text id="objecturl" name="review.objectUrl" size="80">${ actionBean.review.objectUrl }</stripes:text></td>
								</tr>
								<tr>
									<td><label class="question" for="reviewcontent">Review content</label></td>
									<td><stripes:textarea id="reviewcontent" name="review.reviewContent" cols="80" rows="10">${ actionBean.review.reviewContent }</stripes:textarea></td>
								</tr>
								<tr>
									<td colspan="2">
										<stripes:submit name="cancel" value="Cancel"/>
										<stripes:submit name="editSave" value="Save review"/>
									</td>
								</tr>
							</table>
						</crfn:form>
					</c:when>
					<c:when test="${not empty param.editAttachments}">
					
					<div id="operations">
						<ul>
							<li>
								<stripes:link href="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/reviews/${actionBean.reviewId}">Back</stripes:link>
							</li>
						</ul>
					</div>
					
						<h1>Review #${actionBean.reviewId} - edit attachments</h1>
						
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
						</table>
						
						<crfn:form action="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/reviews/${actionBean.reviewId}?editAttachments=Edit" method="post">
							<table>
								<col style="width:10em"/>
								<col style="width:100%"/>
								<tr>
									<td><label class="question" for="attachment"><b>Upload file:</b></label></td>
									<td><stripes:file name="attachment" id="attachment" size="80"/></td>
								</tr>
								<tr>
									<td colspan="2">
										<stripes:submit name="upload" value="Upload"/>       
									</td>
								</tr>
							</table>
						</crfn:form>
						
						<c:if test="${not empty actionBean.attachment}">
						<p>${ actionBean.attachment.fileName } / ${ actionBean.attachment.size }</p>
						</c:if>
						
						<br/>
						<c:choose>
							<c:when test="${not empty actionBean.review.attachments}">
							<crfn:form id="attachmentList"
								action="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/reviews/${actionBean.reviewId}?editAttachments=Edit?deleteAttachments=delete"
								method="post">
								<display:table name="${actionBean.review.attachments}" class="sortable"
									pagesize="20" sort="list" id="attachment" htmlId="attachments"
									requestURI="${actionBean.urlBinding}" style="width:100%">
									<display:column title="" sortable="false" style="width:50px;">
										<input type="checkbox" value="${ attachment }" name='attachmentList'></input>
									</display:column>
									<display:column title="URL" sortable="false">
										<stripes:link href="/download.action?download=${ attachment }">
										${ attachment }	
										</stripes:link>
									</display:column>
								</display:table>
								
								<div><stripes:submit name="deleteAttachments" value="Delete"
									title="Delete selected attachments" /> <input type="button"
									name="selectAll" value="Select all"
									onclick="toggleSelectAll('attachmentList');return false" /></div>
								</crfn:form>
							</c:when>
							<c:otherwise>
								<p>No attachments added.</p>
							</c:otherwise>
						</c:choose>
					</c:when>
					
					<c:otherwise>
					
					<div id="operations">
						<ul>
							<li>
								<stripes:link href="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/reviews/">Back to reviews</stripes:link>
							</li>
						</ul>
					</div>
					<br/>
						<ul id="dropdown-operations">
							<li><a href="#">Operations</a>
								<ul>
									<li>
										<stripes:link class="link-plain" href="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/reviews/${actionBean.reviewId}?edit=Edit">
										Edit
										</stripes:link>
									</li>
									<li>
										<stripes:link class="link-plain" href="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/reviews/${actionBean.reviewId}?editAttachments=Edit">
										Edit attachments
										</stripes:link>
									</li>
									<li>
										<stripes:link class="link-plain" href="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/reviews?deleteReview=${actionBean.reviewId}">
										Delete
										</stripes:link>
									</li>
								</ul>
							</li>
						</ul>
					
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
								<td colspan="2"><label><b>Review content</b> (${ actionBean.review.reviewContentType })</label></td>
							</tr>
							<tr>
								<td colspan="2"><label>${ actionBean.review.reviewContent }</label></td>
							</tr>
						</table>
						<br/>
						<c:choose>
							<c:when test="${not empty actionBean.review.attachments}">
								<display:table name="${actionBean.review.attachments}" class="sortable"
									pagesize="20" sort="list" id="attachment" htmlId="attachments"
									requestURI="${actionBean.urlBinding}" style="width:100%">
									<display:column title="URL" sortable="false">
										<stripes:link href="/download.action?download=${ attachment }">
										${ attachment }	
										</stripes:link>
									</display:column>
								</display:table>
							</c:when>
							<c:otherwise>
								<p>No attachments added.</p>
							</c:otherwise>
						</c:choose>
						
					</c:otherwise>
				</c:choose>
			</c:if>
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
								<td><stripes:textarea id="reviewcontent" name="review.reviewContent" cols="80" rows="10"></stripes:textarea></td>
							</tr>
							<tr>
								<td colspan="2">
									<stripes:submit name="addSave" value="Add review"/>       
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
						action="${actionBean.baseHomeUrl}${actionBean.attemptedUserName}/reviews?delete=delete"
						method="post">
						<display:table name="${actionBean.reviews}" class="sortable"
							pagesize="20" sort="list" id="review" htmlId="reviews"
							requestURI="${actionBean.urlBinding}" style="width:100%">
							<display:column title="" sortable="false" style="width:50px;">
								<input type="checkbox"
									value="${review.reviewID}" name='reviewIds'></input>
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