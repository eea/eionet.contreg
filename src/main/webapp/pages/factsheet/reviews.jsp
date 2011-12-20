<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Reviews">

<stripes:layout-component name="contents">

	<cr:tabMenu tabs="${actionBean.tabs}" />

	<br style="clear:left" />
	<br />

    <c:choose>
        <c:when test="${actionBean.reviewView}">
            <c:if test="${ actionBean.review != null }">
                <c:choose>
                    <c:when test="${not empty param.edit}">
                        <h1>Edit review #${actionBean.reviewId}</h1>
                            <crfn:form action="/reviews.action" method="post">
                            <table>
                                <col style="width:30em"/>
                                <col style="width:200em"/>
                                <tr>
                                    <td><label class="question required" for="title">Title</label></td>
                                    <td><stripes:text id="title" name="review.title" size="80">${actionBean.review.title}</stripes:text></td>
                                </tr>
                                <tr>
                                    <td><label class="question required" for="objecturl">Object URL</label></td>
                                    <td><stripes:text id="objecturl" name="review.objectUrl" size="80">${actionBean.review.objectUrl}</stripes:text></td>
                                </tr>
                                <tr>
                                    <td><label class="question" for="reviewcontent">Review content</label></td>
                                    <td>
                                    	<stripes:textarea id="reviewcontent" name="review.reviewContent" cols="80" rows="20">
                                    		${actionBean.review.reviewContent}
                                    	</stripes:textarea>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="2">
                                    	<stripes:hidden name="reviewId" value="${actionBean.reviewId}"/>
                                    	<stripes:hidden name="uri" value="${actionBean.uri}"/>
                                        <stripes:submit name="cancel" value="Cancel"/>
                                        <stripes:submit name="save" value="Save review"/>
                                    </td>
                                </tr>
                            </table>
                        </crfn:form>
                    </c:when>
                    <c:when test="${not empty param.editAttachments}">
                        <c:if test="${actionBean.usersReview}">
                            <h1>Review #${actionBean.reviewId}, ${actionBean.review.title} - add attachments</h1>
                            <crfn:form action="/reviews.action" method="post">
                                <table>
                                    <col style="width:30em"/>
                                    <col style="width:200em"/>
                                    <tr>
                                        <td><label class="question" for="attachment"><b>Upload file:</b></label></td>
                                        <td><stripes:file name="attachment" id="attachment" size="40"/></td>
                                    </tr>
                                    <tr>
                                        <td colspan="2">
                                        	<stripes:hidden name="reviewId" value="${actionBean.reviewId}"/>
                                    		<stripes:hidden name="uri" value="${actionBean.uri}"/>
                                            <stripes:submit name="upload" value="Upload"/>
                                            <input type="button" name="cancel" value="Cancel" onclick="history.go(-1);"/>
                                        </td>
                                    </tr>
                                </table>
                            </crfn:form>

                            <c:if test="${not empty actionBean.attachment}">
                            <p>${ actionBean.attachment.fileName } / ${ actionBean.attachment.size }</p>
                            </c:if>

                            <br/>
                        </c:if>
                    </c:when>

                    <c:otherwise>
                        <c:if test="${ actionBean.usersReview}">
                        <ul id="dropdown-operations">
                            <li><a href="#">Operations</a>
                                <ul>
                                    <li>
                                        <stripes:link href="/reviews.action">
                                        	<stripes:param name="uri" value="${actionBean.uri}" />
                                        	Back to my reviews
                                        </stripes:link>
                                    </li>
                                    <li>
                                        <stripes:link class="link-plain" href="/reviews.action?edit=Edit">
                                        	<stripes:param name="reviewId" value="${actionBean.reviewId}" />
                                        	<stripes:param name="uri" value="${actionBean.uri}" />
                                        	Edit
                                        </stripes:link>
                                    </li>
                                    <li>
                                        <stripes:link class="link-plain" href="/reviews.action?editAttachments=Edit">
                                        	<stripes:param name="reviewId" value="${actionBean.reviewId}" />
                                        	<stripes:param name="uri" value="${actionBean.uri}" />
                                        	Add attachment
                                        </stripes:link>
                                    </li>
                                    <li>
                                        <stripes:link class="link-plain" href="/reviews.action" event="deleteSingleReview" onclick=" return confirm('Are you sure you want to delete this review?');">
                                        	<stripes:param name="reviewId" value="${actionBean.reviewId}" />
                                        	<stripes:param name="uri" value="${actionBean.uri}" />
                                        	Delete
                                        </stripes:link>
                                    </li>
                                </ul>
                            </li>
                        </ul>
                        </c:if>

                        <h1>Review #${actionBean.reviewId}</h1>
                        <c:if test="${actionBean.obsolete}">
                            <div class="advice-msg">Review is potentially obsolete!</div>
                        </c:if>
                        <table class="datatable">
                            <col style="width:50em"/>
                            <col style="width:300em"/>
                            <tr>
                                <th scope="row" class="scope-row" style="white-space:nowrap">Title</th>
                                <td>${actionBean.review.title}</td>
                            </tr>
                            <tr>
                                <th scope="row" class="scope-row" style="white-space:nowrap">Object URL</th>
                                <td>
                                    <a class="link-external" href="${fn:escapeXml(actionBean.review.objectUrl)}"><c:out value="${actionBean.review.objectUrl}"/></a>
                                </td>
                            </tr>
                            <tr>
                                <th scope="row" class="scope-row" style="white-space:nowrap">Review content</th>
                                <td>${ actionBean.review.reviewContentType }</td>
                            </tr>
                            <tr>
                            <c:choose>
                                <c:when test="${ actionBean.reviewContentPresent }">
                                    <td colspan="2">${ actionBean.reviewContentHTML }</td>
                                </c:when>
                                <c:otherwise>
                                    <td colspan="2"><i><b>HTML preview not available, try downloading the file instead.</b></i></td>
                                </c:otherwise>
                            </c:choose>
                            </tr>
                        </table>
                        <c:choose>
                            <c:when test="${actionBean.usersReview}">
	                            <c:choose>
	                                <c:when test="${not empty actionBean.review.attachments}">
	                                <crfn:form id="attachmentList" action="/reviews.action" method="post">
	                                    <table class="datatable" width="100%" cellspacing="0" summary="">
	                                        <col style="width:100%;"/>
	                                        <thead>
	                                            <th scope="col" class="scope-col">Attachments</th>
	                                        </thead>
	                                        <c:forEach var="attachment" items="${actionBean.review.attachments}">
	                                            <tr>
	                                                <td><input type="checkbox" value="${ attachment }" name='attachmentList'></input>&nbsp;
	                                                <stripes:link href="/download?uri=${ attachment }">
	                                                ${ attachment }
	                                                </stripes:link></td>
	                                            </tr>
	                                        </c:forEach>
	                                    </table>

	                                    <div>
	                                    	<stripes:hidden name="reviewId" value="${actionBean.reviewId}"/>
	                                    	<stripes:hidden name="uri" value="${actionBean.uri}"/>
	                                    	<stripes:submit name="deleteAttachments" value="Delete"
	                                        title="Delete selected attachments"  onclick=" return confirm('Are you sure you want to delete selected attachments?');"/>
	                                        <input type="button" name="selectAll" value="Select all" onclick="toggleSelectAll('attachmentList');return false" /></div>
	                                    </crfn:form>
	                                </c:when>
	                                <c:otherwise>
	                                    <p>No attachments added.</p>
	                                </c:otherwise>
	                            </c:choose>
                            </c:when>
                            <c:otherwise>
                                <c:choose>
                                    <c:when test="${not empty actionBean.review.attachments}">
                                        <table class="datatable" width="100%" cellspacing="0" summary="">
                                        <col style="width:100%;"/>
                                        <thead>
                                            <th scope="col" class="scope-col">Attachment</th>
                                        </thead>
                                        <c:forEach var="attachment" items="${actionBean.review.attachments}">
                                            <tr>
                                                <td><stripes:link href="/download?uri=${ attachment }">
                                                ${ attachment }
                                                </stripes:link></td>
                                            </tr>
                                        </c:forEach>
                                        </table>
                                    </c:when>
                                    <c:otherwise>
                                        <p>No attachments added.</p>
                                    </c:otherwise>
                                </c:choose>
                            </c:otherwise>
                        </c:choose>

                    </c:otherwise>
                </c:choose>
            </c:if>
        </c:when>
        <c:otherwise>
            <c:choose>
                <c:when test="${not empty param.addReview}">
                    <h1>Adding new review</h1>
                        <crfn:form action="/reviews.action" method="post">
                        <table>
                            <col style="width:10em"/>
                            <col style="width:100%"/>
                            <tr>
                                <td><label class="question required" for="title">Title</label></td>
                                <td><stripes:text id="title" name="review.title" size="80"/></td>
                            </tr>
                            <tr>
                                <td><label class="question required" for="objecturl">Object URL</label></td>
                                <td><stripes:text id="objecturl" name="review.objectUrl" size="80">${param.addUrl}</stripes:text></td>
                            </tr>
                            <tr>
                                <td valign="top"><label class="question" for="reviewcontent">Review content</label></td>
                                <td><stripes:textarea id="reviewcontent" name="review.reviewContent" cols="80" rows="10"></stripes:textarea></td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                	<stripes:hidden name="uri" value="${actionBean.user.reviewsUri}"/>
                                    <stripes:submit name="add" value="Add review"/>
                                </td>
                            </tr>
                        </table>
                    </crfn:form>
                </c:when>
                <c:otherwise>
	                <c:if test="${actionBean.usersReview}">
	                	<h1>My reviews</h1>
	                    <div id="operations">
	                        <ul>
	                            <li>
	                                <stripes:link href="/reviews.action?addReview=Add">
	                                	<stripes:param name="uri" value="${actionBean.user.reviewsUri}"/>
	                                	Add new Review
	                                </stripes:link>
	                            </li>
	                        </ul>
	                    </div>
	                </c:if>
	                <c:if test="${!actionBean.usersReview}">
	                    <h1>${actionBean.attemptedUserName}'s reviews</h1>
	                </c:if>

                    <c:choose>
                        <c:when test="${not empty actionBean.reviews}">
                        <crfn:form id="reviewList" action="/reviews.action" method="post">
                            <display:table name="${actionBean.reviews}" class="sortable"
                                pagesize="20" sort="list" id="review" htmlId="reviews" requestURI="/reviews.action" style="width:100%">
                                <c:if test="${actionBean.usersReview}">
                                    <display:column title="" sortable="true" style="width:50px;">
                                        <input type="checkbox" value="${review.reviewID}" name='reviewIds'/>
                                    </display:column>
                                </c:if>
                                <display:column title="Title" sortable="false" style="width:300px;">
                                    <stripes:link href="/reviews.action">
                                    	<stripes:param name="reviewId" value="${review.reviewID}" />
                                    	<stripes:param name="uri" value="${review.reviewSubjectUri}" />
                                        ${review.title}
                                    </stripes:link>
                                </display:column>
                                <display:column title="URL" sortable="false">
                                    <stripes:link href="/factsheet.action">${review.objectUrl}
                                        <stripes:param name="uri" value="${review.objectUrl}" />
                                    </stripes:link>
                                </display:column>
                            </display:table>

                            <c:if test="${actionBean.usersReview}">
                                <div>
                                	<stripes:hidden name="uri" value="${actionBean.uri}"/>
                                	<stripes:submit name="deleteReviews" value="Delete Reviews"
                                    title="Delete selected reviews"  onclick=" return confirm('Are you sure you want to delete selected reviews?');" />
                                    <input type="button" name="selectAll" value="Select all" onclick="toggleSelectAll('reviewList');return false" />
                                </div>
                            </c:if>
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
